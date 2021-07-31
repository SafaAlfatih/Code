/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;


class JabberModel {

    Formatter formatter ;
    QueueThread thread ;

    public QueueThread getThread() {
        return thread;
    }

  JabberModel(QueueThread qThread) {
      formatter = new Formatter(); // initialize the formatter to start formatting the strings
      thread = qThread ;
    packetQueue = qThread.getQueue();
    qThread.setDaemon(true);
    qThread.addListener(new OpenStreamHandler(),"stream:stream");
    qThread.addListener(new CloseStreamHandler(),"/stream:stream");
    qThread.addListener(msgHandler,"message");
    qThread.addListener(authHandler,"jabber:iq:auth");
    qThread.addListener(new IQHandler(this),"iq");
    qThread.addListener(new RosterHandler(),"jabber:iq:roster");
    qThread.addListener(new JingleHandler(this), "urn:xmpp:jingle:1");
    qThread.start();

  }

  // Create the global queue and session everyone works with
  Session session = new Session();

    public Session getSession() {
        return session;
    }
  PacketQueue packetQueue;
  Authenticator authenticator = new Authenticator();
  public int getSessionStatus() {
    return session.getStatus();
  }

  String version = "v. 1.0 - ";
  public String getVersion(){ return version; }

  String sName;
  public String getServerName()               {return sName;}
  public void   setServerName(String name)    {sName = name;}

  String sAddress;
  public String getServerAddress()            {return sAddress;}
  public void   setServerAddress(String addr) {sAddress = addr;}

  String sPort;
  public String getPort()                     {return sPort;}
  public void   setPort(String port)          {sPort = port;}

  String user;
  public String getUser()                     {return user;}
  public void   setUser(String usr)           {user = usr; }

  String resource;
  public String getResource()                 {return resource;}
  public void   setResource(String res)       {resource = res;}

  String auth;
  public String getAuthMode() {return auth;}
  public void   setAuthMode(String mode) { auth = mode; }

  String password;
  public String getPassword() {return password;}
  public void   setPassword(String pass) {password = pass;}

  public void addStatusListener(StatusListener listener){
    // Create the session and get it setup
    session.addStatusListener(listener);
  }

  public void removeStatusListener(StatusListener listener){
    session.removeStatusListener(listener);
  }

  PacketListener authHandler = new AuthHandler(this);
  MessageHandler msgHandler = new MessageHandler(this);
  Hashtable resultHandlers = new Hashtable();
  public void addResultHandler(String id_code,PacketListener listener){
    resultHandlers.put(id_code,listener);
  }
  public PacketListener removeResultHandler(String id_code){
    return (PacketListener)resultHandlers.remove(id_code);
  }

  public void connect() throws IOException {

    // Create a socket
    session.setSocket(new Socket(sAddress,Integer.parseInt(sPort)));
    session.setStatus(Session.CONNECTED);
//    Client.getLogFrame().logSession(session);

    // Process incoming messages
    (new ProcessThread(packetQueue,session)).start();

    // Send our own "open stream" packet with the server name
    Writer out = session.getWriter();
    session.setJID(new JabberID(user,sName,resource));
    out.write("<?xml version='1.0' encoding='UTF-8' ?><stream:stream to='");
    out.write(sName);
    out.write("' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams'>");
    out.flush();
  }

  // Send close stream fragment
  public void disconnect() throws IOException {
    session.closeStream();
  }

  // Register as user with given password
  // Book should describe how to detect register methods available
  // it described it using waitFor() method takes element + type

  public void register() throws IOException {
    if (auth.equals("0k")){
      register0k();
    } else {
      registerPlain();
    }
  }

  void registerPlain() throws IOException {
    Writer out = session.getWriter();
    out.write("<iq type='set' id='reg_id'><query xmlns='jabber:iq:register'><username>");
    out.write(formatter.format(this.user));
    out.write("</username><password>");
    out.write(formatter.format(this.password));
    out.write("</password></query></iq>");
    out.flush();
    addResultHandler("reg_id",new RegisterHandler(this));
  }

  void registerPlain(String user , String password) throws IOException {
    Writer out = session.getWriter();
    out.write("<iq type='set' id='reg_id'><query xmlns='jabber:iq:register'><username>");
    out.write(""+user+"</username><password>");
    out.write(""+password+"</password></query></iq>");
    out.flush();
    addResultHandler("reg_id",new RegisterHandler(this));
  }

  void register0k() throws IOException {
    String token = authenticator.randomToken();
    String hash = authenticator.getZeroKHash(100,
                                             token.getBytes(),
                                             password.getBytes());
    Writer out = session.getWriter();
    out.write("<iq type='set' id='reg_id'><query xmlns='jabber:iq:register'><username>");
    out.write(this.user);
    out.write("</username><sequence>");
    out.write("100");
    out.write("</sequence><token>");
    out.write(token);
    out.write("</token><hash>");
    out.write(hash);
    out.write("</hash></query></iq>");
    out.flush();
    // Notice password is never sent or stored on server
    addResultHandler("reg_id",new RegisterHandler(this));
  }

  // Authenticate as user with given password
  int counter; // used to generate auth id's
  public void authenticate() throws IOException {
    if (auth.equals("0k")){
      authenticate0k();
    } else if (auth.equals("digest")){
      authenticateDigest();
    } else {
      authenticatePlain();
    }
  }

  // Authenticate as user with given password
  void authenticatePlain() throws IOException {
    addResultHandler("plain_auth_" + Integer.toString(counter),authHandler);
    Writer out = session.getWriter();
    out.write("<iq type='set' id='plain_auth_");
    out.write(Integer.toString(counter++));
    out.write("'><query xmlns='jabber:iq:auth'><username>");
    out.write(formatter.format(this.user));
    out.write("</username><resource>");
    out.write(formatter.format(this.resource));
    out.write("</resource><password>");
    out.write(formatter.format(this.password));
    out.write("</password></query></iq>");
    out.flush();
  }
  void authenticatePlain(String user , String password) throws IOException {
    addResultHandler("plain_auth_" + Integer.toString(counter),authHandler);
    Writer out = session.getWriter();
    out.write("<iq type='set' id='plain_auth_");
    out.write(Integer.toString(counter++));
    out.write("'><query xmlns='jabber:iq:auth'><username>");
    out.write(""+user+"</username><resource>");
    out.write("Android</resource><password>");
    out.write(""+password+"</password></query></iq>");
    out.flush();
  }

  // Authenticate as user with given password
  void authenticateDigest() throws IOException {
    addResultHandler("digest_auth_" + Integer.toString(counter),authHandler);
    Writer out = session.getWriter();
    out.write("<iq type='set' id='digest_auth_");
    out.write(Integer.toString(counter++));
    out.write("'><query xmlns='jabber:iq:auth'><username>");
    out.write(this.user);
    out.write("</username><resource>");
    out.write(this.resource);
    out.write("</resource><digest>");
    out.write(authenticator.getDigest(session.getStreamID(),password));
    out.write("</digest></query></iq>");
    out.flush();
  }

  // Authenticate as user with given password
  void authenticate0k() throws IOException {
    // Initiate authentication here... it is finished in AuthHandler
    Writer out = session.getWriter();
    out.write("<iq type='get' id='auth_get_");
    out.write(Integer.toString(counter++));
    out.write("'><query xmlns='jabber:iq:auth'><username>");
    out.write(this.user);
    out.write("</username></query></iq>");
    out.flush();
  }
   // this method is used to
  public void sendMessage(String recipient,String type,String body) throws IOException {
    Packet packet = new Packet("message");
    UUID uuid = UUID.randomUUID();
    packet.setAttribute("id", uuid.toString());
    packet.setAttribute("status", "sent");
    packet.setFrom(this.user+"@"+this.sName);
    if(type != null)
    {
       packet.setAttribute("type", type);    
    }
    

    
    if (body != null){
      packet.getChildren().add(new Packet("body",body));
    }
    packet.writeXML(session.getWriter());
    Log.trace("Sent : " + packet);
  }

  public void SendGroupMessage(String GroupName,String thread,String id,String body)throws IOException
  {
      Packet packet = new Packet("message");
      UUID uuid = UUID.randomUUID();
    packet.setAttribute("id", uuid.toString());
    packet.setAttribute("status", "sent");
    packet.setFrom(this.user+"@"+this.sName);
    packet.setType("groupchat");

      if (GroupName != null){
      packet.setTo(GroupName);
    }
    if (id != null){
      packet.setID(id);
    }

    if (thread != null){
      packet.getChildren().add(new Packet("thread",thread));
    }
    if (body != null){
      packet.getChildren().add(new Packet("body",body));
    }
    packet.writeXML(session.getWriter());
    Log.trace("Sent : " + packet);

  }
  public void sendRosterGet() throws IOException {
    Packet packet = new Packet("iq");
    packet.setType("get");
    packet.setID("roster_get");
    Packet query = new Packet("query");
    query.setAttribute("xmlns","jabber:iq:roster");
    query.setParent(packet);
    packet.writeXML(session.getWriter());
  }

  public void sendRosterRemove(String jid) throws IOException {
    Packet packet = new Packet("iq");
    packet.setType("set");
    packet.setID("roster_remove");
    Packet query = new Packet("query");
    query.setAttribute("xmlns","jabber:iq:roster");
    query.setParent(packet);
    Packet item = new Packet("item");
    item.setAttribute("subscription","remove");
    item.setAttribute("jid",jid);
    item.setParent(query);
    packet.writeXML(session.getWriter());
  }

  public void sendRosterBlock(String jid) throws IOException {
    Packet packet = new Packet("iq");
    packet.setType("set");
    packet.setID("roster_block");
    Packet query = new Packet("query");
    query.setAttribute("xmlns","jabber:iq:roster");
    query.setParent(packet);
    Packet item = new Packet("item");
    item.setAttribute("subscription","block");
    item.setAttribute("jid",jid);
    item.setParent(query);
    packet.writeXML(session.getWriter());
  }
  
   public void sendRosterUnblock(String jid) throws IOException {
    Packet packet = new Packet("iq");
    packet.setType("set");
    packet.setID("roster_unblock");
    Packet query = new Packet("query");
    query.setAttribute("xmlns","jabber:iq:unblock");
    query.setParent(packet);
    Packet item = new Packet("item");
    item.setAttribute("subscription","unblock");
    item.setAttribute("jid",jid);
    item.setParent(query);
    packet.writeXML(session.getWriter());
  }


  public void sendRosterSet(String jid,String name,Iterator groups) throws IOException {
    Packet packet = new Packet("iq");
    packet.setType("set");
    packet.setID("roster_set");
    Packet query = new Packet("query");
    query.setAttribute("xmlns","jabber:iq:roster");
    query.setParent(packet);
    Packet item = new Packet("item");
    item.setAttribute("jid",jid);
    item.setAttribute("name",name);
    item.setParent(query);
        
    item.setAttribute("subscription","add");
    if(groups != null) {
    while (groups.hasNext()){
      new Packet("group",(String)groups.next()).setParent(item);
    }
    }
    packet.writeXML(session.getWriter());
  }

  public void sendPresence(String recipient,String type,String show,String status,String priority) throws IOException {
    Packet packet = new Packet("presence");

    packet.setFrom(user+"@localhost");
    if (recipient != null){
      packet.setTo(recipient);
    }
    if (type != null){
      packet.setType(type);
    }
    if (show != null){
      packet.getChildren().add(new Packet("show",show));
    }
    if (status != null){
      packet.getChildren().add(new Packet("status",status));
    }
    if (priority != null){
      packet.getChildren().add(new Packet("priority",priority));
    }
    packet.writeXML(session.getWriter());
  }
  void sendXML(String x) throws IOException {
    Writer out = session.getWriter();
    out.write(x);
    out.flush();

  }
   public void createGroup(String groupName) throws IOException
   {
       Packet packet = new Packet("presence");

       if(groupName != null)
       {
           packet.setTo(groupName+".group@"+this.sName+"/"+this.user);
           packet.setFrom(this.user+"@"+this.sName+"/"+this.resource);
       }
       else
       {
           return;
       }
       Packet x = new Packet("x");
       x.setAttribute("xmlns", "http://jabber.org/protocol/muc");
       packet.getChildren().add(x);

       packet.writeXML(session.getWriter());
       Log.trace("Group creation request sent : " + packet);
   }

   public void addMember(String groupName ,String adminName , String memberJid) throws IOException
   {
       addResultHandler("add_member",new memberAdditionHandler(this));
       Packet iq = new Packet("iq");
       if(adminName != null){
           iq.setFrom(adminName);
       }
       if(groupName != null)
       {
           iq.setTo(groupName);
       }

       iq.setType("set");
       UUID uuid = UUID.randomUUID();
       iq.setAttribute("id", uuid.toString());
       Packet query = new Packet("query");
       query.setAttribute("xmlns", "http://jabber.org/protocol/muc#admin");
       Packet item = new Packet("item");
       item.setAttribute("affiliation", "member");
       if(memberJid != null)
       {
                  item.setAttribute("jid", memberJid);

       }

       query.getChildren().add(item);
       iq.getChildren().add(query) ;

       Log.trace("sending member addition request :"+iq);
       iq.writeXML(session.getWriter());

   }
//   <iq from='crone1@shakespeare.lit/desktop'
//    id='member2'
//    to='coven@chat.shakespeare.lit'
//    type='set'>
//  <query xmlns='http://jabber.org/protocol/muc#admin'>
//    <item affiliation='none'  jid='hag66@shakespeare.lit'/>
//  </query>
//</iq>
   public void RemoveMember(String groupName , String adminName , String memberJid) throws IOException
   {
       addResultHandler("remove_member",new memberAdditionHandler(this));
       Packet iq = new Packet("iq");
       if(adminName != null){
           iq.setFrom(adminName);
       }
       if(groupName != null)
       {
           iq.setTo(groupName);
       }

       iq.setType("set");
       UUID uuid = UUID.randomUUID();
       iq.setAttribute("id", uuid.toString());
       Packet query = new Packet("query");
       query.setAttribute("xmlns", "http://jabber.org/protocol/muc#admin");
       Packet item = new Packet("item");
       item.setAttribute("affiliation", "none");
       if(memberJid != null)
       {
                  item.setAttribute("jid", memberJid);

       }

       query.getChildren().add(item);
       iq.getChildren().add(query) ;

       Log.trace("sending member removal request :"+iq);
       iq.writeXML(session.getWriter());
   }

   public void LeaveGroup (String GroupName) throws IOException
   {
        Packet packet = new Packet("presence");
        packet.setType("unavailable");

       if(GroupName != null)
       {
           packet.setTo(GroupName+".group@"+this.sName+"/"+this.user);
           packet.setFrom(this.user+"@"+this.sName+"/"+this.resource);
       }
       else
       {
           return;
       }
       Packet x = new Packet("x");
       x.setAttribute("xmlns", "http://jabber.org/protocol/muc");
       packet.getChildren().add(x);

       packet.writeXML(session.getWriter());
       Log.trace("Group creation request sent : " + packet);
   }

//   <message
//    from='wiccarocks@shakespeare.lit/laptop'
//    id='lh2bs617'
//    to='coven@chat.shakespeare.lit'
//    type='groupchat'>
//  <subject>Fire Burn and Cauldron Bubble!</subject>
//</message>
   public void ChangeGroupSubject(String GroupName , String NewSubject) throws IOException
   {
    Packet packet = new Packet("message");
    UUID uuid = UUID.randomUUID();
    packet.setAttribute("status", "sent");
    packet.setFrom(this.user+"@"+this.sName);
    packet.setType("groupchat");
    packet.setID(uuid.toString());
    if (GroupName != null){
      packet.setTo(GroupName);
    }
    if (NewSubject != null){
      packet.getChildren().add(new Packet("subject",NewSubject));
    }

    packet.writeXML(session.getWriter());
    Log.trace("Sent : " + packet);
   }

//   <iq from='crone1@shakespeare.lit/desktop'
//    id='member3'
//    to='coven@chat.shakespeare.lit'
//    type='get'>
//  <query xmlns='http://jabber.org/protocol/muc#admin'>
//    <item affiliation='member'/>
//  </query>
//</iq>
   public void getGroupMemberList(String GroupName) throws IOException
   {
       Packet iq = new Packet("iq");
       UUID uuid = UUID.randomUUID();
       iq.setID(uuid.toString());
       iq.setFrom(this.user+"@"+this.sName+"/Android");
       iq.setType("get");
       iq.setTo(GroupName);

       Packet query = new Packet("query");
       query.setAttribute("xmlns", "http://jabber.org/protocol/muc#admin");


       Packet item = new Packet("item");
       item.setAttribute("affiliation", "member");
       item.setParent(query);
       query.setParent(iq);

       iq.writeXML(session.getWriter());
   }
//   <iq from='crone1@shakespeare.lit/desktop'
//    id='admin2'
//    to='coven@chat.shakespeare.lit'
//    type='set'>
//  <query xmlns='http://jabber.org/protocol/muc#admin'>
//    <item affiliation='none'
//          jid='wiccarocks@shakespeare.lit'/>
//  </query>
//</iq>
   public void AdminToMember(String GroupName , String AdminName) throws IOException
   {
       Packet iq = new Packet("iq");
       iq.setFrom(this.user+"@"+this.sName+"/Android");
       iq.setTo(GroupName);
       UUID uuid = UUID.randomUUID();
       iq.setID(uuid.toString());
       iq.setType("set");

       Packet query = new Packet("query");
       query.setAttribute("xmlns", "http://jabber.org/protocol/muc#admin");
       query.setParent(iq);

       Packet item = new Packet("item");
       item.setAttribute("affiliation", "none");
       item.setAttribute("jid", AdminName);
       item.setParent(query);

       iq.writeXML(session.getWriter());
   }
//   <iq from='crone1@shakespeare.lit/desktop'
//    id='admin1'
//    to='coven@chat.shakespeare.lit'
//    type='set'>
//  <query xmlns='http://jabber.org/protocol/muc#admin'>
//    <item affiliation='admin'
//          jid='wiccarocks@shakespeare.lit'/>
//  </query>
//</iq>
   public void MemberToAdmin (String GroupName , String MemberName) throws IOException
   {
       Packet iq = new Packet("iq");
       iq.setFrom(this.user+"@"+this.sName+"/Android");
       iq.setTo(GroupName);
       UUID uuid = UUID.randomUUID();
       iq.setID(uuid.toString());
       iq.setType("set");

       Packet query = new Packet("query");
       query.setAttribute("xmlns", "http://jabber.org/protocol/muc#admin");
       query.setParent(iq);

       Packet item = new Packet("item");
       item.setAttribute("affiliation", "admin");
       item.setAttribute("jid", MemberName);
       item.setParent(query);

       iq.writeXML(session.getWriter());

   }
   // those methods are used to share files between clients of the service
   public boolean FTPfileDownload(String remoteFile1/*the name of the file*/)
   {

        int port = 21; // standard FTP port
        // instatiate a new client from apache commons that has all needed methods to be used here
        FTPClient ftpClient = new FTPClient();
        try {
            //attach a command listener to the printstream so you can log the communication
            // between server and client
            ftpClient.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
            // connect to the server
            ftpClient.connect(FTPsName, port);
            // login
            ftpClient.login(user, password);
            // this is used to change to passive mode and bypass the firewall
            ftpClient.enterLocalPassiveMode();
            // set the File type which is one of two options 1- text 2- binary
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            // first way: using retrieveFile(String, OutputStream)

            //File object to be sent to the output stream  where the retrieved file will be written
            File downloadFile1 = new File("C:\\Users\\Siddiq\\Desktop\\FTP client Home\\"+remoteFile1); // this will be fixed in the server
            // creating buffered file output stream linked to a destination file
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            // use commons net retrieveFile method which takes the remote file and the output stream
            // of the destination to write the file to , returns TRUE if successful
            boolean success = ftpClient.retrieveFile(remoteFile1, outputStream1);
            // make sure to close the output stream
            outputStream1.close();

            if (success) {
                System.out.println("File #1 has been downloaded successfully.");
            }

//            // second way : using InputStream retrieveFileStream(String)
//            String remoteFile2 = "/test/song.mp3";
//            File downloadFile2 = new File("D:/Downloads/song.mp3");
//            OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
//            //using the input stream of the remote file in the server
 //           InputStream inputStream = ftpClient.retrieveFileStream(remoteFile2);
              // write it to byte array buffer manually
//            byte[] bytesArray = new byte[4096];
//            int bytesRead = -1;
//            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                  // when the file is all receieved then write it to the file locally
//                outputStream2.write(bytesArray, 0, bytesRead);
//            }
//            // necessary to complete the pending command after finishing the download
//            success = ftpClient.completePendingCommand();
//            if (success) {
//                System.out.println("File #2 has been downloaded successfully.");
//            }
              // make sure to close the output/input streams
//            outputStream2.close();
//            inputStream.close();

        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            ex.printStackTrace();
            return false ; //unsuccessful download
        } finally {
            try {
                // if the client is connected
                if (ftpClient.isConnected()) {
                    // logout
                    ftpClient.logout();
                    //disconnect from FTP server
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                return true ; // unseccuessful disconnection/log out , it does not affect the file download
            }
       return true;
   }
   }

   String FTPsName ;

    public String getFTPsName() {
        return FTPsName;
    }

    public void setFTPsName(String FTPsName) {
        this.FTPsName = FTPsName;
    }

   // we use this method to download a file from ther server with a given id
   public boolean FTPfileUpload(String localFileFullName , /*String fileName ,*/
   String recipient,String thread,String type,String body ) throws IOException
   {
       UUID FileIDuuid = UUID.randomUUID();
       String FileID = /*FileIDuuid.toString();*/ "ABC";
       // this is a constant in the server and I should program the server to point to only one directory
       String hostDir = "C:\\Users\\Siddiq\\Desktop\\FTP home";
       // create an object of the ftp client from apache commons
       FTPClient ftp = new FTPClient();
       // add listener  to the command and print it to the console
       ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
       int reply;
      try {
          // connect to the server  given the sever name
          ftp.connect(FTPsName);
          // check the reply  code from the server
          reply = ftp.getReplyCode();
          // if the reply code is NOT positive
          if (!FTPReply.isPositiveCompletion(reply)) {
              // disconnect from the server
              ftp.disconnect();
              // and throw new Exception
              throw new Exception("Exception in connecting to FTP Server");
          }
          // here after a successful connection with server you long in with the same user name and password
          ftp.login(user, password);
          // change the type of the file to BINARY which is better , instead of TEXT
          ftp.setFileType(FTP.BINARY_FILE_TYPE);
          // enter local passive mode which helps bypass the firewall blockage
          ftp.enterLocalPassiveMode();
          // create input stream from the local file you want to upload fiven the local file name
          InputStream input = new FileInputStream(new File(localFileFullName));
          //use storeFile method to upload file to the server in the specified host directory with the given file name
          //given the input stream of the file
          // name the file in the sever with a unique identifier so you can communicate
          // it to the reciever and enable him to download the file from the server

          ftp.storeFile(hostDir + FileID, input);

      } catch (FileNotFoundException ex) {
          ex.printStackTrace(); // in case the file was not there
          return false ; // end method with false
      }catch (IOException ex) {
          ex.printStackTrace(); // in case there was a problem in IO operations
          return false; // end method with false
      }catch (Exception ex) {
          ex.printStackTrace(); // any other
          return false ; // end method with false
      }finally{ // anyway
           if (ftp.isConnected()) // if still connected
           {
               try {
                   ftp.logout(); // log out
                   ftp.disconnect(); // and disconnect
               } catch (IOException f) {
                   f.printStackTrace(); // something wrong ?
               }
           }
      }
      // here after a successful file upload you need to send the file id to the reciever
      SendFileTransferMessage(recipient, thread, type, body , FileID);
      return true; // successful file upload + IQ packet to the intended reciever or recievers in case of rooms
   }
//   <message from='stpeter@jabber.org/work'
//         to='MaineBoy@jabber.org/home'>
//  <body>Yeah, but do you have a license to Jabber?</body>
//  <x xmlns='jabber:x:oob'>
//    <url>psa-license.jpg</url>
//    <desc size='put the size here' type='put the type here' length='put length here'/>
//  </x>
//</message>
   public void SendFileTransferMessage(String recipient,String thread,String type,String body , String FileID) throws IOException
   {
    Packet packet = new Packet("message");
    UUID uuid = UUID.randomUUID();
    packet.setAttribute("status", "sent");
    packet.setFrom(this.user+"@"+this.sName);

    if (recipient != null){
      packet.setTo(recipient);
    }

      packet.setID(uuid.toString());

    if (type != null){
      packet.setType(type);
    }
    if (FileID != null){
        Packet x =  new Packet("x");
        x.setAttribute("xmlns", "jabber:x:oob");
        x.getChildren().add(new Packet("url" ,FileID));
        // another packet of description
      packet.getChildren().add(x);
    }
    if (thread != null){
      packet.getChildren().add(new Packet("thread",thread));
    }
    if (body != null){
      packet.getChildren().add(new Packet("body",body));
    }
    packet.writeXML(session.getWriter());
    Log.trace("Sent : " + packet);
   }
   /**
    * according to the SIP protocol we should start a call negotiation with an INVITE request
   */

   long sequence = 0 ;
//   <iq from='romeo@montague.lit/orchard'
//    id='rg6s5134'
//    to='juliet@capulet.lit/balcony'
//    type='set'>
//  <jingle xmlns='urn:xmpp:jingle:1'
//          action='session-initiate'
//          initiator='romeo@montague.lit/orchard'
//          sid='a73sjjvkla37jfea'>
//    <content creator='initiator' name='voice'>
//      <description xmlns='urn:xmpp:jingle:apps:rtp:1' media='audio'>
//        <payload-type id='96' name='speex' clockrate='16000'/>
//        <payload-type id='97' name='speex' clockrate='8000'/>
//        <payload-type id='18' name='G729'/>
//        <payload-type id='103' name='L16' clockrate='16000' channels='2'/>
//        <payload-type id='98' name='x-ISAC' clockrate='8000'/>
//      </description>
//      <transport xmlns='urn:xmpp:jingle:transports:ice-udp:1'
//                 pwd='asd88fgpdd777uzjYhagZg'
//                 ufrag='8hhy'>
//        <candidate component='1'
//                   foundation='1'
//                   generation='0'
//                   id='el0747fg11'
//                   ip='10.0.1.1'
//                   network='1'
//                   port='8998'
//                   priority='2130706431'
//                   protocol='udp'
//                   type='host'/>
//        <candidate component='1'
//                   foundation='2'
//                   generation='0'
//                   id='y3s2b30v3r'
//                   ip='192.0.2.3'
//                   network='1'
//                   port='45664'
//                   priority='1694498815'
//                   protocol='udp'
//                   rel-addr='10.0.1.1'
//                   rel-port='8998'
//                   type='srflx'/>
//      </transport>
//    </content>
//  </jingle>
//</iq>
   public boolean makeCall(String callee) // call someone
   {


       return true;
   }
//   <iq from='juliet@capulet.lit/balcony'
//    id='wps8b597'
//    to='romeo@montague.lit/orchard'
//    type='set'>
//  <jingle xmlns='urn:xmpp:jingle:1'
//          action='session-terminate'
//          initiator='romeo@montague.lit/orchard'
//          sid='a73sjjvkla37jfea'>
//    <reason>
//      <success/>
//      <text>Sorry, gotta go!</text>
//    </reason>
//  </jingle>
//</iq>
   public boolean hangupCall() // end the current call
   {
       return false;
   }

//   <iq from='juliet@capulet.lit/balcony'
//    id='lj3bf87g'
//    to='romeo@montague.lit/orchard'
//    type='set'>
//  <jingle xmlns='urn:xmpp:jingle:1'
//          action='session-accept'
//          initiator='romeo@montague.lit/orchard'
//          responder='juliet@capulet.lit/balcony'
//          sid='a73sjjvkla37jfea'>
//    <content creator='initiator' name='voice'>
//      <description xmlns='urn:xmpp:jingle:apps:rtp:1' media='audio'>
//        <payload-type id='97' name='speex' clockrate='8000'/>
//        <payload-type id='18' name='G729'/>
//      </description>
//      <transport xmlns='urn:xmpp:jingle:transports:ice-udp:1'
//                 pwd='YH75Fviy6338Vbrhrlp8Yh'
//                 ufrag='9uB6'>
//        <candidate component='1'
//                   foundation='1'
//                   generation='0'
//                   id='or2ii2syr1'
//                   ip='192.0.2.1'
//                   network='0'
//                   port='3478'
//                   priority='2130706431'
//                   protocol='udp'
//                   type='host'/>
//      </transport>
//    </content>
//  </jingle>
//</iq>
   public boolean pickupCall() // pick up the ringing call
   {
       return false;
   }

   public boolean declineCall() // reject the call before answering it
   {
       return false;
   }
//   <iq from='juliet@capulet.lit/balcony'
//    id='xv39z423'
//    to='romeo@montague.lit/orchard'
//    type='set'>
//  <jingle xmlns='urn:xmpp:jingle:1'
//          action='session-info'
//          initiator='romeo@montague.lit/orchard'
//          sid='a73sjjvkla37jfea'>
//    <hold xmlns='urn:xmpp:jingle:apps:rtp:info:1'/>
//  </jingle>
//</iq>
    public boolean holdCall() // hold the call
   {
       return false;
   }
//    <iq from='juliet@capulet.lit/balcony'
//    id='br81gd63'
//    to='romeo@montague.lit/orchard'
//    type='set'>
//  <jingle xmlns='urn:xmpp:jingle:1'
//          action='session-info'
//          initiator='romeo@montague.lit/orchard'
//          sid='a73sjjvkla37jfea'>
//    <unhold xmlns='urn:xmpp:jingle:apps:rtp:info:1'/>
//  </jingle>
//</iq>
     public boolean unholdCall() // unhold the call
   {
       return false;
   }

   public boolean cancelCall() // end the current call before the callee picks up
   {

       return false;
   }
//   <iq from='juliet@capulet.lit/balcony'
//    id='hg4891f5'
//    to='romeo@montague.lit/orchard'
//    type='set'>
//  <jingle xmlns='urn:xmpp:jingle:1'
//          action='session-info'
//          initiator='romeo@montague.lit/orchard'
//          sid='a73sjjvkla37jfea'>
//    <mute xmlns='urn:xmpp:jingle:apps:rtp:info:1'
//          creator='responder'
//          name='voice'/>
//  </jingle>
//</iq>
   public boolean mute() // to mute the call
   {
       // let Arwa work on this device specific
       return false;
   }
//   <iq from='juliet@capulet.lit/balcony'
//    id='ms91g47c'
//    to='romeo@montague.lit/orchard'
//    type='set'>
//  <jingle xmlns='urn:xmpp:jingle:1'
//          action='session-info'
//          initiator='romeo@montague.lit/orchard'
//          sid='a73sjjvkla37jfea'>
//    <unmute xmlns='urn:xmpp:jingle:apps:rtp:info:1'
//            creator='responder'
//            name='voice'/>
//  </jingle>
//</iq>
   public boolean unmute() // to unmute the call
   {
       return false;
   }

   public boolean switchToLoudSpeakers() // to loud speakers
   {
       // let Arwa work on this device specific
       return false ;
   }

   public boolean switchToNormalSpeakers() // to normal speakers
   {
       // let Arwa work on this device specific
       return false ;
   }

   public boolean switchToBackCamera() // switch between to back camera
   {
       // let Arwa work on this device specific
       return false ;
   }

   public boolean switchToFrontCamera() // switch between to front camera
   {
       // let Arwa work on this device specific
       return false ;
   }

        // return ip address for which u need to do port forwarding
    private String getYourIp(String defaultAddress) {

        String temp = defaultAddress.substring(0, 11);
        String ipToForward = "";

        TreeSet<String> ipAddrs = getIpAddressList();
        for (Iterator<String> iterator = ipAddrs.iterator(); iterator.hasNext();) {

            String tempIp = iterator.next();
            if (tempIp.contains(temp)) {
                ipToForward = tempIp;
                break;
            }
        }

        return ipToForward;

    }// ipForPortForwarding


        // get the ipaddress list
    private TreeSet<String> getIpAddressList() {
        TreeSet<String> ipAddrs = new TreeSet<String>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                // filters out 127.0.0.1 and inactive interfaces
                if (iface.isLoopback() || !iface.isUp())
                    continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {

                    InetAddress addr = addresses.nextElement();

                    ipAddrs.add(addr.getHostAddress());

                }// 2 nd while
            }// 1 st while
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return ipAddrs;

    }// getIpAddressList

     // get default gateway address in java

    private String getDefaultGateWayAddress() {
        String defaultAddress = "";
        try {
            Process result = Runtime.getRuntime().exec("netstat -rn");

            BufferedReader output = new BufferedReader(new InputStreamReader(
                    result.getInputStream()));

            String line = output.readLine();
            while (line != null) {
                if (line.contains("0.0.0.0")) {

                    StringTokenizer stringTokenizer = new StringTokenizer(line);
                    stringTokenizer.nextElement();// first string is 0.0.0.0
                    stringTokenizer.nextElement();// second string is 0.0.0.0
                    stringTokenizer.nextElement();// second string is on-link
                    defaultAddress = (String) stringTokenizer.nextElement(); // this is our default address
                    break;
                }

                line = output.readLine();

            }// while
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return defaultAddress;

    }// getDefaultAddress

    void sendMessage(String localhost) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

   

}

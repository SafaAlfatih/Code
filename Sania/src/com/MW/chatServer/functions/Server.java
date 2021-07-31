package com.MW.chatServer.functions;

import com.MW.chatServer.jabber.xml.QueueThread;
import com.MW.chatServer.jabber.xml.PacketQueue;
import java.io.IOException;
import java.net.Socket;
import java.net.ServerSocket;

import com.MW.chatServer.jabber.Authenticator;
import com.MW.chatServer.jabber.Session;
import com.MW.chatServer.jabber.xml.ProcessThread;

/**
The main MW Jabber server class.

We listen for a connection, open a Jabber XML stream, watch for their stream,
then close down the connection.

@author Siddiq
*/
public class Server {

  final static public int    JABBER_PORT = 5222;
  final static public String SERVER_NAME = "localhost";

  // Create the global data structures everyone works with
  PacketQueue packetQueue = new PacketQueue();
  UserIndex index = new UserIndex();

    public UserIndex getIndex() {
        return index;
    }
  
  

  protected Server(){
      
     
    createQueueThread();

    ServerSocket serverSocket;

    try {
      // Create the server socket to listen to
      serverSocket = new ServerSocket(JABBER_PORT);
    } catch (IOException ex){
      // This is likely an very bad problem...
      // ... probably can't use the port...
      // ... report the problem and quit
      ex.printStackTrace();
      return;
    }

    // We accept incoming sockets and start new threads forever
    while (true){
      try {
        // Wait for the connection
        Socket newSock = serverSocket.accept();
        Session session = new Session(newSock);

        // Spin off a new thread to handle the connection
        ProcessThread processor = new ProcessThread(packetQueue,session);
        processor.start();
      } catch (IOException ie){
        ie.printStackTrace();
      }
    }
  }

  void createQueueThread(){
      // Create the Queue worker thread to process packets in the packet queue
    QueueThread qThread = new QueueThread(packetQueue);
    qThread.setDaemon(true);
    qThread.addListener(new OpenStreamHandler(index),"stream:stream");
    qThread.addListener(new CloseStreamHandler(index),"/stream:stream");
    qThread.addListener(new MessageHandler(index),"message");
    qThread.addListener(new PresenceHandler(index),"presence");
    qThread.addListener(new RegisterHandler(index),"jabber:iq:register");
    qThread.addListener(new AuthHandler(index),"jabber:iq:auth");
    qThread.addListener(new RosterHandler(index),"jabber:iq:roster");
    qThread.addListener(new IQHandler(index),"http://jabber.org/protocol/muc#admin");
    qThread.addListener(new OutOfBandHandler(index), "jabber:iq:oob");
    qThread.addListener(new JingleHandler(index), "urn:xmpp:jingle:1");
    qThread.start();
  }

  static public void main(String [] args){
    System.out.print("Initializing authenticator...");
    Authenticator.randomToken();
    System.out.println(" initialized.");
    System.out.println("MW PayChat Server -- " + SERVER_NAME + ":"+ JABBER_PORT);
   new Server();
  }
}

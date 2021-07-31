package ClientApplicationCore;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Siddiq M.Hassan
 */
public class SimpleMessageClient {
    
    
    // creat Client Object
    public static void main(String[] args){
        

Scanner scan = new Scanner(System.in);
String server = System.getProperty("jab.server.name", "localhost");
String address = System.getProperty("jab.server.address","127.0.0.1");
String port = System.getProperty("jab.server.port", "5222");
String auth = System.getProperty("jab.server.auth","plain");
// Extract settings from System properties


// Create the Main worker thread to process packets in the packet queue
    QueueThread MainWorker = new QueueThread();
    JabberModel ClientModel = new JabberModel(MainWorker);
    
ClientModel.setServerName(server);
ClientModel.setServerAddress(address);
ClientModel.setPort(port);

boolean more = true ;
while(more){
System.err.println("1/register a new user  2/log in   3/disconnect \n4/go offline  "+
        "5/add a friend 6/send message 7/create group 8/add member 9/send Group Message 10/Exit the group\n11/Change group subject 12/remove member from group 13/get all Members \n"
        + "14/ send File     15/downloadFile  16/make a call\n 17/pick up    18/decline   19/cancel  20/hang up    21/remove user\n"
        + "22/ block user       23/unblock user");
String inputs = scan.next();
int choiceNumber = Integer.parseInt(inputs);
        switch (choiceNumber) {
            case 1:
            System.out.println(" Registering : Enter user and password in this format user/password :");
            String A = scan.next();
            String user = A.substring(0, A.indexOf('/'));
            String pass = A.substring(A.indexOf('/')+1);
            String resource = "Android";
            
            
    try {
        ClientModel.setUser(user);
        ClientModel.setAuthMode(auth);
        ClientModel.setResource(resource);
        ClientModel.setPassword(pass);
        ClientModel.connect();
        
        ClientModel.registerPlain();
    } catch (IOException ex) {
        System.err.println("Something went wrong with the registeration step .");
    }
            
        break ;
            case 2 : 
                 System.out.println(" Loging : Enter user and password in this format user/password :");
                 String B = scan.next();
                 user = B.substring(0, B.indexOf('/'));
                 pass = B.substring(B.indexOf('/')+1);
                 ClientModel.setUser(user);
                 ClientModel.setPassword(pass);
                 ClientModel.setAuthMode(auth);
                 ClientModel.setResource("Android");  
try {
    ClientModel.connect();
    ClientModel.authenticatePlain();
    ClientModel.sendPresence(null, null, null, null, null);
} catch (IOException ex) {
System.err.println("Something went wrong with the login step .");
} catch (Exception ex){
    System.err.println(ex.getMessage());
}
                
  break ;
            case 3: try {
                ClientModel.disconnect();
} catch (IOException ex) {
System.err.println("Something went wrong with the disconnection step .");
}
break ;
            case 4 : 
try {
    ClientModel.sendPresence(null, "unavailable", null, null, null);
} catch (IOException ex) {
    System.err.println("Something went wrong with the presence update step .");
}
                break;
            case 5 :
                 System.out.println(" Adding Friends : Enter Friend's name:");
                 String C = scan.next();
                 try {
                          ClientModel.sendRosterSet(C, "omer", null);
                     } catch (IOException ex) {
                        System.err.println("Something went wrong with friend addition step .");                     }
                break;
            case 6 : 
                System.out.println(" Messaging : Enter Friend's name ");
                Scanner scan2 = new Scanner(System.in);
            String friend = scan2.nextLine();
            String message = "Greeting my fellow Devil";
try {
    ClientModel.sendMessage(friend, "normal", message);
} catch (IOException ex) {
    System.out.println(" something went wrong with the message");
} catch (NumberFormatException ex) {
    System.out.println(" something went wrong with the message");
}
                 
                break ;
            case 7 : System.out.println("Enter the name of the group :");
               String groupName = "assholes";
try {
    ClientModel.createGroup(groupName);
} catch (IOException ex) {
    System.out.println(" something went wrong with group creation");
}
                break ;
            case 8 :System.out.println("adding omer@localhost to the group memebers :");
try {
    ClientModel.addMember("assholes.group@localhost", "0922233401@localhost/Android", "0922233171@localhost/Android");
} catch (IOException ex) {
    System.out.println(" something went wrong with adding members");
}
                break;
            case 9: try {
                ClientModel.SendGroupMessage("assholes.group@localhost", null, "gr1", "I am sending to the group");
} catch (IOException ex) {
    System.err.println("Something went wrong with group message");
}
                break;
            case 10 : 
try {
    ClientModel.LeaveGroup("assholes");
} catch (IOException ex) {
    System.err.println("Something went wrong with leaving the group");
}
                break;
            case 11 : try {
                ClientModel.ChangeGroupSubject("assholes.group@localhost", "Awesome people.group@localhost");
} catch (IOException ex) {
    System.err.println("Something went wrong with subject change");
}
                break;
            case 12 : try {
                ClientModel.RemoveMember("assholes.group@localhost", "0922233401@localhost/Android", "0922233171@localhost/Android");
} catch (IOException ex) {
   System.err.println("something went wrong with member removal");
}
                break;
            case 13: try {
                ClientModel.getGroupMemberList("assholes.group@localhost");
} catch (IOException ex) {
    System.err.println("something went wrong with member list retrieval");
}
                break;
            case 14: 
try {
    boolean fileUploadResult = ClientModel.FTPfileUpload("C:\\Users\\Siddiq\\Desktop\\My UTM PDF\\Passport.pdf", "0922233171@localhost/Android", null, "normal", null);
    if(!fileUploadResult) System.err.print("File not sent");
} catch (IOException ex) {
    System.err.println("something went wrong with File transfer");
}
                break;
            case 15: 
                boolean fileUploadResult = ClientModel.FTPfileDownload("ABC");
    if(!fileUploadResult) System.err.print("File not sent");
                break;
            case 20 : more = false ; break;
                
            case 21:
                  System.out.println(" Roster : Enter Friend's name ");
                Scanner scan3 = new Scanner(System.in);
            String friend2 = scan3.nextLine();
            
{
    try {
        ClientModel.sendRosterRemove(friend2+"@localhost");
    } catch (IOException ex) {
      System.out.println("something went wrong with the removal");
    }
}
            case 22:
                  System.out.println(" Roster : Enter Friend's name ");
                Scanner scan4 = new Scanner(System.in);
            String friend3 = scan4.nextLine();
            
{
    try {
        ClientModel.sendRosterBlock(friend3+"@localhost");
    } catch (IOException ex) {
      System.out.println("something went wrong with the block");
    }
}
             case 23:
                  System.out.println(" Roster : Enter Friend's name ");
                Scanner scan5 = new Scanner(System.in);
            String friend4 = scan5.nextLine();
            
{
    try {
        ClientModel.sendRosterUnblock(friend4+"@localhost");
    } catch (IOException ex) {
      System.out.println("something went wrong with the Unblock");
    }
}
            default : System.err.println("Wrong option number .");
}
    }
    
    }
   
}




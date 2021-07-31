package ClientApplicationCore;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * 
 * @author Siddiq M.Hassan
 */
public class SafaClient {
    
    
    // creat Client Object
    public static void main(String[] args){
        

        try {
            
            String server = System.getProperty("jab.server.name", "localhost");
            String address = System.getProperty("jab.server.address","192.168.8.100");
            String port = System.getProperty("jab.server.port", "5222");
            String auth = System.getProperty("jab.server.auth","plain");
// Extract settings from System properties
           // 192.168.8.100
           // 127.0.0.1
            //192.168.8.104
            //192.168.43.12
            //10000000011640
// Create the Main worker thread to process packets in the packet queue
            QueueThread MainWorker = new QueueThread();
            JabberModel ClientModel = new JabberModel(MainWorker);
            
            ClientModel.setServerName(server);
            ClientModel.setServerAddress(address);
            ClientModel.setPort(port);
            ClientModel.setUser("11111111111111");
            ClientModel.setPassword("123");
            ClientModel.setAuthMode(auth);
            ClientModel.setResource("Android");
            
            
           
                
            
            
         
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            
            Scanner scan = new Scanner(System.in);
            
            ClientModel.connect();
ClientModel.registerPlain();
 ////ClientModel.authenticatePlain();
 //ClientModel. sendRosterRemove("99999999999999@localhost") ;
   // ClientModel.sendRosterBlock("99999999999999@localhost");
    //ClientModel.sendRosterUnblock("11111111111111@localhost");
       //ClientModel.sendRosterGet();
             //ClientModel.sendPresence("77777777777777@localhost","available","chat","I'm bored out of my mind, talk to me","10");
  //ClientModel.createGroup("chat");
//ClientModel.SendGroupMessage("chat","","","HI");
//ClientModel.addMember("chat" ,"10000000011640", "66677788899921");
 //ClientModel.RemoveMember("chat", "10000000011640" , "66677788899921");
// ClientModel.LeaveGroup("chat");
// ClientModel.ChangeGroupSubject("chat" , "MW CO.LTD");
 // [group name].group@[server name]/[nickname]
// ClientModel. getGroupMemberList("chat");
 //ClientModel.AdminToMember("chat" , "10000000011640");
 //ClientModel.MemberToAdmin ("MW CO.LTD" , "66677788899921");
 
   //ClientModel.sendMessage("11111111111111@localhost","normal","hi I am Safa");
 
 
 
 
 
    
   // (String recipient,String subject,String thread,String type,String id,String body)
       // System.out.println(" Adding Friends : Enter Friend's name:");
                  
    //String C = scan.next();
           //  try {
                       //ClientModel.sendRosterSet(C, "", null);
                   // } 
                //catch (IOException ex) {
                   // System.err.println("Something went wrong with friend addition step .");         
               // }
                
        // ClientModel.disconnect();
            
            
            
            
            
            
            
            
            
        }
        
        
        catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
     
    }
   
}




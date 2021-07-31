package com.MW.chatServer.Database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.MW.chatServer.functions.User;
import com.MW.chatServer.jabber.xml.Packet;
import com.MW.chatServer.log.Log;
import com.timesten.jdbc.TimesTenConnection;
import com.timesten.jdbc.TimesTenDataSource;





/**
 
/**
 * @author Siddiq Hamed
 * @version 1.0
 * @since Copyright 2017
 * @company MW modern Tech
 * @Title ChatDataBase
 * @description it handles all the database operations
 *
 */
public class ChatDatabase {
private static TimesTenConnection ttcon;
    
    public static void main(String args[])
    {
        try {
            // create the TimesTen data source and set the connection URL
            TimesTenDataSource ttds = new TimesTenDataSource();
            ttds.setUrl("jdbc:timesten:direct:dsn=paychatdsn;UID=test;PWD=123");
            
            // connect to the TimesTen database
             ttcon = (TimesTenConnection) ttds.getConnection();
            
             ChatDatabase db = new ChatDatabase();
          
         //    Packet message = new Packet("message");
         //    message.setAttribute("from", "00249922233171");
         //    message.setType("normal");
         //    message.setAttribute("to", "00249922233401");
        //     message.getChildren().add(new Packet("body" , "I am testing the data base"));
             
             
         //    boolean storeOperation = db.storeMessage(message);
           //  boolean newUser = db.saveUser("00249922233401", "google.com", "123", "hash", "token", "sequence");
         //    User user = new User("00249922233401");
         //    List<Message> retrieved = db.getMessages(user);
             
            String group_id = db.saveGroup("54353453", "MW team");
          //   boolean removeGroup = db.removeGroup("2");
          //   boolean addMember = db.addMemberToGroup("3", "234234");
          //   boolean removeMember = db.removeMemberFromGroup("3", "54353453");
          //   boolean makeRegMem = db.makeRegularMemberOfGroup("2", "54353453");
          //   boolean makeAdmin = db.makeAdminOfGroup("2", "54353453");
          //   boolean addtoRoster = db.addToRoster("922233401", "922233171");
          //   boolean removeRoster = db.removeFromRoster("922233401", "922233171");
          //   boolean blockUser = db.blockUser("922233401", "922233171");
          //   boolean blockUser = db.unBlockUser("922233401", "922233171");
         //    boolean chngSub = db.updateGroupSubject("1", "safa group");
             
             
             
         //    boolean deleteOperation = db.deleteMessage("11", "00249922233401");
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        
    }
    
    public boolean deleteMessage(String msg_id , String from)
    {
    	boolean success = false ;
    	Log.info("Database is deleting a message from "+from);
        
        try {
            String deleteMsgWithID = "delete from admin.messages where msg_id=?";
            PreparedStatement preparedStatement = ttcon.prepareStatement(deleteMsgWithID);
            preparedStatement.setString(1, msg_id);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully deleted message from "+from);
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    }
    
    public boolean storeMessage(Packet message)
    {

    	Log.info("Database is saving message to " + message.getTo()+" from "+message.getFrom());
        boolean success = false ;
        try {
            String insertNewMessage = "INSERT INTO admin.messages VALUES (admin.messages_sequence.NEXTVAL,?,?,?,?)";
            PreparedStatement preparedStatement = ttcon.prepareStatement(insertNewMessage);
            String body = message.getFirstChild("body").getValue();
            String from = message.getFrom();
            String type = message.getType();
            String to   = message.getTo(); 
            preparedStatement.setString(1, to);
            preparedStatement.setString(2, from);
            preparedStatement.setString(3, body);
            preparedStatement.setString(4, type);
            System.out.print("");
            preparedStatement .executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully stored message from "+from);
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());
            
        }
        return success; //success ;
    }
    
    public List getMessages(User user)
    {
    	List<Message> messages = new ArrayList<Message>();
    	List<String> deletedItems = new ArrayList<String>();
    	
    	String phone_number = user.getPhone_number();
    	  try {
              String getUserSQL= "SELECT  msg_id , to_user , from_user , body , type from admin.messages  where to_user = ?";
              PreparedStatement preparedStatement = ttcon.prepareStatement(getUserSQL);
              preparedStatement.setString(1, phone_number); // don't use the unbound SQL again
              ResultSet rs = preparedStatement.executeQuery();
              Message msg ;
              String deletedID = "";
              
              while (rs.next()) {
              	           msg = new Message(); 
              	            msg.setBody(rs.getString("body"));
              	            msg.setTo(rs.getString("to_user"));
              	            msg.setFrom(rs.getString("from_user"));
  	                        msg.setMsg_id(rs.getString("msg_id")); 
                            msg.setType(rs.getString("type"));
                            deletedID = rs.getString("msg_id");
                            messages.add(msg);
                            Log.info("Retrieved : "+msg.toString());
                            deletedItems.add(rs.getString("msg_id"));
                             }
              
          } catch (SQLException ex) {
               Log.error("Database : ", ex);
          }
    	  
    	  // then delete all retrieved messages
    	  Iterator<String> iterator = deletedItems.iterator();
    		while (iterator.hasNext()) {
    			
    			deleteMessage(iterator.next(),phone_number);
    			
    		}
      
    	
    	return messages;
    }
   
    // this method is used to get a user from the database -- Users table
    public User  getUser(String phone)
    {
        User user = null;
        try {
            String getUserSQL= "SELECT  user_id , phone_number , avatar_link , password , hash , token , sequence from admin.users  where phone_number = ?";
            PreparedStatement preparedStatement = ttcon.prepareStatement(getUserSQL);
            preparedStatement.setString(1, phone); // don't use the unbound SQL again
            ResultSet rs = preparedStatement.executeQuery();
            user = new User(phone);
            while (rs.next()) {
            	            
            	            user.setPassword(rs.getString("password"));
            	            user.setAvatar_link(rs.getString("avatar_link"));
	                        user.setHash(rs.getString("hash")); 
                            user.setSequence(rs.getString("sequence"));
                            user.setToken(rs.getString("token")); 
                           }
            
        } catch (SQLException ex) {
             Log.error("Database : ", ex);
        }
        return user ;
    }
    
    public boolean saveUser(String phonenumber , String avatarlink , String password , String hash , String token , String sequence)
    {
    	Log.info("Database is saving user : "+phonenumber);
        boolean success = false ;
        try {
            
            
            String insertNewUser = "INSERT INTO admin.users VALUES (admin.users_sequence.NEXTVAL,?,?,?,?,?,?)";
            PreparedStatement preparedStatement = ttcon.prepareStatement(insertNewUser);
            preparedStatement.setString(1, phonenumber);
            preparedStatement.setString(2, avatarlink);
            preparedStatement.setString(3, password);
            preparedStatement.setString(4, hash);
            preparedStatement.setString(5, token);
            preparedStatement.setString(6, sequence);
            preparedStatement .executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully saved user "+phonenumber);
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());
            
        }
        return success; //success ;
    }
    
    public boolean removeUser(String phone)
    {
    	boolean success = false ;
    	Log.info("Database is removing a user from Paychat System");
        
        try {
            String deleteMsgWithID = "delete from admin.users where phone_number=?";
            PreparedStatement preparedStatement = ttcon.prepareStatement(deleteMsgWithID);
            preparedStatement.setString(1, phone);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully removed user "+phone+" from Paychat system");
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    
        
    }

    public boolean addToRoster(String RosterOwnerUser , String addedToRosterUserJID)
    {

    	boolean success = false ;
    	Log.info("Database is adding the user "+addedToRosterUserJID+" to "+RosterOwnerUser+" roster");
        
        try {
            String addRosterItemwithJID = "INSERT INTO ADMIN.ROSTERS (roster_id , roster_owner , phone_number)VALUES" + 
            		"(admin.roster_sequence.NEXTVAL, ?, ?)";
            PreparedStatement preparedStatement = ttcon.prepareStatement(addRosterItemwithJID);
            preparedStatement.setString(1, RosterOwnerUser);
            preparedStatement.setString(2, addedToRosterUserJID);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully added Roster item from "+RosterOwnerUser);
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    }
    
    public boolean removeFromRoster(String RosterOwnerUser , String removedFromRosterUserJID)
    {

    	boolean success = false ;
    	Log.info("Database is deleting the user "+removedFromRosterUserJID+" from "+RosterOwnerUser+" roster");
        
        try {
            String deleteMsgWithID = "DELETE FROM admin.rosters where roster_owner=? and phone_number=?";
            PreparedStatement preparedStatement = ttcon.prepareStatement(deleteMsgWithID);
            preparedStatement.setString(1, RosterOwnerUser);
            preparedStatement.setString(2, removedFromRosterUserJID);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully deleted Roster item from "+RosterOwnerUser);
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    
    }
    
    public boolean blockUser(String initiatorofBlock , String bockedUser)
    {

    	boolean success = false ;
    	Log.info("Database is blocking the user "+bockedUser+" from "+initiatorofBlock+" roster");
        
        try {
            String blockWithID = "UPDATE ADMIN.ROSTERS SET ISBLOCKED = '1' where roster_owner=? and phone_number=?";
            PreparedStatement preparedStatement = ttcon.prepareStatement(blockWithID);
            preparedStatement.setString(1, initiatorofBlock);
            preparedStatement.setString(2, bockedUser);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully blocked Roster item from "+initiatorofBlock);
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    
    	
    }
    
    public boolean unBlockUser(String initiatorofunBlock , String unbockedUser)
    {

    	boolean success = false ;
    	Log.info("Database is unblocking the user "+unbockedUser+" from "+initiatorofunBlock+" roster");
        
        try {
            String blockWithID = "UPDATE ADMIN.ROSTERS SET ISBLOCKED = '0' where roster_owner=? and phone_number=?";
            PreparedStatement preparedStatement = ttcon.prepareStatement(blockWithID);
            preparedStatement.setString(1, initiatorofunBlock);
            preparedStatement.setString(2, unbockedUser);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully unblocked Roster item from "+initiatorofunBlock);
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    
    	
    }
    
    public String saveGroup(String phone , String subject)
    {

    	
    	Log.info("Database is creating new Group with subject ["+subject+"]");
        String group_id = "";
        try {
        	// first we get the next group ID from the sequence
            String getNxtVal= "SELECT admin.group_sequence.nextval as \"id\" from dual";
            PreparedStatement preparedStatement = ttcon.prepareStatement(getNxtVal);
            ResultSet rs = preparedStatement.executeQuery();
           // ttcon.commit();
            while (rs.next()) {
            	group_id = rs.getString("id");
            }
            
        } catch (SQLException ex) {
             Log.error("Database : ", ex);
        }
        
        try {
        	// second create the group
            String addGroupWithNewID = "INSERT INTO ADMIN.groups (group_id , subject)VALUES" + 
            		"(?,?)";
            PreparedStatement grouppreparedStatement = ttcon.prepareStatement(addGroupWithNewID);
            grouppreparedStatement.setString(1, group_id);
            grouppreparedStatement.setString(2, subject);
            grouppreparedStatement.executeUpdate();
            ttcon.commit();
            Log.info("Database handler successfully created new group with subject"+subject);
            
            
            
            // third we add the admin as member with administrator 
            String insertAdminforGroupAbove = "INSERT INTO ADMIN.members (member_id , group_id , isadmin , phone_number)VALUES" + 
            		"(admin.members_sequence.NEXTVAL,?,?,?)";
            PreparedStatement adminpreparedStatement = ttcon.prepareStatement(insertAdminforGroupAbove);
            adminpreparedStatement.setString(1, group_id);
            adminpreparedStatement.setString(2, "1");
            adminpreparedStatement.setString(3, phone);
            adminpreparedStatement.executeUpdate();
            ttcon.commit();
            Log.info("Database handler successfully added admin "+phone+" to group "+group_id+" with subject "+subject);
            
            
        }
        catch (SQLException ex) {
           
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return group_id;

    }

    public boolean removeGroup(String group_id)
    {
    	boolean success = false ;
    	Log.info("Database is deleting the group "+group_id);
        
        try {
            String deleteGroupWithID = "DELETE FROM admin.groups where group_id=?";
            PreparedStatement preparedStatement = ttcon.prepareStatement(deleteGroupWithID);
            preparedStatement.setString(1, group_id);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully deleted group "+group_id);
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    }
    
    public boolean addMemberToGroup(String group_id , String phone_number)
    {

    	boolean success = false ;
    	Log.info("Database is adding the member "+phone_number+" to group "+group_id);
        
        try {
            String addMemberToGroup = "INSERT INTO ADMIN.members (member_id , group_id , isadmin , phone_number)VALUES" + 
            		"(admin.members_sequence.NEXTVAL, ?, ? ,?)";
            PreparedStatement preparedStatement = ttcon.prepareStatement(addMemberToGroup);
            preparedStatement.setString(1, group_id);
            preparedStatement.setString(2, "0");
            preparedStatement.setString(3, phone_number);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully added member "+phone_number+" to group "+group_id);
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    }
    
    public boolean updateGroupSubject(String groupID , String subject)
    {

    	boolean success = false ;
    	Log.info("Database is updating the subject of group "+groupID+" to ["+subject+"]");
        
        try {
            String updateGroupSubject = "UPDATE ADMIN.groups SET subject =? where group_id=?";
            PreparedStatement preparedStatement = ttcon.prepareStatement(updateGroupSubject);
            preparedStatement.setString(1, subject);
            preparedStatement.setString(2, groupID);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully updated subject");
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    }
    
    public boolean removeMemberFromGroup(String groupID , String phone)
    {

    	boolean success = false ;
    	Log.info("Database is removing the member "+phone+" from group "+groupID);
        
        try {
            String removeMemberFromGroup = "DELETE FROM admin.members where group_id=? and phone_number=?";
            PreparedStatement preparedStatement = ttcon.prepareStatement(removeMemberFromGroup);
            preparedStatement.setString(1, groupID);
            preparedStatement.setString(2, phone);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully removed user "+phone+" from group "+groupID);
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    }

    public boolean makeAdminOfGroup(String groupID , String phone)
    {
    	boolean success = false ;
    	Log.info("Database is making user "+phone+"an admin in the group "+groupID);
        
        try {
            String updateGroupSubject = "UPDATE ADMIN.members SET isadmin =? where group_id=? and phone_number=?";
            PreparedStatement preparedStatement = ttcon.prepareStatement(updateGroupSubject);
            preparedStatement.setString(1, "1");
            preparedStatement.setString(2, groupID);
            preparedStatement.setString(3, phone);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully made "+phone+" an admin");
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    }
    
    public boolean makeRegularMemberOfGroup(String groupID , String phone)
    {
    	boolean success = false ;
    	Log.info("Database is making admin "+phone+" a regular user in the group "+groupID);
        
        try {
            String updateGroupSubject = "UPDATE ADMIN.members SET isadmin =? where group_id=? and phone_number=?";
            PreparedStatement preparedStatement = ttcon.prepareStatement(updateGroupSubject);
            preparedStatement.setString(1, "0");
            preparedStatement.setString(2, groupID);
            preparedStatement.setString(3, phone);
            preparedStatement.executeUpdate();
            ttcon.commit();
            success = true ;
            Log.info("Database handler successfully made "+phone+" a regular member");
            return success ;
        }
        catch (SQLException ex) {
            success = false ;
            Log.info("Chat Database Exception : "+ex.getMessage());    
        }
    	return success ;
    }
 
}


 class Message {
	String msg_id ;
	String body ;
	String from ;
	String type ;
	String to ;

	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getMsg_id() {
		return msg_id;
	}
	public void setMsg_id(String msg_id) {
		this.msg_id = msg_id;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	public String toString()
	{
		return "Message : ["+msg_id+" , "+from+" , "+type+" , "+body+"]";
	}

}


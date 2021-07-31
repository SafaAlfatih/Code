package com.MW.chatServer.functions;

import com.MW.chatServer.jabber.JabberID;
import com.MW.chatServer.jabber.Session;
import com.MW.chatServer.jabber.xml.PacketListener;
import com.MW.chatServer.log.Log;
import com.MW.chatServer.functions.GroupChatManager.Group;
import com.MW.chatServer.jabber.xml.Packet;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2016
 * Company:
 * @author : Siddiq
 * @version 1.0
 */

public class IQHandler implements PacketListener {

  UserIndex userIndex;
  GroupChatManager manager ;
  public IQHandler(UserIndex index) { 
      userIndex = index;
      manager = GroupChatManager.getManager();
  }

  public void notify(Packet packet){
    Log.trace("Delivering packet: " + packet.toString());
    String recipient = packet.getAttribute("to");
    if (recipient.equalsIgnoreCase(Server.SERVER_NAME) || recipient == null){
      Log.trace("Dropping packets for server: " + packet.toString());
      return;
    }
    
    Group group = manager.getGroup(packet.getTo());
    if(packet.getType().equalsIgnoreCase("set")){
      boolean found = group.isAdmin(new JabberID(packet.getFrom()));
      if (found)
      {
              Packet query = packet.getFirstChild("query");
              Packet item = query.getFirstChild("item");
              String affiliation = item.getAttribute("affiliation");
              JabberID itemJID = new JabberID(item.getAttribute("jid"));
              if(affiliation.equalsIgnoreCase("member"))
              { 
                  // check if this member exists
                  JabberID alreadyHere = (JabberID) group.jid2nick.get(item.getAttribute("jid"));
                  
                  if(alreadyHere != null)
                  {
                      Iterator itr = group.listOfAdmins.iterator();
        JabberID jid ;
       while(itr.hasNext())
        {
            jid = (JabberID) itr.next();
            if((jid.getUser()).equals(alreadyHere.getUser()))
            {
                // remove him from the admin list 
                group.listOfAdmins.remove(alreadyHere.getUser());
                break;
            }
            
        }
        // reflect the result back to the admin
                  Packet iq = new Packet("iq");
                  iq.setID(packet.getID());
                  iq.setTo(packet.getFrom());
                  iq.setType("result");
                  MessageHandler.deliverPacket(iq);
       // send a presence to tell members that admin lost his position
//<presence
//    from='coven@chat.shakespeare.lit/secondwitch'
//    to='crone1@shakespeare.lit/desktop'>
//  <x xmlns='http://jabber.org/protocol/muc#user'>
//    <item affiliation='none'
//          jid='wiccarocks@shakespeare.lit'
//          role='participant'/>
//  </x>
//</presence>
                  Packet presence = new Packet("presence");
                  presence.setFrom(group.jid+"/"+packet.getFrom());
                  presence.setID(packet.getID());
                  
                  Packet x = new Packet("x");
                  x.setAttribute("xmlns", "http://jabber.org/protocol/muc#user");
                  x.setParent(presence);
                  
                  Packet presenceItem = new Packet("item");
                  presenceItem.setAttribute("affiliation", "none");
                  presenceItem.setAttribute("jid", itemJID.toString());
                  presenceItem.setParent(x);
                  manager.deliverToGroup(group, presence);
                  }
                  
                  else {
                  // added to the group as a member
                   itemJID = new JabberID(item.getAttribute("jid"));
                  group.jid2nick.put(itemJID.toString(),itemJID.getUser());
                  group.nick2jid.put(itemJID.getUser(),itemJID.toString());
                  
                  // reflect the result back to the admin
                  Packet iq = new Packet("iq");
                  iq.setID(packet.getID());
                  iq.setTo(packet.getFrom());
                  iq.setType("result");
                  MessageHandler.deliverPacket(iq);
                  
                     // send all the old members to the newly added member
//                  <presence
//                          from='coven@chat.shakespeare.lit/firstwitch'
//                          id='3DCB0401-D7CF-4E31-BE05-EDF8D057BFBD'
//                          to='hag66@shakespeare.lit/pda'>
//                         <x xmlns='http://jabber.org/protocol/muc#user'>
//                         <item affiliation='owner' role='moderator'/>
//                         </x>
//                   </presence>
                  Enumeration members = group.nick2jid.keys();
                  
    while(members.hasMoreElements()){
        
        
        Packet AlltoNew = new Packet("presence");
                  String rec = (String)members.nextElement();
                  AlltoNew.setFrom(group.jid+"/"+rec);
                  AlltoNew.setID("35353425");
                  AlltoNew.setTo(itemJID.toString());
                  
                  Packet newX = new Packet("x");
                  newX.setAttribute("xmlns", "http://jabber.org/protocol/muc#user");
                  
                  Packet newItem = new Packet("item");
                   Iterator itr = group.listOfAdmins.iterator();
        JabberID jid ;
       while(itr.hasNext())
        {
            jid = (JabberID) itr.next();
            if((jid.getUser()).equals(rec))
            {
                newItem.setAttribute("affiliation", "admin");
            }
            else
            {
                newItem.setAttribute("affiliation", "member");
            }
        }
      
       newItem.setParent(newX);
       newX.setParent(AlltoNew);
      Log.trace("ChatMan: delivering to new member  "+ group.jid+"/"+itemJID.getUser()+"    "+ AlltoNew.toString());
      MessageHandler.deliverPacket(AlltoNew);
    }
                  // then you update the presence of this individual to all the other occupants
                  Packet presenceUpdate = new Packet ("presence");
                  presenceUpdate.setFrom(group.jid+"/"+itemJID.getUser()); // this should come from inside the group not outside
                  Packet x = new Packet("x");
                  x.setAttribute("xmlns", "http://jabber.org/protocol/muc#user");
                  Packet otheritem = new Packet("item");
                  otheritem.setAttribute("affiliation", "member");
                  otheritem.setAttribute("jid", itemJID.toString());
                  manager.deliverToGroup(group, presenceUpdate);
              }
                  }
                  
                  
               
              if(affiliation.equalsIgnoreCase("none"))
              {
                  // remove this member from the group
                  JabberID itemJID2 = new JabberID(item.getAttribute("jid"));
                  group.jid2nick.remove(itemJID2.toString());
                  group.nick2jid.remove(itemJID2.toString());
                  
                  
                   // reflect the result back to the admin
                  Packet iq = new Packet("iq");
                  iq.setID("remove_member");
                  iq.setTo(packet.getFrom());
                  iq.setType("result");
                  MessageHandler.deliverPacket(iq);
                  
//<presence
//    from='coven@chat.shakespeare.lit/thirdwitch'
//    to='crone1@shakespeare.lit/desktop'>
//    type='unavailable'>
//  <x xmlns='http://jabber.org/protocol/muc#user'>
//    <item affiliation='none' role='none'/>
//    <status code='321'/>
//  </x>
//</presence>
                   
                   Packet AlltoNew = new Packet("presence");
                  String rec = itemJID2.toString();
                  AlltoNew.setFrom(group.jid+"/"+rec.substring(0, rec.indexOf('@')));
                  AlltoNew.setID("35353425");
                  AlltoNew.setTo(itemJID2.toString());
                  AlltoNew.setType("unavailable");
                  
                  Packet xpacket = new Packet("x");
                  xpacket.setAttribute("xmlns", "http://jabber.org/protocol/muc#user");
                  xpacket.setParent(AlltoNew);
                  
                  Packet itempacket = new Packet("item");
                  itempacket.setAttribute("affiliation", "none");
                  itempacket.setParent(xpacket);
                  
                  Packet status = new Packet("status");
                  status.setAttribute("code", "321");
                  status.setParent(xpacket);
                  MessageHandler.deliverPacket(AlltoNew);
                  manager.deliverToGroup(group, AlltoNew);

                  
              }
              if(affiliation.equalsIgnoreCase("admin"))
              {
                  // add the member to the admin list
                  group.listOfAdmins.add(itemJID.toString());
                  
                  // reflect the result back to the admin
                  Packet iq = new Packet("iq");
                  iq.setID("remove_member");
                  iq.setTo(packet.getFrom());
                  iq.setType("result");
                  MessageHandler.deliverPacket(iq);
                  
//                  <presence
//    from='coven@chat.shakespeare.lit/secondwitch'
//    to='crone1@shakespeare.lit/desktop'>
//  <x xmlns='http://jabber.org/protocol/muc#user'>
//    <item affiliation='admin'
//          jid='wiccarocks@shakespeare.lit'
//          role='moderator'/>
//  </x>
//</presence>
                  Packet presence = new Packet("presence");
                  presence.setFrom(group.jid+"/"+packet.getFrom());
                  
                  Packet x = new Packet("x");
                  x.setAttribute("xmlns", "http://jabber.org/protocol/muc#user");
                  x.setParent(presence);
                  
                  Packet item3 = new Packet("item");
                  item3.setAttribute("affiliation", "admin");
                  item3.setAttribute("jid", itemJID.toString());
                  
                  manager.deliverToGroup(group, presence);
              }
              
              
      }
    }
//    <iq from='coven@chat.shakespeare.lit'
//    id='member3'
//    to='crone1@shakespeare.lit/desktop'
//    type='result'>
//  <query xmlns='http://jabber.org/protocol/muc#admin'>
//    <item affiliation='member'
//          jid='hag66@shakespeare.lit'
//          nick='thirdwitch'
//          role='participant'/>
//  </query>
//</iq>
    if(packet.getType().equalsIgnoreCase("get"))
    {
        Packet iq = new Packet("iq");
        iq.setFrom(group.jid);
        iq.setType("result");
        iq.setID("234234464565465");
        iq.setTo(packet.getFrom());
        
        Packet query = new Packet("query");
        query.setAttribute("xmlns", "http://jabber.org/protocol/muc#admin");
        query.setParent(iq);
        Enumeration members = group.jid2nick.keys();
        while(members.hasMoreElements()){
            Packet item = new Packet("item");
            String mmbr = (String) members.nextElement();
            item.setAttribute("jid", mmbr);
             Iterator itr = group.listOfAdmins.iterator();
        JabberID jid ;
       while(itr.hasNext())
        {
            jid = (JabberID) itr.next();
            if((jid.toString()).equals(mmbr))
            {
                item.setAttribute("affiliation", "admin");
            }
            else
            {
                item.setAttribute("affiliation", "member");
            }
        }
           query.getChildren().add(item);
        }
        MessageHandler.deliverPacket(iq);
    }
//    try {
//      Writer out = userIndex.getWriter(recipient);
//      if (out != null){
//        packet.writeXML(out);
//      } else {
//        Log.info("Could not deliver: " + packet.toString()); // Will eventually store&forward
//      }
//    } catch (Exception ex){
//      ex.printStackTrace();
//    }
    /*
    Log.trace("[QT] Handling IQ packet");

    // Ensure packet is addressed to us
    if (packet.getTo().equals(Server.SERVER_NAME)){

      IQPacket reply = new IQPacket(packet.getSession());
      reply.setTo(packet.getFrom());
      reply.setFrom(packet.getTo());
      reply.setID(packet.getID());
      reply.setType("result");

      // Grab all queries and handle them
      LinkedList queries = (LinkedList)packet.getQueries();
      while(queries.size() > 0){
        Query query = (Query)queries.removeFirst();
        query.addResultQuery(reply);
        if (reply.getType().equals("error")){
          break;
        }
      }

      // All queries recognized and handled so we just need to send the reply
      reply.writeXML();
      return;
    }
      
    // packet addressed to someone else, send to addressee
    // TODO: We don't know how to do this yet...
    Log.error("*Server TODO* delivery to other clients not supported");
    return;
    */
  }
}
/*here we can add all the services you want :
      1- restaurant menus
      2- book a flight
      3- buy a ticket (all kinds of tickets)
      4- hire a driver
      5- delivery services
      6- shopping
 */
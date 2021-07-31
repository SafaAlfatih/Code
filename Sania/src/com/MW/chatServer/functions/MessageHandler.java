package com.MW.chatServer.functions;

import com.MW.chatServer.jabber.ErrorTool;
import java.io.Writer;
import java.util.Iterator;
import com.MW.chatServer.jabber.xml.PacketListener;
import com.MW.chatServer.jabber.Session;
import com.MW.chatServer.jabber.JabberID;
import com.MW.chatServer.log.Log;
import com.MW.chatServer.jabber.xml.Packet;

/**
 * Title:
 * Description:  handles Messages
 * Copyright:     Copyright (c) 2010
 * Company:     Sania Corp.
 * @author        Siddiq Hamed
 * @version      1.0
 */

public class MessageHandler implements PacketListener {

  static UserIndex userIndex;
  //static int counter =1;
  GroupChatManager chatMan = GroupChatManager.getManager();
  public MessageHandler(UserIndex index) { userIndex = index; }

  public void notify(Packet packet){
    if (packet.getSession().getStatus() != Session.AUTHENTICATED){
      Log.trace("Dropping packet (no auth): " + packet.toString());
      return;
    }
    String recipient = packet.getTo();
    String status = packet.getAttribute("status");
    if(status.equalsIgnoreCase("recepient-received"))
    {
     //   Log.trace("Forwarding the delivery report: "+counter+" " + packet);
        boolean cleared = packet.clearChildren();
        deliverPacket(packet);
        return ;
    } else if(status.equalsIgnoreCase("server-received")){
        Log.trace("Dropping packet: " + packet.toString());
        return;
    }
    else {
    
    
    Packet sentPacket = new Packet("message");
    sentPacket.setFrom(Server.SERVER_NAME);
    sentPacket.setTo(packet.getFrom());
    sentPacket.setAttribute("status", "server-received");
    sentPacket.setAttribute("id" , packet.getAttribute("id"));
    sentPacket.setType(packet.getType());
         deliverPacket(sentPacket);
          Log.trace("Delivering message status back to sender");
      
    if (recipient == null){ // to server
      Log.trace("Dropping packet: " + packet.toString());
      return;
    }

    if (recipient.equalsIgnoreCase(Server.SERVER_NAME)){ // to server
      Log.trace("Dropping packet: " + packet.toString());
      return;
    }

    // Fill in sender as resource that sent message (anti-spoofing)
    packet.setFrom(packet.getSession().getJID().toString());

    if (packet.getAttribute("type").equals("groupchat")){
      if (chatMan.isChatPacket(packet)){
        chatMan.handleChatMessage(packet);
      } else {
        Log.trace("Dropping packet: " + packet.toString());
      }
      return;
    }
    deliverPacket(packet);

    }
  }

  static public void deliverPacketToAll(String username, Packet packet){
    packet.setTo(null); // clear recipient
    User user = userIndex.getUser(username);
    Iterator sessions = user.getSessions();
    while (sessions.hasNext()){
      Session session = (Session)sessions.next();
      if (session.getPriority() >= 0){
        packet.setSession(session);
        deliverPacket(packet);
      }
    }
  }

  static public void deliverPacket(Packet packet){
    try {
      String recipient = packet.getTo();
      Writer out;

      if (recipient == null){
        out = packet.getSession().getWriter();
        if (out == null){
          Log.info("Undeliverable packet " + packet.toString());
          return;
        }
      } else {
        out = userIndex.getWriter(recipient);
      }
      if (out != null){
        Log.trace("Delivering packet: " + packet.toString());
        packet.writeXML(out);
      } else { 
        Log.info("Store & forward: " + packet.toString()); // user is off-line in this moment we save it for later then
        User user = userIndex.getUser(new JabberID(recipient).getUser());
        user.storeMessage(packet);
      }
    } catch (Exception ex){
      Log.error("MessageHandler: ", ex);
    }
  }
}
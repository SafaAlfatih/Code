package com.MW.chatServer.functions;


import com.MW.chatServer.jabber.xml.PacketListener;
import com.MW.chatServer.jabber.ErrorTool;
import com.MW.chatServer.jabber.Session;
import com.MW.chatServer.log.Log;
import com.MW.chatServer.jabber.xml.Packet;

/**
 * Title:nothing
 * Description: I feel bored
 * Copyright:    Copyright (c) 2014
 * Company:Sania
 * @author Siddiq
 * @version 1.0
 */

public class PresenceHandler implements PacketListener {

  UserIndex userIndex;
  GroupChatManager chatMan = GroupChatManager.getManager();
  public PresenceHandler(UserIndex index) { userIndex = index; }

  public void notify(Packet packet){
    if (packet.getSession().getStatus() != Session.AUTHENTICATED){
      Log.info("PresenceHandler: Not authenticated" + packet.toString());
      packet.setTo(null);
      packet.setFrom(null);
      ErrorTool.setError(packet,401,"You must be authenticated to send presence");
      MessageHandler.deliverPacket(packet);
    } else if (chatMan.isChatPacket(packet)){ // if the packet is a groupchat packet
      Log.trace("PresenceHandler: groupchat presence");
      chatMan.handleChatPresence(packet); //send it to groupchat Manager to handle this presence packet
    } else {
      Log.trace("PresenceHandler: user presence sending to user roster: " +
                packet.getSession().getJID().getUser());
      User user = userIndex.getUser(packet.getSession().getJID().getUser());
      user.getRoster().updatePresence(packet);
    }
  }
}
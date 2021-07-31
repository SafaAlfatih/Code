package com.MW.chatServer.functions;

import com.MW.chatServer.jabber.ErrorTool;
import com.MW.chatServer.jabber.Session;
import com.MW.chatServer.jabber.xml.PacketListener;
import com.MW.chatServer.log.Log;
import com.MW.chatServer.jabber.xml.Packet;


/**
 * Title: handles roster (friends lists)
 * Description: it handles the friends addition and deletion of friends
 * along with blocking them and refreshing lists when members uninstall the
 * client app
 * Copyright:    Copyright (c) 2017
 * Company: MW modern Tech
 * @author : Siddiq Hamed
 * @version 1.0
 */

public class RosterHandler implements PacketListener {

  UserIndex userIndex;
  public RosterHandler(UserIndex index) { userIndex = index; }

  public void notify(Packet packet) {
    packet.setTo(null);
    packet.setFrom(null);

    if (packet.getSession().getStatus() != Session.AUTHENTICATED){
      Log.info("RosterHandler: Not authenticated" + packet.toString());
      ErrorTool.setError(packet,401,"Authentication is required");
      MessageHandler.deliverPacket(packet);
      return;
    }

    User user = userIndex.getUser(packet.getSession());
    if (packet.getType().equals("set")){
    	String jid = packet.getFirstChild("query").getFirstChild("item").getAttribute("jid");
    	if(jid == null)
    	{
    		packet.clearChildren();
    		ErrorTool.setError(packet, 400, "Contact Phone Number is Required");
    		MessageHandler.deliverPacket(packet);
    		return;
    	}
    	if(jid.equalsIgnoreCase(""))
    	{
    		packet.clearChildren();
    		ErrorTool.setError(packet, 400, "Contact Phone Number is Required");
    		MessageHandler.deliverPacket(packet);
    		return;
    	}
    	
    	User contact = userIndex.getUser(jid);
    	if(contact == null)
    	{
    		packet.clearChildren();
    		ErrorTool.setError(packet, 404, "Contact Phone Number is not a User");
    		MessageHandler.deliverPacket(packet);
    		return;
    	}
      user.getRoster().updateRoster(packet);
      return;
    }

    if (packet.getType().equals("get")){
      Log.trace("RosterHandler: get roster List");
      packet.setType("result");
      packet.getChildren().clear();
      user.getRoster().getPacket().setParent(packet);
      MessageHandler.deliverPacket(packet);
      return;
    }

    Log.info("RosterHandler: Unknown packet" + packet.toString());
    ErrorTool.setError(packet,400,"Unknown Operation");
    MessageHandler.deliverPacket(packet);
  }
}

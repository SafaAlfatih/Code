package com.MW.chatServer.functions;


import com.MW.chatServer.jabber.xml.PacketListener;
import com.MW.chatServer.jabber.Session;

import com.MW.chatServer.log.Log;
import com.MW.chatServer.jabber.xml.Packet;
/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author : Siddiq
 * @version 1.0
 */

public class CloseStreamHandler implements PacketListener {

  UserIndex userIndex;
  public CloseStreamHandler(UserIndex index) { userIndex = index; }

  public void notify(Packet packet){
    try {
      Log.trace("Closing session");
      Session session = packet.getSession();
      session.getWriter().write("</stream:stream> ");
      session.getWriter().flush();
      session.getSocket().close();
      userIndex.removeSession(session);
      Log.trace("Closed session");
    } catch (Exception ex){
      Log.error("CloseStreamHandler: ",ex);
      userIndex.removeSession(packet.getSession());
      Log.trace("Exception closed session");
    }
  }
}
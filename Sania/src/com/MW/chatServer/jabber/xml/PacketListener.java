package com.MW.chatServer.jabber.xml;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2017
 * Company:
 * @author :Siddiq Hamed apparently
 * @version 1.0
 */

public interface PacketListener {
  public void notify(Packet packet);
}
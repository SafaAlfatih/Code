package com.MW.chatServer.jabber;

/**
A listener that can be updated on a session's status.
*/
public interface StatusListener {
  public void notify(int status);
}

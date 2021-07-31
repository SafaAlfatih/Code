package com.MW.chatServer.jabber.xml;

import com.MW.chatServer.jabber.Session;
import com.MW.chatServer.jabber.xml.JabberInputHandler;
import com.MW.chatServer.jabber.xml.PacketQueue;
import com.MW.chatServer.log.Log;
import org.xml.sax.SAXParseException;


public class ProcessThread extends Thread {

  Session session;
  PacketQueue packetQueue;

  public ProcessThread(PacketQueue queue, Session session){
    packetQueue = queue;
    this.session = session;
  }

  public void run(){
    try {
      // Processing incoming
      JabberInputHandler handler = new JabberInputHandler(packetQueue);
      handler.process(session);
    }catch(SAXParseException exxx){
       Log.trace("ProcessThread: User logged out enexpectedly");
       if (session.getStatus() > Session.CONNECTED){
           try {
        	 Log.trace("Closing session");
             session.disconnect();
             Log.trace("Closed session");
           } catch (Exception eex){
           }
         }
    } 
    catch (Exception ex){
      Log.error("ProcessThread: ", ex);
      if (session.getStatus() > Session.CONNECTED){
        try {
          session.disconnect();
        } catch (Exception eex){
        }
      }

    }
  }
}


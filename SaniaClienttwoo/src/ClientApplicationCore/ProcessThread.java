/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

class ProcessThread extends Thread {

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
    } catch (Exception ex){
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
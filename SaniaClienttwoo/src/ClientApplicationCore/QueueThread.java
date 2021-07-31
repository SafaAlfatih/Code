/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

import java.util.HashMap;
import java.util.Iterator;

class QueueThread extends Thread {

  PacketQueue packetQueue = new PacketQueue();

    
  public QueueThread() {  }

  HashMap packetListeners = new HashMap();

  public boolean addListener(PacketListener listener, String element){
    if (listener == null || element == null){
      return false;
    }
    packetListeners.put(listener,element);
    return true;
  }

  public boolean removeListener(PacketListener listener){
    packetListeners.remove(listener);
    return true;
  }

  public void run(){

    for( Packet packet = packetQueue.pull();
         packet != null;
         packet = packetQueue.pull()) {

      try {
        Packet child;
        String matchString;
        if (packet.getElement().equals("iq")){
          child = packet.getFirstChild("query");
          if (child == null){
            matchString = "iq";
          } else {
            matchString = child.getNamespace();
          }
        } else {
          matchString = packet.getElement();
        }

        synchronized(packetListeners){
          Iterator iter = packetListeners.keySet().iterator();
          while (iter.hasNext()){
            PacketListener listener = (PacketListener)iter.next();
            String listenerString = (String)packetListeners.get(listener);
            if (listenerString.equals(matchString)){
              listener.notify(packet);
            } // if
          } // while
        } // sync
      } catch (Exception ex){
        Log.error("QueueThread: ", ex); // Soldier on - no matter what
      }
    } // for
  } // run()

    PacketQueue getQueue() {
        return this.packetQueue;
    }
} // class QueueThread

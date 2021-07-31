/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

import java.util.Iterator;
import java.util.LinkedList;

class PacketQueue {
  LinkedList queue = new LinkedList();
  
  /**
  Push a packet onto the end of the queue.
  */
  public synchronized void push(Packet packet){
    Log.trace("[PQ] " + packet);
    queue.add(packet);
    notifyAll();
    
  }

  /**
  Pull the packet at the head of the queue.
  
  @return The packet at the head of the queue, or null if no packets are available
  */
  public synchronized Packet pull(){
      try {
      while (queue.isEmpty()) {
        wait();
      }
    } catch (InterruptedException e){
      return null;
    }
    return (Packet)queue.remove(0);
  }
}

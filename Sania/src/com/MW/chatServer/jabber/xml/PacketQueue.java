package com.MW.chatServer.jabber.xml;

import java.util.LinkedList;

import com.MW.chatServer.log.Log;

/**
A simple class that queues up packets for processing, and processes them.
*/
public class PacketQueue {
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

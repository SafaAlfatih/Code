/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.MW.chatServer.functions;

import com.MW.chatServer.jabber.xml.Packet;
import com.MW.chatServer.jabber.xml.PacketListener;

/**
 *
 * @author Siddiq
 * we are mimicking a stateless server  so we don't maintain any transaction in the server
 * we only work as a proxy that routes the packet to their destination.
 */
public class JingleHandler implements PacketListener{
    
  UserIndex userIndex;
  public JingleHandler(UserIndex index) { userIndex = index; }

    @Override
    public void notify(Packet packet) {
        // just send the packet to it's destination , as we are implementing an stateless server
        // so we don't keep any transaction in the server , the server only job is routing the jingle packets
        
            MessageHandler.deliverPacket(packet);   
    }
    
}

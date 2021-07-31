/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.MW.chatServer.functions;


import com.MW.chatServer.jabber.xml.PacketListener;
import com.MW.chatServer.jabber.xml.Packet;

/**
 *
 * @author Siddiq
 */
public class OutOfBandHandler implements PacketListener {
UserIndex userIndex;
 public OutOfBandHandler(UserIndex index) { userIndex = index; }
    @Override
    public void notify(Packet packet) {
       
    }
    
}

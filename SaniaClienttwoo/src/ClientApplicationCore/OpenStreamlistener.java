/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

 class OpenStreamHandler implements PacketListener{

  public void notify(Packet packet){
    Session session = packet.getSession();
    session.setStreamID(packet.getID());
    session.setJID(new JabberID(packet.getTo()));
    session.setStatus(Session.STREAMING);
  }
}

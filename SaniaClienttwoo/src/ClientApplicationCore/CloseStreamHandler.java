/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

class CloseStreamHandler implements PacketListener {

  public void notify(Packet packet){
    Session session = null;
    try {
      packet.getSession().setStatus(Session.DISCONNECTED);
    } catch (Exception ex){
      ex.printStackTrace();
    }
  }

}
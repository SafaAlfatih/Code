/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

class memberAdditionHandler implements PacketListener {

  JabberModel jabberModel;
  
  memberAdditionHandler(JabberModel model){
    jabberModel = model;
  }  

    

  public void notify(Packet packet) {
      
    if (packet.getID() != null){
      PacketListener listener = jabberModel.removeResultHandler(packet.getID());
      if (listener != null){
        listener.notify(packet);
        Log.trace("something in here I guess");
        return;
      }
    }
    Log.trace("Dropping : " + packet.toString());
  }
}

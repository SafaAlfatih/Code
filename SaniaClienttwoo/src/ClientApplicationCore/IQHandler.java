/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

class IQHandler implements PacketListener {

  JabberModel jabberModel;
  
  public IQHandler(JabberModel model){
    jabberModel = model;
  }  

  public void notify(Packet packet) {
      
    if (packet.getID() != null){
      PacketListener listener = jabberModel.removeResultHandler(packet.getID());
      if (listener != null){
        listener.notify(packet);
        return;
      }
    }
    Log.trace("Dropping : " + packet.toString());
  }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;
class RegisterHandler implements PacketListener {

// called only on register results/errors
  JabberModel jabberModel;
  public RegisterHandler(JabberModel model){
    jabberModel = model;
  }

  public void notify(Packet packet){
  
      try {
      if (packet.getType().equals("result")){
        jabberModel.authenticate();
        
      } else {
        String message = "Failed to register";
        if (packet.getType().equals("error")){
          message = message + ": " + packet.getChildValue("error");
        }
        System.out.println("Register Handler: " + message);
      }
    } catch (Exception ex){
      ex.printStackTrace();
    }
  }
}
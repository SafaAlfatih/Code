/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

import java.io.IOException;

class MessageHandler implements PacketListener {
    private  JabberModel jabbermodel;

    public MessageHandler(JabberModel model) {
        this.jabbermodel = model;
    }

  public void notify(Packet packet){
      
    String status = packet.getAttribute("status");
   Packet deliveryReport = new Packet("message");
    if(status != null)
    {
        if(status.equalsIgnoreCase("sent"))
        {
            deliveryReport.setAttribute("status", "recepient-received");        
                deliveryReport.setTo(packet.getFrom());
                deliveryReport.setFrom(jabbermodel.getUser()+"@"+jabbermodel.sName);
                deliveryReport.setID(packet.getID());
                
            try {
                packet.getSession().getWriter().flush();
                deliveryReport.writeXML(jabbermodel.getSession().getWriter());
                
            } catch (IOException ex) {
                Log.error("Error sending delivery update", ex);
            }
           
            
        }
    }
    else
    {
        Log.trace("Recieved A new Message : "+packet);
        /*
        here we should handle the different message that contain the URL of files
        and the user interface should show different bubbles for different contents
        1- Regular text messages
        2- Photo/picture messages ( with some place for the picture in the bubble )
        3- Video messages with some panel to view the video
        4- Location information message
        5- Contact information message
        6- Audio message
        7- Documents messages
        */
    }  
  }
}

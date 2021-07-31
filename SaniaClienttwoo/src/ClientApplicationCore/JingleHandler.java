/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Siddiq
 * this is the handler which will handle the SIP related packets
 */
public class JingleHandler implements PacketListener{
    
    private  JabberModel jabbermodel;

    public JingleHandler(JabberModel model) {
        this.jabbermodel = model;
    }

  public void notify(Packet packet){
//      <iq from='juliet@capulet.lit/balcony'
//    id='rg6s5134'
//    to='romeo@montague.lit/orchard'
//    type='result'/>
//    
      
      
//      <iq from='juliet@capulet.lit/balcony'
//    id='ch3vs61d'
//    to='romeo@montague.lit/orchard'
//    type='set'>
//  <jingle xmlns='urn:xmpp:jingle:1'
//          action='session-terminate'
//          initiator='romeo@montague.lit/orchard'
//          sid='a73sjjvkla37jfea'>
//    <reason>
//      <busy/>
//    </reason>
//  </jingle>
//</iq>
    
    
    
      
// <iq from='juliet@capulet.lit/balcony'
//    id='tgr515bt'
//    to='romeo@montague.lit/orchard'
//    type='set'>
//  <jingle xmlns='urn:xmpp:jingle:1'
//          action='session-info'
//          initiator='romeo@montague.lit/orchard'
//          sid='a73sjjvkla37jfea'>
//    <ringing xmlns='urn:xmpp:jingle:apps:rtp:info:1'/>
//  </jingle>
//</iq>
           
         
          
      
    
  }
}
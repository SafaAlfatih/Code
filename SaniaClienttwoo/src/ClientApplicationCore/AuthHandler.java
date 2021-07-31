/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

import java.io.Writer;

class AuthHandler implements PacketListener {

  JabberModel jabberModel;
  public AuthHandler(JabberModel model){
    jabberModel = model;
  }
  Authenticator auth = new Authenticator();
  int counter;

  public void notify(Packet packet) {
    try {
      if (packet.getID().startsWith("auth_get")){ // 0k token query
        Packet query = packet.getFirstChild("query");
        String token = query.getChildValue("token");
        int sequence = Integer.parseInt(query.getChildValue("sequence"));
        String hash = auth.getZeroKHash(sequence,
                                        token.getBytes(),
                                        jabberModel.getPassword().getBytes());
        // Send 0k auth
        jabberModel.addResultHandler("0k_auth_" + Integer.toString(counter),this);
        Writer out = packet.getSession().getWriter();
        out.write("<iq type='set' id='0k_auth_");
        out.write(Integer.toString(counter++));
        out.write("'><query xmlns='jabber:iq:auth'><username>");
        out.write(jabberModel.getUser());
        out.write("</username><resource>");
        out.write(jabberModel.getResource());
        out.write("</resource><hash>");
        out.write(hash);
        out.write("</hash></query></iq>");
        out.flush();
      } else if (packet.getType().equals("result")){
        packet.getSession().setStatus(Session.AUTHENTICATED);
      } else if (packet.getType().equals("error")){
      System.out.println();//  JOptionPane.showMessageDialog(null,"Failed to authenticate: " + packet.getChildValue("error"));
      } else {
      System.out.println();//  JOptionPane.showMessageDialog(null,"Unknown auth result: " + packet.toString());
      }
    } catch (Exception ex){
      ex.printStackTrace();
    }
  }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Siddiq
 */
public class CallThread extends Thread{
    final static public int    RTP_port = 5221;
  //  SIPSession session ;
    final int CALLING     = 1;
    final int PICKINGUP = 2;
    int status ;

    public CallThread(/*SIPSession session , */int status) {
   //     this.session = session;
        this.status    = status;
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        
        // since we are using the same object to handle both incoming and outgoing calls, we check
        if(status == CALLING) // if we are calling
        {
            
            try {
            // Create the server socket to listen to the callee to connect
            serverSocket = new ServerSocket(RTP_port);
            Socket newSock = serverSocket.accept();
            // change the state of the session to Calling
        //    session.setStatus(SIPSession.CALLING);
        } catch (IOException ex) {
            Log.error("could not accept connections", ex);
        }
            
        } else if(status == PICKINGUP) // if we are taking a call
        {
            
        } else // some other value that means nothing 
        {
            Log.error("wrong choice", null); // log the error
            return ; // jumb out to kill the thread
        }
        
    }
    
    
}

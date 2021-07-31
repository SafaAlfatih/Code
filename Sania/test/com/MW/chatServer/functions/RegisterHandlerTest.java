/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.MW.chatServer.functions;

import com.MW.chatServer.jabber.xml.Packet;
import junit.framework.TestCase;

/**
 *
 * @author safa alfatih
 */
public class RegisterHandlerTest extends TestCase {
    
    public RegisterHandlerTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of notify method, of class RegisterHandler.
     */
    public void testNotify() {
        System.out.println("notify");
        Packet packet = null;
        RegisterHandler instance = null;
        instance.notify(packet);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}

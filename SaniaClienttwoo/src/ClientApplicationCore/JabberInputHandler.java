/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ClientApplicationCore;

import java.io.IOException;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class JabberInputHandler extends DefaultHandler {

  PacketQueue packetQ;
  Session session;
  /**
  Setup the class with the queues for incoming tasks and outgoing messages.

  @param packetQueue A Queue (FIFO) to place incoming jabber commands
  */
  public JabberInputHandler(PacketQueue packetQueue) {
    packetQ = packetQueue;
  }

  /**
  We process the input stream until it returns an end of stream, placing messages into
  the sendQueue and all other into the taskQueue.  Internally we use a SAX parser to
  parse the InputStream.  Since the parser isn't capable of handling chunks, if you
  want to share this stream processor with different sockets, you need to create
  a "multiplexing stream" to switch stream inputs from the various sockets into
  the single input stream handed to this process.
  <h2>Notice</h2>
  In order to enable the incremental paring of streaming XML data, we have to setup
  our own custom streaming character reader factory.  This customization makes the code
  Xerces specific... you may need to change this if you use an xml parsing library that is
  not Xerces.

  @param in The input stream to process
  */
  public void process(Session session) throws IOException, SAXException {
    // Create the XML parser... SAX
    // The unusual syntax switches in our StreamingCharFactory class so we can
    // load xml off a stream at will.  The 2 changes are marked with **

    // ** First we directly create a SAX parser as this code only works with Xerces
    SAXParser parser = new SAXParser();

    // Set this class as the content handler
    parser.setContentHandler(this);

    // ** Set our custom streaming char reader factory
    parser.setReaderFactory(new StreamingCharFactory());

    this.session = session;

    // Parse as normal.
    parser.parse(new InputSource(session.getReader()));
  }


  /* #####################################################################
  Our listener methods extends the default handler.  We're only interested in
  a limited amount of information.  We simply ignore unknown elements.
  */

  /** The current packet being built.  Is null if there is no packet under construction. */
  Packet packet;
  int depth = 0;

  /**
  The beginning element determines the behavior of the processor for the element
  and all its children.  The possible outcomes are:

  <ul>
  <li>Ignore  - The element is not recognized so it and its children are ignored.
  <li>Build   - The element is supported and an appropriate java object
                must be built. (e.g. the message element)
  <li>Act     - The element signals an immediate need for action.
                (e.g. the open stream element)
  </ul>

  Currently we support only the action elements

  <ul>
  <li>&lt;xml:stream&gt; and &lt;/xml:stream&gt;
  </ul>
  */
  public void startElement(String namespaceURI,
                           String localName,
                           String qName,
                           Attributes atts)
                    throws SAXException{
  //  Log.trace("[XS] URI: " + namespaceURI + " lName: " + localName + " qName: " + qName);
    switch (depth++){
    case 0:   // Only stream:stream allowed... all others is error
      if (qName.equals("stream:stream")){
        Packet openPacket = new Packet(null,qName,namespaceURI,atts);
        openPacket.setSession(session);
        packetQ.push(openPacket);
        return;
      }
      throw new SAXException("Root element must be <stream:stream>");
    case 1:   // Only message, presence, iq
      packet = new Packet(null,qName,namespaceURI,atts);
      packet.setSession(session);
      break;
    default:  // Inside packet
      Packet child = new Packet(packet,qName,namespaceURI,atts);
      packet = child;
    }
  }

  /**
  Add given characters to the current packet if there is one.
  */
  public void characters(char[] ch,
                     int start,
                     int length)
              throws SAXException{

  //  Log.trace("[XC] " + new String(ch,start,length));

    if (depth > 1){
      packet.getChildren().add(new String(ch,start,length));
    }
  }

  /**
  Complete the packet under constructon (if there is one) and insert
  it into the appropriate queue.
  <p>
  Notice that end elements can also trigger three possible actions:

  <ul>
  <li>Ignore - if the packet is unknown it must be ignored.
  <li>Build  - If a packet is under construction, the close element tells us to complete
               it and add it to the appropriate queue. (e.g. the message element)
  <li>Act    - Some packets can trigger immediate action. (e.g. the close stream element)
  */
  public void endElement(java.lang.String uri,java.lang.String localName,java.lang.String qName)throws SAXException {

//    Log.trace("[XE] finished with " + qName);

    switch(--depth){
    case 0:   // We're back at the end of the root
      Packet closePacket = new Packet("/stream:stream");
      closePacket.setSession(session);
      packetQ.push(closePacket);
      break;
    case 1:   // The Packet is finished
      packetQ.push(packet);
      break;
    default:  // Move back up the tree
      packet = packet.getParent();
    }
  }
}
package com.MW.chatServer.functions;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Iterator;

import com.MW.chatServer.log.Log;

import com.MW.chatServer.jabber.Session;
import com.MW.chatServer.jabber.Presence;
import com.MW.chatServer.jabber.JabberID;
import com.MW.chatServer.jabber.xml.Packet;

/**
 * Title:
 * Description:
 *
 * Actions:
 * get: Send roster snapshot
 * set: Send roster pushes
 * presence subscription: send roster pushes, forward presence packets
 * user presence update: forward presence to subscribers
 * subscriber presence update: forward presence to user, detect new subscribers available and update them with presence
 *
 * Data:
 * Roster data structure
 * The user
 * Available outgoing subscribers
 * Available
 *
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class Roster {

  String user;
  public Roster(String username) { user = username; }

  /**
   * Updates the Roster with the given roster "set" <query> packet.
   *
   * For each item, we replace any existing roster <item>, from
   * the items hashtable, and add the new roster <item> (editing
   * it with subscriber info).
   *
   * We a roster <query> packet that should be sent back to the client
   * as a "roster push" with the roster info for changed items.
   */
  public void updateRoster(Packet packet){
    Packet rosterQuery = packet.getFirstChild("query");
    rosterQuery.setAttribute("xmlns","jabber:iq:roster");
    Iterator rosterItems = rosterQuery.getChildren().iterator();
    while (rosterItems.hasNext()){
      Object child = rosterItems.next();
      if (child instanceof Packet){
        Packet itemPacket = (Packet)child;

        // Clean out empty subitem members
        /*
        Iterator subitems = itemPacket.getChildren().iterator();
        while(subitems.hasNext()){
          Object subitem = subitems.next();
          if (subitem instanceof String){
            if (((String)subitem).trim().length() == 0){
              subitems.remove();
            }
          }
        }
        */

        String subJID = itemPacket.getAttribute("jid");
        Subscriber sub = (Subscriber)subscribers.get(subJID);
        if (sub == null){
          sub = new Subscriber();
          sub.subscription = "none";
          sub.ask = null;
          subscribers.put(subJID,sub);
        }
        itemPacket.setAttribute("subscription",sub.subscription);
        itemPacket.setAttribute("ask",sub.ask);
        items.put(subJID,itemPacket);
        Log.trace("Roster: added " + user + "'s roster item: " + itemPacket);
      } else if (child instanceof String){
        rosterItems.remove();
      }
    }

    // roster push
    packet.setType("set");
    MessageHandler.deliverPacket(packet);
  }

  /**
   * Updates subscribers with given <presence>.
   *
   * We use the presence packet for a few things:
   *
   * Server addressed presence updates (or no "to" address) causes us to
   *    Update the presence associated with the Session.
   *    send a copy of the presence update to all interested subscribers.
   * User addressed presence updates cause us to
   *    Update the presence associated with the Session.
   *    forward presence update to recipient
   * Subscription management packets cause us to
   *    update subscriber entry
   *    update roster item (if any)
   *    roster push changed roster item (if any)
   *    forward the subscription packet to recipient
   *
   * @todo Clean up and refactor into several methods
   * @todo Convert subscription changes into a truth table
   */
  public void updatePresence(Packet presence){
    Log.trace("Roster: processing presence " + presence.toString());
    String type = presence.getType();
    Session session = presence.getSession();
    Presence sessionPresence = session.getPresence();
    String recipient = presence.getTo();
    JabberID recipientID;
    boolean isUserSent;
    if (recipient == null){
      recipientID = new JabberID(Server.SERVER_NAME);
      isUserSent = true;
    } else {
      recipientID = new JabberID(recipient);
      if (user.equals(recipientID.getUser())){
        isUserSent = false;
      } else {
        isUserSent = true;
      }
    }
    String sender = presence.getFrom();
    JabberID senderID;
    if (sender == null){
      senderID = session.getJID();
    } else {
      senderID = new JabberID(sender);
    }
    String subscriber = isUserSent ? recipientID.toString() : senderID.toString();

    if (type == null) {
      type = "available";
    }
    // Presence Update
    if (type.equals("available") || type.equals("unavailable")){
      Log.trace("Roster: presence update");
      if (!isUserSent){
        MessageHandler.deliverPacket(presence);
        return;
      }
      // Update session presence
      sessionPresence.setAvailable(type.equals("available"));
      sessionPresence.setShow(presence.getChildValue("show"));
      sessionPresence.setStatus(presence.getChildValue("status"));
      String priority = presence.getChildValue("priority");
      sessionPresence.setPriority(priority);
      if (priority != null){
        session.setPriority(Integer.parseInt(priority));
      }

      updateSubscribers(presence);
      return;
    }

    // Server-to-server presence probes
    // It should be impossible to receive them
    if (type.equals("probe")) {
      Log.trace("Roster: We don't handle probes yet " + presence.toString());
      return;
    }

    // Subscription maintenance
    // update subscriber entry
    Subscriber sub = (Subscriber)subscribers.get(subscriber);
    if (sub == null){
      sub = new Subscriber();
      sub.subscription = "none";
      subscribers.put(recipient,sub);
      Packet itemPacket = new Packet("item");
      itemPacket.setAttribute("jid",subscriber);
      items.put(sub,itemPacket);
    }
    if (type.equals("subscribe") || type.equals("unsubscribe")){
      Log.trace("Roster: presence subscription");
      sub.ask = type;
    } else if (type.equals("subscribed")){
      sub.ask = null;
      if (isUserSent){
        if (sub.subscription.equals("from")){
          sub.subscription = "both";
        } else if (sub.subscription.equals("none")){
          sub.subscription = "to";
        }
      } else {
        if (sub.subscription.equals("to")){
          sub.subscription = "both";
        } else if (sub.subscription.equals("none")){
          sub.subscription = "from";
        }
      }
    } else if (type.equals("unsubscribed")){
      sub.ask = null;
      if (isUserSent){
        if (sub.subscription.equals("from")){
          sub.subscription = "none";
        } else if (sub.subscription.equals("both")){
          sub.subscription = "to";
        }
      } else {
        if (sub.subscription.equals("to")){
          sub.subscription = "none";
        } else if (sub.subscription.equals("both")){
          sub.subscription = "from";
        }
      }
    } else {
      Log.trace("Roster: Unknown presence type " + presence.toString());
      return;
    }
    // update roster item (if any)
    // roster push changed roster item (if any)
    Packet item = (Packet)items.get(subscriber);
    if (item != null){
      item.setAttribute("subscription",sub.subscription);
      item.setAttribute("ask",sub.ask);
      Packet iq = new Packet("iq");
      iq.setType("set");
      Packet query = new Packet("query");
      query.setAttribute("xmlns","jabber:iq:roster");
      query.setParent(iq);
      item.setParent(query);
      MessageHandler.deliverPacketToAll(user,iq);
    }
    // forward the subscription packet to recipient
    MessageHandler.deliverPacket(presence);
  }

  void updateSubscribers(Packet packet){
    Enumeration subs = subscribers.keys();
    while (subs.hasMoreElements()){
      packet.setTo((String)subs.nextElement());
      MessageHandler.deliverPacket(packet);
    }
  }

  // The actual roster
  // subscriber Jabber ID (String) -> <item> Packets
  Hashtable items = new Hashtable();

  // The subscriber list
  // subscriber Jabber ID (String) -> Subscriber
  Hashtable subscribers = new Hashtable();
  class Subscriber {
    String subscription;
    String ask;
  }

  // For easy delivery of updates
  // List of subscribers JID (Strings) that receive presence updates
  LinkedList presenceRecipients = new LinkedList();

  public Packet getPacket(){
    Packet packet = new Packet("query");
    packet.setAttribute("xmlns","jabber:iq:roster");
    Iterator itemIterator = items.values().iterator();
    while (itemIterator.hasNext()){
      ((Packet)itemIterator.next()).setParent(packet);
    }
    return packet;
  }

}
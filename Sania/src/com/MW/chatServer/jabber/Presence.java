package com.MW.chatServer.jabber;


/**
 * Title:
 * Description:
 *
 * fields: status | priority | show
 *
 * Copyright:    Copyright (c) 2014
 * Company:Sania
 * @author : Siddiq
 * @version 1.0
 */

public class Presence {

  boolean available;
  public boolean isAvailable(){return available;}
  public void    setAvailable(boolean isAvailable){ available = isAvailable;}

  static public final String SHOW_CHAT = "chat";
  static public final String SHOW_AWAY = "away";
  static public final String SHOW_XA   = "xa";
  static public final String SHOW_DND  = "dnd";

  String show;
  public String getShow()               { return show;    }
  public void   setShow(String newShow) { show = newShow; }

  String status;
  public String getStatus()                 { return status;      }
  public void   setStatus(String newStatus) { status = newStatus; }

  String priority;
  public String getPriority()                   { return priority;        }
  public void   setPriority(String newPriority) { priority = newPriority; }
}
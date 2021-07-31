package com.MW.chatServer.jabber;

import com.MW.chatServer.jabber.xml.Packet;




/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class ErrorTool {

  static public String getCode(int codeNumber){
switch (codeNumber){
    case 302: return "Redirect";
    case 400: return "Bad Request";
    case 401: return "Unauthorized";
    case 402: return "Payment Required";
    case 403: return "Forbidden";
    case 404: return "Not Found";
    case 405: return "Not Allowed";
    case 406: return "Not Acceptable";
    case 407: return "Registration Required";
    case 408: return "Request Timeout";
    case 409: return "Conflict";
        // SIP error codes
              case  410  :  return "Gone";
              case  413  :  return "Request Entity Too Large";
              case  414  :  return "Request-URI Too Large";
              case  415  :  return "Unsupported Media Type";
              case  416 :  return "Unsupported URI Scheme";
              case  420  :  return "Bad Extension";
              case  421  :  return "Extension Required";
              case  423  :  return "Interval Too Brief";
              case  480  :  return "Temporarily not available";
              case  481  :  return "Call Leg/Transaction Does Not Exist";
              case  482  :  return "Loop Detected";
              case  483 :  return "Too Many Hops";
              case  484  :  return "Address Incomplete";
              case  485  :  return "Ambiguous";
              case  486  :  return "Busy Here";
              case  487  :  return "Request Terminated";
              case  488  :  return "Not Acceptable Here";
              case  491  :  return "Request Pending";
              case  493  :  return "Undecipherable";
     
    case 500: return "Internal Server Error";
    case 501: return "Not Implemented";
    case 502: return "Remote Server Error";
    case 503: return "Service Unavailable";
    case 504: return "Remote Server Timeout";
        
        // SIp errors
    case 505  :  return "SIP Version not supported";
    case 513  :  return "Message Too Large";
    default: return "Unknown error code";
    }
  }

  static public void setError(Packet packet, int code, String message){
    packet.setType("error");
    Packet e = new Packet("error",message == null ? getCode(code) : message);
    e.setAttribute("code",Integer.toString(code));
    e.setParent(packet);
  }
}
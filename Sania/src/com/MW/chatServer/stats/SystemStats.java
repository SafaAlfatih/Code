/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.MW.chatServer.stats;

import com.MW.chatServer.log.Log;

/**
 *
 * @author Siddiq
 */
public class SystemStats { // singleton class to host all statistics collected from the server
    
    private  static SystemStats systemStats; // the only instance we will be using
    public static final int  SET = 1 ;
    public static final int INCREMENT = 2 ;

    private  SystemStats() {} // no instantiation is allowed
    
    public  static synchronized  SystemStats getInstance() { // single access point to be used to acquire the object
      if(systemStats == null) {
         systemStats = new SystemStats(); // new if this is the first time we get an instance
         Log.trace("System statistics object instantiated");
      }
      return systemStats; // return
   }
    // all items monitored 
    private long threads = 1; // number of working threads
    private long socketConnections = 0; // number of opened connections in the server
    private long users = 0; //number of all users
    private long onlineUsers =0;// number of online users with open connections
    private long databaseConnections =0; // number of opened database connections currently
    private long messages =0;// number of message packets handled by the server in this run (text messages)
    private long presences =0; // number of presence packets handled by the server in this run
    private long IQs =0; // number of IQ packets handled by the server in this run
    private long packetQueueLength =0; // length of packet queue
    private long groups =0; // number of chat rooms
    private long rosterAdditions =0;// number of friend addition request
    private long rosterRemovals =0;// number of friend deletion
    private long rosterGets =0; // number of roster update requests
    private long authentications =0;// number of all authentication operations handled by the server in this run
    private long registrations =0;// number of all registration operations handled by the server in this run
    private long serverReceptionReports =0;// number of all deliver reports  handled by the server in this run
    private long clientReceptionReports =0;// number of all client delivery reports handled by the server in this run
    private long files =0; // number of files currently in the server
    private long fileUploades =0; // number of file downloaded in this run
    private long fileDownloads =0;// number of files uploaded in this run
    private long groupMemberAddition =0;// number of requests made by admins to add  members
    private long groupMemberRemoval =0; // number of requests made by admins to remove members
    private long voiceCalls =0; // number of voice calls signalled with the server in this run
    private long videoCalls =0; // number of video calls signalled with the server in this run
    private long callsDurationInMins =0; // irrelevant

    public long getThreads() {
        return threads;
    }

    public void setThreads(long threads) {
        this.threads = threads;
    }

    public long getSocketConnections() {
        return socketConnections;
    }

    public void setSocketConnections(long socketConnections) {
        this.socketConnections = socketConnections;
    }

    public long getUsers() {
        return users;
    }

    public void setUsers(long users) {
        this.users = users;
    }

    public long getOnlineUsers() {
        return onlineUsers;
    }

    public void setOnlineUsers(long onlineUsers) {
        this.onlineUsers = onlineUsers;
    }

    public long getDatabaseConnections() {
        return databaseConnections;
    }

    public void setDatabaseConnections(long databaseConnections) {
        this.databaseConnections = databaseConnections;
    }

    public long getMessages() {
        return messages;
    }

    public void setMessages(long messages) {
        this.messages = messages;
    }

    public long getPresences() {
        return presences;
    }

    public void setPresences(long presences) {
        this.presences = presences;
    }

    public long getIQs() {
        return IQs;
    }

    public void setIQs(long IQs) {
        this.IQs = IQs;
    }

    public long getPacketQueueLength() {
        return packetQueueLength;
    }

    public void setPacketQueueLength(long packetQueueLength) {
        this.packetQueueLength = packetQueueLength;
    }

    public long getGroups() {
        return groups;
    }

    public void setGroups(long groups) {
        this.groups = groups;
    }

    public long getRosterAdditions() {
        return rosterAdditions;
    }

    public void setRosterAdditions(long rosterAdditions) {
        this.rosterAdditions = rosterAdditions;
    }

    public long getRosterRemovals() {
        return rosterRemovals;
    }

    public void setRosterRemovals(long rosterRemovals) {
        this.rosterRemovals = rosterRemovals;
    }

    public long getRosterGets() {
        return rosterGets;
    }

    public void setRosterGets(long rosterGets) {
        this.rosterGets = rosterGets;
    }

    public long getAuthentications() {
        return authentications;
    }

    public void setAuthentications(long authentications) {
        this.authentications = authentications;
    }

    public long getRegistrations() {
        return registrations;
    }

    public void setRegistrations(long registrations) {
        this.registrations = registrations;
    }

    public long getServerReceptionReports() {
        return serverReceptionReports;
    }

    public void setServerReceptionReports(long serverReceptionReports) {
        this.serverReceptionReports = serverReceptionReports;
    }

    public long getClientReceptionReports() {
        return clientReceptionReports;
    }

    public void setClientReceptionReports(long clientReceptionReports) {
        this.clientReceptionReports = clientReceptionReports;
    }

    public long getFiles() {
        return files;
    }

    public void setFiles(long files) {
        this.files = files;
    }

    public long getFileUploades() {
        return fileUploades;
    }

    public void setFileUploades(long fileUploades) {
        this.fileUploades = fileUploades;
    }

    public long getFileDownloads() {
        return fileDownloads;
    }

    public void setFileDownloads(long fileDownloads) {
        this.fileDownloads = fileDownloads;
    }

    public long getGroupMemberAddition() {
        return groupMemberAddition;
    }

    public void setGroupMemberAddition(long groupMemberAddition) {
        this.groupMemberAddition = groupMemberAddition;
    }

    public long getGroupMemberRemoval() {
        return groupMemberRemoval;
    }

    public void setGroupMemberRemoval(long groupMemberRemoval) {
        this.groupMemberRemoval = groupMemberRemoval;
    }

    public long getVoiceCalls() {
        return voiceCalls;
    }

    public void setVoiceCalls(long voiceCalls) {
        this.voiceCalls = voiceCalls;
    }

    public long getVideoCalls() {
        return videoCalls;
    }

    public void setVideoCalls( int op , long videoCalls) {
        
        if(op == SET)
        {
        this.videoCalls = videoCalls;   
        }
        if(op == INCREMENT)
        {
            this.videoCalls = videoCalls + 1;
        }
        
    }

    public long getCallsDurationInMins() {
        return callsDurationInMins;
    }

    public void setCallsDurationInMins(int op , long callsDurationInMins) {
        if(op == SET)
        {
        this.callsDurationInMins = callsDurationInMins;    
        }
        if(op == INCREMENT)
        {
            this.callsDurationInMins = this.callsDurationInMins + 1;
        }
        
    }
    
    
    
    
}

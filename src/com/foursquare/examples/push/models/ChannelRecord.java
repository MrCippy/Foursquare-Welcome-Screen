package com.foursquare.examples.push.models;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * A record to keep track of what client ids follow a given venue. Allows us to quickly send a
 * fanned-out message to all of them.
 */
@PersistenceCapable
public class ChannelRecord {
  @PrimaryKey
  private String trackedVenue;
  
  @Persistent
  private List<String> clientIds;
  
  public ChannelRecord(String trackedVenue, List<String> clientIds) {
    this.trackedVenue = trackedVenue;
    this.clientIds = clientIds;
  }
  
  public ChannelRecord(String trackedVenue) {
    this.trackedVenue = trackedVenue;
    this.clientIds = new ArrayList<String>();
  }
  
  public String trackedVenue() { return trackedVenue; }
  
  public List<String> clientIds() { return clientIds; }
  public void addClient(String clientId) { clientIds.add(clientId); }
  public void dropClient(String clientId) { clientIds.remove(clientId); }
  
  public void save(PersistenceManager pm) {
    pm.makePersistent(this);
  }
  
  public static ChannelRecord loadOrCreate(PersistenceManager pm, String vid) {
    if (vid == null || vid.length() == 0) return null;
    
    try {
      return pm.getObjectById(ChannelRecord.class, vid);
    } catch (JDOObjectNotFoundException e) {
      return new ChannelRecord(vid);
    }
  }
}

package com.foursquare.examples.push.models;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * A record class to store google id -> foursquare auth token relationships.
 */
@PersistenceCapable
public class LinkedUser {
	@PrimaryKey
	private String googleId;

	@Persistent
	public String foursquareAuth;

	public LinkedUser(String googleId, String foursquareAuth) {
		this.googleId = googleId;
		this.foursquareAuth = foursquareAuth;
	}
	
	public String googleId() { return googleId; }
	public String foursquareAuth() { return foursquareAuth; }

	public void save(PersistenceManager pm) {
		pm.makePersistent(this);
	}
	
	public static LinkedUser loadOrCreate(PersistenceManager pm, String googid) {
    try {
      return pm.getObjectById(LinkedUser.class, googid);
    } catch (JDOObjectNotFoundException e) {
      return new LinkedUser(googid, null);
    }
  }
}

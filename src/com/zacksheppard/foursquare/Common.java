package com.zacksheppard.foursquare;

import java.util.Date;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import com.google.appengine.api.channel.ChannelMessage;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserServiceFactory;
import com.zacksheppard.foursquare.models.LinkedUser;

import fi.foyt.foursquare.api.FoursquareApi;

public class Common {
  private final static String CLIENT_ID = "YOUR CLIENT ID";
  private final static String CLIENT_SECRET = "YOUR CLIENT SECRET";
  public final static String PUSH_SECRET = "YOUR PUSH SECRET";
  private final static String CALLBACK = "YOUR CALLBACK URL";
  
  public static FoursquareApi getApi() { return getApi(null); }
  public static FoursquareApi getApi(String token) {
    FoursquareApi foursquareApi = new FoursquareApi(CLIENT_ID, CLIENT_SECRET, CALLBACK);
    if (token != null) foursquareApi.setoAuthToken(token);
    return foursquareApi;
  }
  public static FoursquareApi getCurrentApi(PersistenceManager pm) {
    User googler = getGoogleUser();
    if (googler != null) {
      LinkedUser luser = LinkedUser.load(pm, googler.getUserId());
      if (luser != null) {
        return getApi(luser.foursquareAuth());
      }
    }
    
    return null;
  }
  
  public static String getVenueName(String vid) {
    if (vid.equals("4ab7e57cf964a5205f7b20e3")) {
      return "foursquare hq";
    } else if (vid.equals("4d232f2f9822a090189effaf")) {
      return "Grind Spaces";
    } else {
      return null;
    }
  }
  
  // Convert the core aspects of a checkin to json
  public static String checkinToJson(String name, String photo, boolean isMayor) {
    StringBuilder sb = new StringBuilder();
    sb.append("{\"name\":\"");
    sb.append(name);
    sb.append("\",\"photo\":\"");
    sb.append(photo);
    sb.append("\",\"isMayor\":");
    sb.append(isMayor);
    sb.append("}");
    return sb.toString();
  }
  
  /* Methods for getting at aspects of persistence */
  private static final PersistenceManagerFactory pmfInstance =
      JDOHelper.getPersistenceManagerFactory("transactions-optional");

  private static PersistenceManagerFactory getPMF() {
    return pmfInstance;
  }
  
  public static PersistenceManager getPM() {
    return getPMF().getPersistenceManager();
  }
  
  /* Methods for user management tidbits */
  public static User getGoogleUser() {
    return UserServiceFactory.getUserService().getCurrentUser();
  }
  
  public static String getGoogleLoginUrl() {
    return UserServiceFactory.getUserService().createLoginURL("/#retryAuth");
  }
  
  public static String getFoursquareLoginUrl() {
    return "https://foursquare.com/oauth2/authenticate?client_id=" + CLIENT_ID +
           "&response_type=code&redirect_uri=" + CALLBACK;
  }
  
  /* Methods for handling Channel Client ID creation and understanding */
  public static String createChannelToken(String vid) {
    ChannelService cService = ChannelServiceFactory.getChannelService();
    return cService.createChannel(getClientId(vid));
  }
  
  private static String getClientId(String vid) {
    return vid + "-" + (new Date()).getTime();
  }
  
  public static String parseClientId(String clientId) {
    int ind = clientId.indexOf('-');
    if (ind > 0) {
      return clientId.substring(0, ind);
    } else return null;
  }
  
  // Actually handle sending out messages to a given list of clients
  public static void sendUpdate(List<String> clients, String message) {
    ChannelService cService = ChannelServiceFactory.getChannelService();
    
    for (String client : clients) {
      cService.sendMessage(new ChannelMessage(client, message));
    }
  }
}

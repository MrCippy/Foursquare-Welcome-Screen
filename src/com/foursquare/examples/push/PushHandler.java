package com.foursquare.examples.push;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;

import com.foursquare.examples.push.models.ChannelRecord;
import com.foursquare.examples.push.util.Common;

/**
 * The landing point for pushes from foursquare. Ensures that the push secret value is correct,
 * then parses the JSON and pushes the results out to the approrpiate clients.
 */
@SuppressWarnings("serial")
public class PushHandler extends HttpServlet {
  private static final Logger log = Logger.getLogger(PushHandler.class.getName());
  
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String pushSecret = req.getParameter("secret");
    String pushBody = req.getParameter("checkin");
    log.info("Push received: " + pushBody);
    if (pushSecret != null && pushBody != null && pushSecret.equals(Common.PUSH_SECRET)) {
      try {
        handlePush(pushBody);
      } catch (JSONException e) {
        log.warning("Had a terrible run-in with invalid JSON!\n" + e.getMessage()
                    + "\n" + pushBody);
      }
    }
  }
  
  // Parse the json and send the messages.
  private void handlePush(String pushBody) throws JSONException {
    JSONObject pushJson = new JSONObject(pushBody);
    
    // Read out the isMayor flag. This is so complicated since it could be not present if false.
    // If it isn't present, a JSON exception is thrown. So, we catch that and make it false.
    boolean isMayor = false;
    try {
      Boolean mayorness = pushJson.getBoolean("isMayor");
      isMayor = (mayorness != null && mayorness);
    } catch (JSONException e) { isMayor = false; }
    
    // Load in the required information. These will throw an exception if missing, which is OK
    // since we couldn't continue if any of them aren't there.
    String vid = pushJson.getJSONObject("venue").getString("id");
    JSONObject user = pushJson.getJSONObject("user");
    String name = user.getString("firstName");
    String photo = user.getString("photo");
    photo.replace("_thumbs", "");
    
    if (vid != null && name != null) {
      PersistenceManager pm = Common.getPM();
      try {
        List<String> targetClients = ChannelRecord.loadOrCreate(pm, vid).clientIds();
        Common.sendUpdate(targetClients, Common.checkinToJson(name, photo, isMayor));
      } finally {
        pm.close();
      }
    }
  }
}

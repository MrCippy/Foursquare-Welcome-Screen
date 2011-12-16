package com.foursquare.examples.push;

import java.io.IOException;
import java.util.logging.Logger;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.foursquare.examples.push.util.Common;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;
import fi.foyt.foursquare.api.Result;
import fi.foyt.foursquare.api.entities.Checkin;
import fi.foyt.foursquare.api.entities.CheckinGroup;

/**
 * An endpoint to get simplified information about the herenow for the target venue.
 */
@SuppressWarnings("serial")
public class GetHereNow extends HttpServlet {
  private static final Logger log = Logger.getLogger(GetHereNow.class.getName());
  
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    String targetVenue = req.getParameter("vid");
    if (targetVenue != null) {
      PersistenceManager pm = Common.getPM();
      try {
        FoursquareApi api = Common.getCurrentApi(pm);
        Checkin[] herenow = new Checkin[0];
        
        try {
          Result<CheckinGroup> res = api.venuesHereNow(targetVenue, 500, 0, 0L);
          if (res.getMeta().getCode() != 200) {
            log.warning("Failed to retrieve here now for venue "+targetVenue);
            log.info(res.getMeta().toString());
          } else {
            herenow = res.getResult().getItems();
          }
        } catch (FoursquareApiException e) {
          log.warning("Failed to retrieve here now for venue "+targetVenue);
          log.info(e.toString());
        }
        
        String channel = null;
        if (req.getParameter("channel") != null) {
          channel = Common.createChannelToken(targetVenue);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("{\"herenow\":" + herenow.length + ",");
        if (channel != null) {
          sb.append("\"channel\":\"" + channel + "\",");
        }
        sb.append("\"checkins\":[");
        for (int i = 0; i < herenow.length; i++) {
          if (i != 0) {
            sb.append(",");
          }
          
          boolean isMayor = herenow[i].getIsMayor() != null
                            && herenow[i].getIsMayor().equals(Boolean.TRUE);
          
          sb.append(Common.checkinToJson(herenow[i].getUser().getFirstName(),
                                         herenow[i].getUser().getPhoto(), isMayor));
        }
        sb.append("]}");
        resp.getWriter().print(sb.toString());
      } finally {
        pm.close();
      }
    }
  }
}

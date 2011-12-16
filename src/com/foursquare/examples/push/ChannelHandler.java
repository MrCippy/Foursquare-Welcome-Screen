package com.foursquare.examples.push;

import java.io.IOException;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.foursquare.examples.push.models.ChannelRecord;
import com.foursquare.examples.push.util.Common;
import com.google.appengine.api.channel.ChannelService;
import com.google.appengine.api.channel.ChannelServiceFactory;

@SuppressWarnings("serial")
public class ChannelHandler extends HttpServlet {
  /**
   * Handle Google App Engine's channel connect or disconnect messages
   * This updates the storage of what clients follow what venues so that we can fanout when a push
   * arrives for that venue.
   */
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    ChannelService channelService = ChannelServiceFactory.getChannelService();
    String clientId = channelService.parsePresence(req).clientId();
    
    if (clientId != null && clientId.length() > 0) {
      PersistenceManager pm = Common.getPM();      
      try {
        ChannelRecord rec = ChannelRecord.loadOrCreate(pm, Common.parseClientId(clientId));
        if (req.getPathInfo().contains("disconnected") && rec != null) {
          rec.dropClient(clientId);
          rec.save(pm);
        } else if (req.getPathInfo().contains("connected") && rec != null) {
          rec.addClient(clientId);
          rec.save(pm);
        }
      } finally {
        pm.close();
      }
    }
  }
}

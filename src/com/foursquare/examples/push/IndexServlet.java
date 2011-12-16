package com.foursquare.examples.push;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.foursquare.examples.push.models.LinkedUser;
import com.foursquare.examples.push.util.Common;
import com.google.appengine.api.users.User;

import fi.foyt.foursquare.api.FoursquareApi;
import fi.foyt.foursquare.api.FoursquareApiException;

/**
 * A servlet to dynamically create the HTML for the homepage.
 * Also enforces that the user is both logged in to Google and connected to Foursquare.
 */
@SuppressWarnings("serial")
public class IndexServlet extends HttpServlet {
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    PersistenceManager pm = Common.getPM();
    try {
      User googler = Common.getGoogleUser();
      if (googler == null) resp.sendRedirect(Common.getGoogleLoginUrl());
      else {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        
        FoursquareApi api = null;
        
        if (req.getPathInfo().contains("user")) {
          String code = req.getParameter("code");
          if (code != null) {
            api = Common.getApi();
            try {
              api.authenticateCode(code);
              if (api.getOAuthToken() != null) {
                LinkedUser u = LinkedUser.loadOrCreate(pm, Common.getGoogleUser().getUserId());
                u.foursquareAuth = api.getOAuthToken();
                u.save(pm);
              } else api = null;
            } catch (FoursquareApiException e) { api = null; }
          }
        } else api = Common.getCurrentApi(pm);
        
        if (api == null || api.getOAuthToken() == null || api.getOAuthToken().length() <= 0) {
          writeHead(out, "Connect to foursquare");
          writeConnectPrompt(out);
          writeFoot(out, null, null);
        } else {
          writeHead(out, "Welcome to "+Common.TARGET_VENUE_NAME);
          writePage(out, Common.TARGET_VENUE_NAME);
          writeFoot(out, Common.TARGET_VENUE, Common.createChannelToken(Common.TARGET_VENUE));
        }
      }
    } finally {
      pm.close();
    }
  }
  
  private void writeHead(PrintWriter out, String title) {
    out.println("<!doctype html><html>\n<head>\n<title>");
    out.println(title);
    out.println("</title>\n<link rel=\"stylesheet\" href=\"http://twitter.github.com/bootstrap/1.4.0/bootstrap.min.css\"/>");
    out.println("<link type=\"text/css\" rel=\"stylesheet\" media=\"all\" href=\"/static/style.css\"/>");
    out.println("<script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.0/jquery.min.js\"></script>");
    out.println("<script type=\"text/javascript\" src=\"/_ah/channel/jsapi\"></script>");
    out.println("<script type=\"text/javascript\" src=\"/static/script.js\"></script>");
    out.println("</head><body>\n<div id=\"wrapper\" class=\"container-fluid\">");
  }
  
  private void writePage(PrintWriter out, String placeName) {
    out.println("<div id=\"lastCheckin\"><div id=\"picWrapper\"><img id=\"userPic\" src=\"\"/><img id=\"userCrown\" src=\"/static/crown.png\"/></div>");
    out.println("<h1 id=\"message\"></h1></div>\n<div id=\"herenow\">");
    out.println("<div id=\"people\"></div><h2><span id=\"count\">0</span> here @ "+placeName+"</h2>\n</div>");
  }
  
  private void writeConnectPrompt(PrintWriter out) {
    out.println("<div id=\"connect\">\nTo use this widget, you need to connect to foursquare.<br />");
    out.print("<a href=\""+Common.getFoursquareLoginUrl());
    out.println("\"><img src=\"/static/connect-white.png\" /></a>\n</div>");
  }
  
  // If producing a working page, include the channel token right away to get going immediately.
  private void writeFoot(PrintWriter out, String vid, String token) {
    out.println("<script type=\"text/javascript\">");
    
    if (token != null) {
      out.println("channel = new goog.appengine.Channel('"+token+"');");
      out.println("socket = channel.open();\nsocket.onopen = onOpened;\nsocket.onmessage = onMessage;");
      out.println("socket.onerror = onError;\nsocket.onclose = onClose;");
    }
    if (vid != null) {
      out.println("var vid = '"+vid+"';");
    }
    
    out.println("</script>");

    out.print("</div></body></html>");
  }
}

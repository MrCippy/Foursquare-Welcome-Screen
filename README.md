Foursquare-powered Welcome Screen
=================================

This is the code for a foursquare-powered welcome screen for any venue. It is written in Java for
Google App Engine. Feel free to use it as an example of how to interact with the foursquare push API,
or just use it because it's a pretty rad welcome screen (we have it set up at the foursquare office).


To get it set up for your own venue:
------------------------------------

1.  Fork or download this code.
2.  Get a Google App Engine account and make a new application.
3.  Get a foursquare account and make a new oauth consumer (at [foursquare.com/oauth](http://foursquare.com/oauth, "Foursquare"))
4.  Update the appengine-web.xml file with your new application id.
5.  Update the com.foursquare.example.push.util.Common file with your new foursquare consumer's information. While there, also update this file with the venue you want to track.

    **Note:** You can only track venues that you are the foursquare manager of, or that is a home venue you created.

6.  Upload the code to your App Engine account, and it should work out of the box!
$(function() {
  loadHereNow(true);
});

// Fetch the whole herenow for a venue and load it in (1 time every 2 minutes)
var loadHereNow = function(firstTime) {
  // If the channel is closed, request the server to send us a new one
  var tackOn = '';
  if (!connected) { tackOn = '&channel=request'; }
  $.getJSON('/herenow?vid='+vid+tackOn, function(data) {
    // Handle opening the channel on our end if we just got a new one
    if (!(typeof(data.token) === 'undefined')) { refreshChannel(data.token); }
    
    if (firstTime) { updatePush(data.checkins[0]); }

    // Load all the contents in at once to avoid a cascade flicker
    var newContents = $('<div></div>');
    for (c in data.checkins) {
      var checkin = data.checkins[c]
      var personCard = $('<div class="person"></div>');
      personCard.append($('<img src="'+checkin.photo+'"/><div class="name">'+checkin.name+'</div>'));
      if (checkin.isMayor) {
        personCard.addClass('isMayor');
        personCard.prepend('<img id="userCrown" src="/static/crown.png"/>');
      }
      newContents.append(personCard)
    }
    
    $('#count').html(data.herenow + " people");
    $('#people').html(newContents);

    window.setTimeout(loadHereNow, 120000);
  })
}

// Load in the data from a new checkin
var updatePush = function(checkin) {
  $('#userPic').attr('src', checkin.photo);
  if (checkin.isMayor) {
    $('#picWrapper').addClass('isMayor');
    $('#message').html('All hail his royal highness, <span class="name">'
        + checkin.name + '</span>!');
  } else {
    $('#picWrapper').removeClass('isMayor');
    $('#message').html('Everyone say hi to your new friend, <span class="name">'
        + checkin.name + '</span>. Clap, clap!');
  }
}

// Handle opening a new channel in the event that it needs to be refreshed on the fly
var refreshChannel = function(token) {
  channel = new goog.appengine.Channel(token);
  socket = channel.open();
  socket.onopen = onOpened;
  socket.onmessage = onMessage;
  socket.onerror = onError;
  socket.onclose = onClose;
}

// Channel business
var connected = false;
var onMessage = function(data) { updatePush($.parseJSON(data.data)); }
var onOpened = function() { connected = true; }
var onClose = function() { connected = false; }
var onError = function() {
  socket.close();
  connected = false;
}

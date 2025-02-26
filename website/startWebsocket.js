var socket = new WebSocket('ws://localhost:9090');

socket.onopen = function(event) {
  // Handle connection open
};

socket.onmessage = function(event) {
    console.log(event);
};

socket.onclose = function(event) {
  // Handle connection close
};

function sendMessage(message) {
  socket.send(message);
}
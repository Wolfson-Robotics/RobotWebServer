var socket = new WebSocket('wss://localhost:8080');

socket.onopen = function(event) {
  // Handle connection open
};

socket.onmessage = function(event) {
    console.log(event)
};

socket.onclose = function(event) {
  // Handle connection close
};

function sendMessage(message) {
  socket.send(message);
}
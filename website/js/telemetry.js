(async function telemetry() {

    const telemetrySocket = window.startSocket(window.getEndpoint("telemetry"));
    telemetrySocket.onmessage = (e) =>
        document.getElementById("telemetry").value = e.data;
    window.aliveSocket(telemetrySocket);
    window.persistSocket(telemetrySocket, telemetry);

})();
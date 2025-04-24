(async function telemetry() {

    const telemetrySocket = window.startSocket("/robot/telemetry");
    telemetrySocket.onmessage = (e) =>
        document.getElementById("telemetry").value = e.data;
    window.aliveSocket(telemetrySocket);
    window.persistSocket(telemetrySocket, telemetry);

})();
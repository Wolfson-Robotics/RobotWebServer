(async() => {

    const telemetrySocket = window.startSocket(`robot/telemetry`);
    telemetrySocket.onmessage = (e) =>
        document.getElementById("telemetry").value = e.data;
    window.aliveSocket(telemetrySocket);

})();
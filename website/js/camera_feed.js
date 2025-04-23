(async() => {

    function init() {

        const cameraSocket = window.startSocket("/robot/camera_feed");
        cameraSocket.binaryType = "arraybuffer";

        // We want to send to get feed instead of feed being sent to us to ensure synchronicity
        cameraSocket.onopen = () => {
            cameraSocket.send("req_full_image");
        }

        cameraSocket.onmessage = (e) => {

            const arrayBuffer = e.data;
            const blob = new Blob([arrayBuffer], { type: "image/jpeg" });
            const imageUrl = URL.createObjectURL(blob);
            const img = document.getElementById("cameraimg");
            img.src = imageUrl;

            cameraSocket.send("");

        };
        document.getElementById("camera-feed").style.display = "block";
        return cameraSocket;

    }
    function stop(cameraSocket) {
        cameraSocket.close(1000, "Closing camera feed");
        document.getElementById("camera-feed").style.display = "none";
    }


    let currSocket;
    let shown = window.localStorage.getItem("camera_feed_shown") ?? true;
    function update() {
        window.localStorage.setItem("camera_feed_shown", shown);
        if (shown) {
            currSocket = init();
        }
        if (!shown) stop(currSocket);
    }

    document.getElementById("camera-toggle").addEventListener("click", () => {
        shown = !shown;
        update();
    });
    update();

})();


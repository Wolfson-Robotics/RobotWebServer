(async() => {

    const feed = document.getElementById("camera-feed")


    function init() {

        const cameraSocket = window.startSocket(window.getEndpoint("cameraFeed"));
        cameraSocket.binaryType = "arraybuffer";

        // We want to send to get feed instead of feed being sent to us to ensure synchronicity
        cameraSocket.onopen = () => {
            cameraSocket.send("req_full_image");
        };
        cameraSocket.onmessage = async(e) => {

            const arrayBuffer = e.data;
            feed.src = URL.createObjectURL(new Blob([arrayBuffer], { type: "image/jpeg" }));
            cameraSocket.send("");

        };
        window.persistSocket(cameraSocket, init);

        feed.style.display = "block";
        return cameraSocket;

    }

    function stop(cameraSocket) {
        cameraSocket.close(1000, "Closing camera feed");
        feed.style.display = "none";
    }


    let currSocket;
    let shown = window.localStorage.getItem("camera_feed_shown") === "true" ?? true;
    function update() {
        window.localStorage.setItem("camera_feed_shown", shown);
        if (shown) currSocket = init();
        if (!shown) stop(currSocket);
    }

    document.getElementById("camera-toggle").addEventListener("click", () => {
        shown = !shown;
        update();
    });
    update();

})();


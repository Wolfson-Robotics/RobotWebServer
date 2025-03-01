const cameraSocket = new WebSocket("ws://localhost:9090/robot/camera_feed");
cameraSocket.binaryType = "arraybuffer";

cameraSocket.onopen = (e) => {
    cameraSocket.binaryType = "arraybuffer";
    cameraSocket.send("req_full_image");
}

/*Possibly switch over to using <video> tag that recieves packets overtime*/
//TODO: implement multiple camera feeds
cameraSocket.onmessage = (e) => {

    const arrayBuffer = e.data;
    const blob = new Blob([arrayBuffer], { type: "image/jpeg" });
    const imageUrl = URL.createObjectURL(blob);
    const img = document.getElementById("cameraimg");
    img.src = imageUrl;

    cameraSocket.send("data");

};
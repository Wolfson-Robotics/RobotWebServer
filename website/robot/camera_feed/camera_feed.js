const cameraSocket = new WebSocket("ws://localhost:9090/robot/camera_feed");
cameraSocket.binaryType = "arraybuffer";

cameraSocket.onopen = (e) => {
    cameraSocket.binaryType = "arraybuffer";
    cameraSocket.send("req_full_image");
}

// TODO: Make this dynamic
let width = 1920;
let height = 1080;
cameraSocket.onmessage = (e) => {

    const rawMatData = new Uint8Array(e.data);
    const rgbBytes = new Uint8ClampedArray(width * height * 4);

    // OpenCV stores the pixels as BGR instead of RGB, reverse it
    for (let i = 0, j = 0; i < rawMatData.length; i += 3, j += 4) {
        // Prevent floating point errors
        i = Math.round(i);
        j = Math.round(j);
        rgbBytes[j] = rawMatData[i + 2];
        rgbBytes[j+1] = rawMatData[i + 1];
        rgbBytes[j+2] = rawMatData[i];
        rgbBytes[j+3] = 255;
    }

    const canvas = document.querySelector("canvas");
    canvas.width = width;
    canvas.height = height;
    canvas.getContext("2d").putImageData(new ImageData(rgbBytes, width, height), 0, 0);
    // canvas.style.width = "40%";
    // canvas.style.height = "40%";
    cameraSocket.send("data");

};
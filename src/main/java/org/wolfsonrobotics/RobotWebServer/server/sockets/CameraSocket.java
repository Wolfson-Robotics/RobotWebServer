package org.wolfsonrobotics.RobotWebServer.server.sockets;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import nu.pattern.OpenCV;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.wolfsonrobotics.RobotWebServer.ServerConfig;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;

import java.io.IOException;


public class CameraSocket extends BaseSocket {

    public CameraSocket(NanoHTTPD.IHTTPSession handshakeRequest, CommunicationLayer commLayer) {
        super(handshakeRequest, commLayer);
    }

    // Don't keep messaging so that the client can ensure synchronicity with the video feed (see camera_feed.js)
    @Override
    protected void onOpen() {
        super.onOpen();
        OpenCV.loadLocally();
    }

    // We want to make the updating of the camera feed only on message to ensure
    // synchronization and smooth, continuous feed on the client side, as absolute
    // concurrency doesn't really matter for our purposes
    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {

        Mat cameraFeed = (Mat) commLayer.callNoThrows(ServerConfig.CAMERA_FEED_METHOD);
        MatOfByte buffer = new MatOfByte();
        boolean success = Imgcodecs.imencode(".jpg", cameraFeed, buffer);

        if (success) {
            send(buffer.toArray().clone());
        } else {
            try {
                send("Failed to send webcam data");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }



}
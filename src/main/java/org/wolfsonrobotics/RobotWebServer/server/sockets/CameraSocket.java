package org.wolfsonrobotics.RobotWebServer.server.sockets;

import java.io.FileOutputStream;
import java.io.IOException;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import nu.pattern.OpenCV;


public class CameraSocket extends BaseSocket {

    private FakeRobot robot;

    public CameraSocket(NanoHTTPD.IHTTPSession handshakeRequest) {
        super(handshakeRequest);
    }

    @Override
    protected void onOpen() {
        super.onOpen();
        OpenCV.loadLocally();
        this.robot = new FakeRobot();
    }

    // We want to make the updating of the camera feed only on message to ensure
    // synchronization and smooth, continuous feed on the client side, as absolute
    // concurrency doesn't really matter for our purposes
    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {
        super.onMessage(message);

        Mat cameraFeed = robot.getCameraFeed();
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
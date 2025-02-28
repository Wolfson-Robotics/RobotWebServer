package org.wolfsonrobotics.RobotWebServer.server.sockets;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import nu.pattern.OpenCV;

import java.io.IOException;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;


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

        // TODO: Probably move all this to its own class later
        // TODO: Make it automatically update without need for future requests
        Mat cameraFeed = robot.getCameraFeed();
        /* byte[] fullMatBytes = new byte[(int) (cameraFeed.total() * cameraFeed.elemSize())];
        cameraFeed.get(0, 0, fullMatBytes); */
        MatOfByte matOfByte = new MatOfByte();
        Imgcodecs.imencode(".jpg", cameraFeed, matOfByte);
        try {
            send(matOfByte.toArray().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
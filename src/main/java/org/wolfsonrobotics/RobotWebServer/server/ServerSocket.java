package org.wolfsonrobotics.RobotWebServer.server;

import java.io.IOException;
import java.net.SocketTimeoutException;

import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;
import nu.pattern.OpenCV;

public class ServerSocket extends WebSocket {

    public ServerSocket(IHTTPSession handshakeRequest) {
        super(handshakeRequest);
    }

    @Override
    protected void onOpen() {
        System.out.println("Websocket Opened");
    }

    @Override
    protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
        System.out.println("Websocket Closed");
    }

    @Override
    protected void onMessage(WebSocketFrame message) {
        OpenCV.loadLocally();
        try {
            message.setUnmasked();
            System.out.println("Received message: " + message.getTextPayload());
            //sendFrame(message);
            FakeRobot robot = new FakeRobot();
            send(robot.stringifyMat(robot.getCameraFeed()));
        } catch (IOException e) {
            if (e.getMessage().trim().toLowerCase().contains("connection reset by peer")) {
                System.out.println("Peer closed connection");
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void onException(IOException exception) {
        if (exception instanceof SocketTimeoutException) {
            System.out.println("Websocked timed out");
            try {
                close(CloseCode.GoingAway, "Timed out", false);
            } catch (IOException e) {
                onException(exception);
            }
        } else {
            exception.printStackTrace();
        }
    }

    @Override
    protected void onPong(WebSocketFrame pong) {
        try {
            send(pong.getTextPayload());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    


}

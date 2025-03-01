package org.wolfsonrobotics.RobotWebServer.server;

import fi.iki.elonen.NanoWSD;
import org.wolfsonrobotics.RobotWebServer.server.sockets.CameraSocket;
import org.wolfsonrobotics.RobotWebServer.server.sockets.DummySocket;

public class HandleSocket extends NanoWSD {

    public HandleSocket(int port) {
        super(port);
    }
    
    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        switch (handshake.getUri()) {
            case "/robot/camera_feed":
                return new CameraSocket(handshake);
            default:
                return new DummySocket(handshake);
        }
    }
    
}

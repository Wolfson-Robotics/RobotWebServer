package org.wolfsonrobotics.RobotWebServer.server;

import fi.iki.elonen.NanoWSD;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.sockets.CameraSocket;
import org.wolfsonrobotics.RobotWebServer.server.sockets.DeviceInfoSocket;
import org.wolfsonrobotics.RobotWebServer.server.sockets.DummySocket;

public class HandleSocket extends NanoWSD {

    private final CommunicationLayer commLayer;

    public HandleSocket(int port, CommunicationLayer commLayer) {
        super(port);
        this.commLayer = commLayer;
    }
    
    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        switch (handshake.getUri()) {
            case "/robot/camera_feed":
                return new CameraSocket(handshake, commLayer);
            case "/robot/device_info":
                return new DeviceInfoSocket(handshake, commLayer);
            default:
                return new DummySocket(handshake, commLayer);
        }
    }
    
}

package org.wolfsonrobotics.RobotWebServer.server.sockets;


import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;

public class DummySocket extends BaseSocket {

    public DummySocket(IHTTPSession handshakeRequest, CommunicationLayer commLayer) {
        super(handshakeRequest, commLayer);
    }

}

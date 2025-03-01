package org.wolfsonrobotics.RobotWebServer.server.sockets;


import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class DummySocket extends BaseSocket {

    public DummySocket(IHTTPSession handshakeRequest) {
        super(handshakeRequest);
    }

}

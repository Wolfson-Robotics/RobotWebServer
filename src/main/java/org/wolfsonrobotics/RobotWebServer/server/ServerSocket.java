package org.wolfsonrobotics.RobotWebServer.server;


import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import org.wolfsonrobotics.RobotWebServer.server.sockets.BaseSocket;

public class ServerSocket extends BaseSocket {

    public ServerSocket(IHTTPSession handshakeRequest) {
        super(handshakeRequest);
    }

}

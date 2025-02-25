package org.wolfsonrobotics.RobotWebServer.server;

import fi.iki.elonen.NanoWSD;

public class HandleSocket extends NanoWSD {

    public HandleSocket(int port) {
            super(port);
        }
    
        @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        return new ServerSocket(handshake);
    }
    
}

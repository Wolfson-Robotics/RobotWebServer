package org.wolfsonrobotics.RobotWebServer.server;

import java.io.IOException;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

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
        try {
            message.setUnmasked();
            System.out.println(message.getTextPayload());
            sendFrame(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onException(IOException exception) {
        exception.printStackTrace();
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

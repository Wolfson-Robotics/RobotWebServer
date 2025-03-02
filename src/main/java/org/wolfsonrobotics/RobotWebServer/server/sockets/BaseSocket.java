package org.wolfsonrobotics.RobotWebServer.server.sockets;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoWSD.WebSocket;

import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;

import java.io.IOException;
import java.net.SocketTimeoutException;

public abstract class BaseSocket extends WebSocket {


    public BaseSocket(IHTTPSession handshakeRequest) {
        super(handshakeRequest);
    }


    @Override
    protected void onOpen() {
        System.out.println("Websocket opened");
    }

    @Override
    protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
        System.out.println("Websocket closed");
    }

    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {
        message.setUnmasked();
        //System.out.println("Received message: " + message.getTextPayload());
    }

    @Override
    protected void onException(IOException exception) {

        if (exception instanceof SocketTimeoutException) {
            System.out.println("Websocked timed out");
            try {
                close(NanoWSD.WebSocketFrame.CloseCode.GoingAway, "Timed out", false);
            } catch (IOException e) {
                onException(exception);
            }
        } else {
            exception.printStackTrace();
        }

    }

    @Override
    protected void onPong(NanoWSD.WebSocketFrame pong) {
        try {
            send(pong.getTextPayload());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void send(byte[] data) {
        try {
            super.send(data);
        } catch (IOException e) {
            if (e.getMessage().trim().toLowerCase().contains("connection reset by peer")) {
                System.out.println("Peer closed connection");
            } else {
                throw new RuntimeException(e);
            }
        }
    }

}

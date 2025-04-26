package org.wolfsonrobotics.RobotWebServer.server.sockets;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;
import org.wolfsonrobotics.RobotWebServer.ServerConfig;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class BaseSocket extends WebSocket {

    protected final CommunicationLayer commLayer;
    private final ScheduledExecutorService ticker = Executors.newSingleThreadScheduledExecutor();

    public BaseSocket(IHTTPSession handshakeRequest, CommunicationLayer commLayer) {
        super(handshakeRequest);
        this.commLayer = commLayer;
    }

    protected void keepRunning(Runnable fn, long ms) {
        ticker.scheduleAtFixedRate(fn, 0, ms, TimeUnit.MILLISECONDS);
    }
    protected void keepRunning(Runnable fn) {
        keepRunning(fn, ServerConfig.DEFAULT_SOCKET_MSG_FREQUENCY);
    }
    protected void keepMessaging() {
        this.keepRunning(this::onMessage);
    }


    protected void closeError(String msg) {
        try {
            close(CloseCode.InternalServerError, "An error occurred: " + msg, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    protected void closeError(Exception e) {
        closeError(e.getMessage());
    }
    protected void closeError() {
        closeError("");
    }



    @Override
    protected void onOpen() {
        System.out.println("Websocket opened");
    }

    @Override
    protected void onClose(CloseCode code, String reason, boolean initiatedByRemote) {
        System.out.println("Websocket closed");
        ticker.shutdownNow();
    }

    @Override
    protected void onMessage(NanoWSD.WebSocketFrame message) {
        message.setUnmasked();
        //System.out.println("Received message: " + message.getTextPayload());
    }

    /**
     * Designed for sockets that do not analyze messages, and are instead simply
     * supposed to execute an independent action on that event.
     */
    protected void onMessage() {
        this.onMessage(null);
    }

    @Override
    protected void onException(IOException exception) {

        if (exception instanceof SocketTimeoutException) {
            System.out.println("Websocket timed out");
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

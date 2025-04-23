package org.wolfsonrobotics.RobotWebServer.server.sockets;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoWSD;
import fi.iki.elonen.NanoWSD.WebSocket;
import fi.iki.elonen.NanoWSD.WebSocketFrame.CloseCode;
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

    protected void keepRunning(Runnable fn) {
        ticker.scheduleAtFixedRate(() -> this.onMessage(null), 0, 1, TimeUnit.SECONDS);
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

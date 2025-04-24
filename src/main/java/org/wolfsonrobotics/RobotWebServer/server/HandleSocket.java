package org.wolfsonrobotics.RobotWebServer.server;

import fi.iki.elonen.NanoWSD;
import org.wolfsonrobotics.RobotWebServer.ServerConfig;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.sockets.BaseSocket;
import org.wolfsonrobotics.RobotWebServer.server.sockets.DummySocket;

import java.util.Map;
import java.util.Optional;

public class HandleSocket extends NanoWSD {

    private final CommunicationLayer commLayer;

    public HandleSocket(int port, CommunicationLayer commLayer) {
        super(port);
        this.commLayer = commLayer;
    }
    
    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {

        // Separately declare Optional since DummySocket.class cannot be directly fed into
        // orElse, since the Java compiler cannot guarantee the type
        Optional<? extends Class<? extends BaseSocket>> socketType = ServerConfig.socketMap.entrySet().stream()
                .filter(e -> handshake.getUri().toLowerCase().contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();

        if (!socketType.isPresent()) {
            return new DummySocket(handshake, commLayer);
        }

        try {
            return socketType.get()
                    .getConstructor(IHTTPSession.class, CommunicationLayer.class)
                    .newInstance(handshake, commLayer);
        } catch (Exception e) {
            return new DummySocket(handshake, commLayer);
        }

    }
    
}

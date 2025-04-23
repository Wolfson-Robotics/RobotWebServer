package org.wolfsonrobotics.RobotWebServer.server;

import fi.iki.elonen.NanoWSD;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.sockets.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HandleSocket extends NanoWSD {

    private final CommunicationLayer commLayer;
    private final Map<String, Class<? extends BaseSocket>> socketMap = new HashMap<>();

    public HandleSocket(int port, CommunicationLayer commLayer) {
        super(port);
        this.commLayer = commLayer;

        this.socketMap.put("robot/camera_feed", CameraSocket.class);
        this.socketMap.put("robot/device_info", DeviceInfoSocket.class);
        this.socketMap.put("robot/telemetry", TelemetrySocket.class);
        this.socketMap.put("dummy", DummySocket.class);
    }
    
    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {

        // Separately declare Optional since DummySocket.class cannot be directly fed into
        // orElse, since the Java compiler cannot guarantee the type
        Optional<? extends Class<? extends BaseSocket>> socketType = socketMap.entrySet().stream()
                .filter(e -> handshake.getUri().toLowerCase().contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();

        try {
            return (socketType.isPresent() ? socketType.get() : socketMap.get("/dummy"))
                    .getConstructor(IHTTPSession.class, CommunicationLayer.class)
                    .newInstance(handshake, commLayer);
        } catch (Exception e) {
            return new DummySocket(handshake, commLayer);
        }

    }
    
}

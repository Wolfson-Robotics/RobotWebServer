package org.wolfsonrobotics.RobotWebServer.server.sockets;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;

import java.util.List;

public class TelemetrySocket extends BaseSocket {

    private CommunicationLayer telemetry;

    public TelemetrySocket(NanoHTTPD.IHTTPSession handshakeRequest, CommunicationLayer commLayer) {
        super(handshakeRequest, commLayer);
    }

    @Override
    public void onOpen() {
        super.onOpen();
        try {
            this.telemetry = commLayer.getFieldLayer("telemetry");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            this.closeError();
            return;
        }
        this.keepMessaging();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onMessage(NanoWSD.WebSocketFrame message) {
        try {
            send(String.join("\n", ((List<String>) telemetry.getFieldLayer("log").getField("entries"))));
        } catch (Exception e) {
            this.closeError(e);
        }
    }

}

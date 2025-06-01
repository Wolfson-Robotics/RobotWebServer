package org.wolfsonrobotics.RobotWebServer.server.api;

import fi.iki.elonen.NanoHTTPD;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;

public abstract class RobotAPI extends BaseAPI {
    
    protected final CommunicationLayer commLayer;

    protected RobotAPI(NanoHTTPD.IHTTPSession session, CommunicationLayer commLayer) {
        super(session);
        this.commLayer = commLayer;
    }

}
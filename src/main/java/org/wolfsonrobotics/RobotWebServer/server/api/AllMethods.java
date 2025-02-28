package org.wolfsonrobotics.RobotWebServer.server.api;

import org.wolfsonrobotics.RobotWebServer.communication.*;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class AllMethods implements BaseAPI {
    
    private CommunicationLayer comLayer;

    public AllMethods(CommunicationLayer comLayer) {
        this.comLayer = comLayer;
    }

    @Override
    public void handle(IHTTPSession session) {
        //TODO:
        return;
    }
    
}

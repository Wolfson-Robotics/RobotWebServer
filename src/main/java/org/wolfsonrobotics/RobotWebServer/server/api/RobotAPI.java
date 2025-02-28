package org.wolfsonrobotics.RobotWebServer.server.api;

import org.wolfsonrobotics.RobotWebServer.communication.*;

public abstract class RobotAPI implements BaseAPI {
    
    private final CommunicationLayer comLayer;

    protected RobotAPI(CommunicationLayer comLayer) {
        //TODO: implement
        this.comLayer = comLayer;
        return;
    }

}

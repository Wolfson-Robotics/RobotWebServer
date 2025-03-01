package org.wolfsonrobotics.RobotWebServer.server.api;

import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.APIException;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class CameraFeed extends RobotAPI {

    protected CameraFeed(IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
    }

    @Override
    public String handle() throws APIException {
        StringBuilder s = new StringBuilder();
            s.append("<html>");
            s.append("<head><script src=\"/camera_feed.js\"></script></head>");
            s.append("<body><img id=\"cameraimg\"></body>");
            s.append("</html>");
        System.out.println("Code ran here");
        return s.toString();
    }
    
}

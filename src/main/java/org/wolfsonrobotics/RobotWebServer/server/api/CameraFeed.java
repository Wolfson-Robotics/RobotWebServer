package org.wolfsonrobotics.RobotWebServer.server.api;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;

public class CameraFeed extends RobotAPI {

    public CameraFeed(IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
        responseType = NanoHTTPD.MIME_HTML;
    }

    /*todo make this work for multiple camera feeds */
    @Override
    public String handle() {
        StringBuilder s = new StringBuilder();
            s.append("<html>");
            s.append("<head><script src=\"/camera_feed.js\"></script></head>");
            s.append("<body><img id=\"cameraimg\"></body>");
            s.append("</html>");
        return s.toString();
    }
    
}

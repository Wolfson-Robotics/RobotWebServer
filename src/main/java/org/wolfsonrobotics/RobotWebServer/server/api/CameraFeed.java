package org.wolfsonrobotics.RobotWebServer.server.api;

import javax.activation.MimeType;

import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.APIException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class CameraFeed extends RobotAPI {

    public CameraFeed(IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
        mimeType = NanoHTTPD.MIME_HTML;
    }

    /*todo make this work for multiple camera feeds */
    @Override
    public String handle() throws APIException {
        StringBuilder s = new StringBuilder();
            s.append("<html>");
            s.append("<head><script src=\"/camera_feed.js\"></script></head>");
            s.append("<body><img id=\"cameraimg\"></body>");
            s.append("</html>");
        return s.toString();
    }
    
}

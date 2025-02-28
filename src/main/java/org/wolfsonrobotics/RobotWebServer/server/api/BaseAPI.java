package org.wolfsonrobotics.RobotWebServer.server.api;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public interface BaseAPI {
    void handle(IHTTPSession session);
}

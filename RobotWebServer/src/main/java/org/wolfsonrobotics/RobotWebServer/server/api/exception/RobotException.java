package org.wolfsonrobotics.RobotWebServer.server.api.exception;

import fi.iki.elonen.NanoHTTPD;

public class RobotException extends APIException {

    public RobotException(Exception e) {
        super(e);
    }

    @Override
    public NanoHTTPD.Response.Status getStatus() {
        return NanoHTTPD.Response.Status.INTERNAL_ERROR;
    }

}

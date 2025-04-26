package org.wolfsonrobotics.RobotWebServer.server.api.exception;

import fi.iki.elonen.NanoHTTPD;

public class BadInputException extends APIException {

    public BadInputException(String message) {
        super(message);
    }

    @Override
    public NanoHTTPD.Response.Status getStatus() {
        return NanoHTTPD.Response.Status.BAD_REQUEST;
    }

}

package org.wolfsonrobotics.RobotWebServer.server.api.exception;

import fi.iki.elonen.NanoHTTPD;

public class MalformedRequestException extends APIException {

    public MalformedRequestException(String msg) {
        super(msg);
    }
    public MalformedRequestException(Exception e) {
        super("A malformed request was sent.", e);
    }
    public MalformedRequestException(String msg, Exception e) {
        super(msg, e);
    }

    @Override
    public NanoHTTPD.Response.Status getStatus() {
        return NanoHTTPD.Response.Status.BAD_REQUEST;
    }

}

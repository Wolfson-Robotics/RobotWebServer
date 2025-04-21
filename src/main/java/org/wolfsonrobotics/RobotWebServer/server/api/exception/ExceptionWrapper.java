package org.wolfsonrobotics.RobotWebServer.server.api.exception;

import fi.iki.elonen.NanoHTTPD;

public class ExceptionWrapper extends APIException {

    public ExceptionWrapper(Exception e) {
        super(e);
    }

    @Override
    public NanoHTTPD.Response.Status getStatus() {
        if (getCause() instanceof IllegalAccessException) {
            return NanoHTTPD.Response.Status.FORBIDDEN;
        }
        return NanoHTTPD.Response.Status.INTERNAL_ERROR;
    }

}

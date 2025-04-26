package org.wolfsonrobotics.RobotWebServer.server.api.exception;

import org.wolfsonrobotics.RobotWebServer.util.GsonHelper;
import static fi.iki.elonen.NanoHTTPD.Response;


public abstract class APIException extends Exception {

    // Manually specify super constructors here so that APIException children
    // can access the super constructors of Exception
    protected APIException(String msg) {
        super(msg);
    }
    protected APIException(Exception e) {
        super(e);
    }
    protected APIException(String msg, Exception e) {
        super(msg, e);
    }


    public abstract Response.Status getStatus();

    @Override
    public String getMessage() {
        return GsonHelper.singletonObj("error", super.getMessage()).toString();
    }

}

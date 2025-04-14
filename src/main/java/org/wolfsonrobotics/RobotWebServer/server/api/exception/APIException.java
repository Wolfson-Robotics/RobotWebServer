package org.wolfsonrobotics.RobotWebServer.server.api.exception;

import org.json.JSONObject;

public class APIException extends Exception {

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

    public static APIException wrap(Exception e) {
        return new APIException(e);
    }

    @Override
    public String getMessage() {
        JSONObject errorRes = new JSONObject();
        errorRes.put("error", super.getMessage());
        return errorRes.toString();
    }

}

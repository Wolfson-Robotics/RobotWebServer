package org.wolfsonrobotics.RobotWebServer.server.api;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.APIException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseAPI {

    protected final IHTTPSession session;

    protected BaseAPI(IHTTPSession session) {
        this.session = session;
    }


    public abstract String handle() throws APIException;


    public Map<String, String> parsePOST() throws NanoHTTPD.ResponseException, IOException {
        Map<String, String> reqBody = new HashMap<>();
        session.parseBody(reqBody);
        return reqBody;
    }

    // Common methods
    public JSONObject parseJSONBody(IHTTPSession session) throws NanoHTTPD.ResponseException, IOException {
        return new JSONObject(this.parsePOST().get("postData"));
    }

}
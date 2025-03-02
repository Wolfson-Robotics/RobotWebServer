package org.wolfsonrobotics.RobotWebServer.server.api;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import org.json.JSONException;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.APIException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.MalformedRequestException;

import java.util.*;

public abstract class BaseAPI {

    protected final IHTTPSession session;
    // Instance variables
    protected JSONObject body;
    public String responseType = "text/plain; charset=utf-8";


    protected BaseAPI(IHTTPSession session) {
        this.session = session;
    }

    public abstract String handle() throws APIException;


    protected void commonHandle() throws MalformedRequestException {

        if (session.getMethod().equals(NanoHTTPD.Method.POST)) {

            if (!session.getHeaders().getOrDefault("content-type", "").equalsIgnoreCase("application/json")) {
                throw new MalformedRequestException("Content-Type header must be application/json");
            }

            Map<String, String> reqBody = new HashMap<>();
            try {
                session.parseBody(reqBody);
            } catch (Exception e) {
                throw new MalformedRequestException("Could not parse body", e);
            }
            if (!reqBody.containsKey("postData")) {
                throw new MalformedRequestException("No body sent");
            }

            try {
                this.body = new JSONObject(reqBody.get("postData"));
            } catch (JSONException e) {
                throw new MalformedRequestException("Body sent is not proper JSON");
            }

        }

    }


}
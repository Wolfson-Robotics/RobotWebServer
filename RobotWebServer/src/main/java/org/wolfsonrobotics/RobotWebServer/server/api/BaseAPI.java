package org.wolfsonrobotics.RobotWebServer.server.api;

import com.google.gson.JsonObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.APIException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.BadInputException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.MalformedRequestException;
import org.wolfsonrobotics.RobotWebServer.util.GsonHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class BaseAPI {

    protected final IHTTPSession session;
    // Instance variables
    protected JsonObject body;
    protected Map<String, String> parameters;

    protected String responseType = "text/plain; charset=utf-8";


    protected BaseAPI(IHTTPSession session) {
        this.session = session;
    }

    public abstract String handle() throws APIException;


    protected void commonHandle() throws MalformedRequestException {

        switch (session.getMethod()) {
            case POST: {

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

                this.body = GsonHelper.getPostPayloadJSON(reqBody);
                break;

            }
            case GET: {
                Map<String, List<String>> rawParameters = session.getParameters();
                if (rawParameters.values().stream().anyMatch(l -> l.size() > 1)) {
                    throw new MalformedRequestException("Multiple values cannot be provided for a single query string parameter");
                }
                this.parameters = rawParameters.entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().get(0)
                ));
                break;
            }

        }

    }


    public <T> T getBody(String key, Class<T> type) throws BadInputException {
        if (!(body.has(key))) {
            throw new BadInputException("No " + key + " specified");
        }
        if (!type.isInstance(body.get(key))) {
            throw new BadInputException("Provided " + key + " must be of type " + type.getSimpleName());
        }
        return type.cast(body.get(key));
    }
    public String getBody(String key) throws BadInputException {
        return getBody(key, String.class);
    }
    public String getResponseType() {
        return this.responseType;
    }


    // Generic responses
    public String success() {
        return GsonHelper.singletonObj("message", "Success").toString();
    }


}
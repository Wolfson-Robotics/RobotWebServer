package org.wolfsonrobotics.RobotWebServer.server.api;

import fi.iki.elonen.NanoHTTPD;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.BadInputException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.RobotException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.MalformedRequestException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CallMethod extends RobotAPI {

    public CallMethod(NanoHTTPD.IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
    }

    @Override
    public String handle() throws BadInputException, MalformedRequestException, RobotException {

        if (!session.getMethod().equals(NanoHTTPD.Method.POST)) {
            throw new MalformedRequestException("Only acceptable method is POST");
        }
        String contentType = session.getHeaders().getOrDefault("content-type", "");
        if (!contentType.equalsIgnoreCase("application/json")) {
            throw new MalformedRequestException("Content-Type header must be application/json");
        }

        // TODO: Implement better and more generalized and abstracted JSON and POST body handling
        JSONObject inputJSON;
        try {
            inputJSON = parseJSONBody(session);
        } catch (Exception e) {
            throw new MalformedRequestException("No body was sent or the body was malformed", e);
        }

        if (!inputJSON.has("name")) {
            throw new BadInputException("No method name specified");
        }
        if (!inputJSON.has("args")) {
            throw new BadInputException("No method args were specified");
        }
        if (!(inputJSON.get("name") instanceof String)) {
            throw new BadInputException("The method name must be of type string");
        }
        if (!(inputJSON.get("args") instanceof JSONArray)) {
            throw new BadInputException("The method arguments must be of type array");
        }


        JSONArray args = inputJSON.getJSONArray("args");
        List<Object> argTypes = IntStream.range(0, args.length())
                .mapToObj(args::get)
                .collect(Collectors.toList());

        try {
            this.comLayer.call(inputJSON.getString("name"), argTypes);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new BadInputException("The method with the specified arguments either does not exist or cannot be called");
        } catch (InvocationTargetException e) {
            throw new RobotException(e);
        }

        return "{\"message\":\"Success\"}";
    }
}

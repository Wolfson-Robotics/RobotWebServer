package org.wolfsonrobotics.RobotWebServer.server.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.RobotWebServer;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.BadInputException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.MalformedRequestException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.RobotException;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class RobotInfo extends RobotAPI {
    
    public RobotInfo(IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
        mimeType = "application/json";
    }

    @Override
    public String handle() throws BadInputException, MalformedRequestException, RobotException {
        TreeMap<String, ArrayList<String>> query = RobotWebServer.parseQueryParameters(session);

        if (query == null) { return null; }

        if (query.get("get") != null) {
            String requestGET = query.get("get").get(0);
            switch (requestGET) {
                case "all_methods":
                    return getAllMethods().toString();
                case "team_name":
                    return new JSONObject().put("team_name", comLayer.getTeamName()).toString();
                case "team_number":
                    return new JSONObject().put("team_number", comLayer.getTeamNumber()).toString();
            }
        }

        if (query.get("post") != null) {
            String requestPOST = query.get("post").get(0);
            switch (requestPOST) {
                case "call_method":
                    return callMethod().toString();
            }
        }
        
        return null;
    }

        

    private JSONObject getAllMethods() {
        JSONObject res = new JSONObject();
        Arrays.stream(comLayer.getCallableMethods()).forEach(m ->
                res.put(m.getName(), Arrays.stream(m.getParameters())
                    .map(Parameter::getType)
                    .collect(Collectors.toList()))
        );
        return res;
    }

    private JSONObject callMethod() throws BadInputException, MalformedRequestException, RobotException {
        if (!session.getMethod().equals(NanoHTTPD.Method.POST)) {
            throw new MalformedRequestException("Only acceptable method is POST");
        }
        super.commonHandle();

        if (!body.has("name")) {
            throw new BadInputException("No method name specified");
        }
        if (!body.has("args")) {
            throw new BadInputException("No method args were specified");
        }
        if (!(body.get("name") instanceof String)) {
            throw new BadInputException("The method name must be of type string");
        }
        if (!(body.get("args") instanceof JSONArray)) {
            throw new BadInputException("The method arguments must be of type array");
        }

        JSONArray args = body.getJSONArray("args");
        List<Object> argTypes = IntStream.range(0, args.length())
                .mapToObj(args::get)
                .collect(Collectors.toList());

        try {
            this.comLayer.call(body.getString("name"), argTypes);
        } catch (IllegalAccessException e) {
            throw new BadInputException("The method with the specified arguments is not allowed to be called");
        } catch (NoSuchMethodException e) {
            throw new BadInputException("The method with the specified arguments does not exist");
        } catch (InvocationTargetException e) {
            throw new RobotException(e);
        }

        JSONObject json = new JSONObject();
        json.append("message", "success");

        return json;
    }

}

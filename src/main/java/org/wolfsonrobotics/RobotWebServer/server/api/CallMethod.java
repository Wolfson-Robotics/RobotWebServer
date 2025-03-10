package org.wolfsonrobotics.RobotWebServer.server.api;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.BadInputException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.MalformedRequestException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.RobotException;

import fi.iki.elonen.NanoHTTPD;


public class CallMethod extends RobotAPI {

    public CallMethod(NanoHTTPD.IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
    }

    @Override
    public String handle() throws BadInputException, MalformedRequestException, RobotException {

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
        // temp for now manually switch bigdecimal to double
        // TODO: just make the above function not get BigDecimal at all
        for (int i = 0; i < argTypes.size(); i++) {
            if (argTypes.get(i).getClass().equals(BigDecimal.class)) {
                double converted = ((BigDecimal) (argTypes.get(i))).doubleValue();
                argTypes.set(i, converted);
            }

        }

        try {
            this.comLayer.call(body.getString("name"), argTypes);
        } catch (IllegalAccessException e) {
            throw new BadInputException("The method with the specified arguments is not allowed to be called");
        } catch (NoSuchMethodException e) {
            throw new BadInputException("The method with the specified arguments does not exist");
        } catch (InvocationTargetException e) {
            throw new RobotException(e);
        }

        JSONObject success = new JSONObject();
        success.put("message", "Success");
        return success.toString();

    }
}
package org.wolfsonrobotics.RobotWebServer.server.api.robot;

import fi.iki.elonen.NanoHTTPD;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.communication.MethodArg;
import org.wolfsonrobotics.RobotWebServer.server.api.RobotAPI;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.BadInputException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.MalformedRequestException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.RobotException;

import java.lang.reflect.InvocationTargetException;
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
        MethodArg[] mArgs = IntStream.range(0, args.length())
                .mapToObj(args::getJSONObject)
                .map(obj -> {
                    String type = obj.keys().next();
                    return MethodArg.of(type, obj.get(type));
                }).toArray(MethodArg[]::new);

        // temp for now manually switch bigdecimal to double
        // TOD: just make the above function not get BigDecimal at all
        /*
        for (int i = 0; i < argTypes.size(); i++) {
            if (argTypes.get(i).getClass().equals(BigDecimal.class)) {
                double converted = ((BigDecimal) (argTypes.get(i))).doubleValue();
                argTypes.set(i, converted);
            }

        }*/

        try {
            this.comLayer.call(body.getString("name"), mArgs);
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
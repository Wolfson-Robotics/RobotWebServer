package org.wolfsonrobotics.RobotWebServer.server.api.robot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import fi.iki.elonen.NanoHTTPD;

import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.communication.MethodArg;
import org.wolfsonrobotics.RobotWebServer.server.api.RobotAPI;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.BadInputException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.MalformedRequestException;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.RobotException;
import org.wolfsonrobotics.RobotWebServer.util.GsonHelper;

import java.lang.reflect.InvocationTargetException;
import java.util.stream.IntStream;


public class CallMethod extends RobotAPI {

    public CallMethod(NanoHTTPD.IHTTPSession session, CommunicationLayer commLayer) {
        super(session, commLayer);
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
        if (!GsonHelper.isString(body, "name")) {
            throw new BadInputException("The method name must be of type string");
        }
        if (!GsonHelper.isJSONArr(body, "args")) {
            throw new BadInputException("The method arguments must be of type array");
        }


        JsonArray args = body.getAsJsonArray("args");
        MethodArg[] mArgs = IntStream.range(0, args.size())
                .mapToObj(args::get)
                .map(JsonElement::getAsJsonObject)
                .map(obj -> MethodArg.of(obj.get("type").getAsString(), GsonHelper.getAsObj(obj, "value")))
                .toArray(MethodArg[]::new);

        try {
            this.commLayer.call(body.get("name").getAsString(), mArgs);
        } catch (IllegalAccessException e) {
            throw new BadInputException("The method with the specified arguments is not allowed to be called");
        } catch (NoSuchMethodException e) {
            throw new BadInputException("The method with the specified arguments does not exist");
        } catch (InvocationTargetException e) {
            throw new RobotException(e);
        }

        return success();

    }
}
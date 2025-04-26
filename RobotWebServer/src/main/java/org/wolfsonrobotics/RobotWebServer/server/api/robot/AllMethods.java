package org.wolfsonrobotics.RobotWebServer.server.api.robot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.api.RobotAPI;
import org.wolfsonrobotics.RobotWebServer.util.GsonHelper;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AllMethods extends RobotAPI {

    public AllMethods(IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
    }

    @Override
    public String handle() {

        JsonObject res = new JsonObject();
        Arrays.stream(comLayer.getCallableMethods()).forEach(m -> {
            List<String> paramTypes = Arrays.stream(m.getParameterTypes())
                    .map(Class::getSimpleName)
                    .collect(Collectors.toList());

            JsonArray methodArgsList = GsonHelper.optJSONArray(res, m.getName());
            GsonHelper.add(methodArgsList, paramTypes);
            GsonHelper.put(res, m.getName(), methodArgsList);
        });
        return res.toString();
    }

}
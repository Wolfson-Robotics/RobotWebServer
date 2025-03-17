package org.wolfsonrobotics.RobotWebServer.server.api;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;

public class AllMethods extends RobotAPI {

    public AllMethods(IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
    }

    @Override
    public String handle() {

        JSONObject res = new JSONObject();
        Arrays.stream(comLayer.getCallableMethods()).forEach(m -> {
            if (m == null) { return; } //null safety
            List<Class<?>> params = Arrays.stream(m.getParameters())
                    .map(Parameter::getType)
                    .collect(Collectors.toList());

            JSONArray methodArgsList = res.optJSONArray(m.getName(), new JSONArray());
            methodArgsList.put(params);
            res.put(m.getName(), methodArgsList);
        });
        return res.toString();
    }

}
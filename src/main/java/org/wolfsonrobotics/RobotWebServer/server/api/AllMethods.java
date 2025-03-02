package org.wolfsonrobotics.RobotWebServer.server.api;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AllMethods extends RobotAPI {

    public AllMethods(IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
        this.responseType = "application/json";
    }

    @Override
    public String handle() {

        JSONObject res = new JSONObject();
        Arrays.stream(comLayer.getCallableMethods()).forEach(m -> {
            List<Class<?>> params = Arrays.stream(m.getParameters())
                    .map(Parameter::getType)
                    .collect(Collectors.toList());

            JSONArray methodOverloadList = res.optJSONArray(m.getName(), new JSONArray());
            methodOverloadList.put(params);
            res.put(m.getName(), methodOverloadList);
        });
        return res.toString();
    }
    
}

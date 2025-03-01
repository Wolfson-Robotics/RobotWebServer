package org.wolfsonrobotics.RobotWebServer.server.api;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AllMethods extends RobotAPI {
    
    public AllMethods(IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
    }

    @Override
    public String handle() {

        JSONObject res = new JSONObject();
        Arrays.stream(comLayer.getCallableMethods()).forEach(m ->
                res.put(m.getName(), Arrays.stream(m.getParameters())
                    .map(Parameter::getType)
                    .collect(Collectors.toList()))
        );
        return res.toString();
    }
    
}

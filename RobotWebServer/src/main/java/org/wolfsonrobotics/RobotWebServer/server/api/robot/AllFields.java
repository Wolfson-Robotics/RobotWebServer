package org.wolfsonrobotics.RobotWebServer.server.api.robot;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.api.RobotAPI;
import org.wolfsonrobotics.RobotWebServer.util.GsonHelper;

import java.util.Arrays;

import fi.iki.elonen.NanoHTTPD;

public class AllFields extends RobotAPI {

    public AllFields(NanoHTTPD.IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
    }

    @Override
    public String handle() {

        JsonObject res = new JsonObject();
        Arrays.stream(comLayer.getRawFields()).forEach(f -> {
            JsonArray types = new JsonArray();

            Class<?> type = f.getType();
            do {
                // Use getName() so that the class may be retrieved when a method with these
                // particular types is called in MethodArg.java
                types.add(type.getName());
            } while ((type = type.getSuperclass()) != null);
            GsonHelper.put(res, f.getName(), types);
        });
        return res.toString();
    }
}

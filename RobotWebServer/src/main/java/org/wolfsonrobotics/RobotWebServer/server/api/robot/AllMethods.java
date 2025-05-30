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

    // String is included, as it, for all practical purposes, is primitive
    private final List<Class<?>> primClasses = List.of(Integer.class, Double.class, Float.class, Long.class, Byte.class, Short.class, Boolean.class, Character.class, String.class);

    public AllMethods(IHTTPSession session, CommunicationLayer comLayer) {
        super(session, comLayer);
    }

    private boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive() || primClasses.contains(clazz);
    }

    @Override
    public String handle() {

        JsonObject res = new JsonObject();
        Arrays.stream(comLayer.getCallableMethods()).forEach(m -> {
            List<String> paramTypes = Arrays.stream(m.getParameterTypes())
                    .map(c -> isPrimitive(c) ? c.getSimpleName() : c.getName())
                    .collect(Collectors.toList());

            JsonArray methodArgsList = GsonHelper.optJSONArray(res, m.getName());
            GsonHelper.add(methodArgsList, paramTypes);
            res.add(m.getName(), methodArgsList);
//            GsonHelper.put(res, m.getName(), methodArgsList);
        });
        return res.toString();
    }

}
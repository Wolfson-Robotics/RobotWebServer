package org.wolfsonrobotics.RobotWebServer.server.api.robot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qualcomm.robotcore.robot.Robot;

import org.wolfsonrobotics.RobotWebServer.ServerConfig;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.api.RobotAPI;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.RobotException;
import org.wolfsonrobotics.RobotWebServer.util.GsonHelper;

import java.util.Arrays;
import java.util.Optional;

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

            // Get the actual instance type of the field, not just the declared type,
            // as the declared type may be different and therefore the variable
            // may be able to encapsulate different types. For instance, a field's
            // type may be the DcMotor interface. This interface has no parent classes,
            // but the variable may actually be declared as a DcMotorImplEx. Therefore,
            // we need to consider both the actual type of the instantiated variable and
            // the declared type.
            try {
                Class<?> instanceType = Optional.ofNullable(comLayer.getField(f)).orElse(new Object()).getClass();
                do {
                    if (!GsonHelper.contains(types, instanceType.getName())) types.add(instanceType.getName());
                } while ((instanceType = instanceType.getSuperclass()) != null);
            } catch (IllegalAccessException e) {
            }

            GsonHelper.put(res, f.getName(), types);
        });
        return res.toString();
    }
}

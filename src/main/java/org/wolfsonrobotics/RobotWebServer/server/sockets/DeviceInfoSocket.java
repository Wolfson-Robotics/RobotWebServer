package org.wolfsonrobotics.RobotWebServer.server.sockets;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.fakerobot.DcMotorEx;
import org.wolfsonrobotics.RobotWebServer.fakerobot.HardwareDevice;
import org.wolfsonrobotics.RobotWebServer.fakerobot.Servo;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.RobotException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DeviceInfoSocket extends BaseSocket {


    private final Map<Class<? extends HardwareDevice>, Map<String, String>> infoProtocol = new HashMap<>();


    public DeviceInfoSocket(NanoHTTPD.IHTTPSession handshakeRequest, CommunicationLayer commLayer) {
        super(handshakeRequest, commLayer);

        Map<String, String> dcMotorHandler = new HashMap<>();
        dcMotorHandler.put("Power", "getPower");
        dcMotorHandler.put("Position", "getPosition");
        this.infoProtocol.put(DcMotorEx.class, dcMotorHandler);

        Map<String, String> servoHandler = new HashMap<>();
        servoHandler.put("Position", "getPosition");
        this.infoProtocol.put(Servo.class, servoHandler);
    }

    @Override
    public void onOpen() {
        super.onOpen();
        this.keepRunning(this::onMessage);
    }

    @Override
    public void onMessage(NanoWSD.WebSocketFrame message) {
        try {

            JSONArray info = new JSONArray();
            for (String field : commLayer.getFields()) {

                CommunicationLayer comp;
                try {
                    comp = commLayer.getField(field);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RobotException(e);
                }

                Optional<Class<? extends HardwareDevice>> compType = infoProtocol.keySet().stream().filter(comp::instanceOf).findFirst();
                if (!compType.isPresent()) {
                    continue;
                }

                JSONObject compInfo = new JSONObject();
                compInfo.put("name", field);
                compInfo.put("type", comp.getName());
                try {
                    // For loop instead of forEach to catch exceptions
                    for (Map.Entry<String, String> entry : infoProtocol.get(compType.get()).entrySet()) {
                        compInfo.put(entry.getKey(), comp.call(entry.getValue()));
                    }
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    throw new RobotException(e);
                }
                info.put(compInfo);

            }
            send(info.toString());

        } catch (IOException | RobotException e) {
            e.printStackTrace();
        }
    }


}

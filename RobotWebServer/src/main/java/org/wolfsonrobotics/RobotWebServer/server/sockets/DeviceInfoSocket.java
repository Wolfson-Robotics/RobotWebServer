package org.wolfsonrobotics.RobotWebServer.server.sockets;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qualcomm.robotcore.hardware.HardwareDevice;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

import org.wolfsonrobotics.RobotWebServer.ServerConfig;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.RobotException;
import org.wolfsonrobotics.RobotWebServer.util.GsonHelper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

public class DeviceInfoSocket extends BaseSocket {


    public DeviceInfoSocket(NanoHTTPD.IHTTPSession handshakeRequest, CommunicationLayer commLayer) {
        super(handshakeRequest, commLayer);
    }

    @Override
    public void onOpen() {
        super.onOpen();
        this.keepMessaging();
    }

    @Override
    public void onMessage(NanoWSD.WebSocketFrame message) {
        try {

            JsonArray info = new JsonArray();
            for (String field : commLayer.getFields()) {

                CommunicationLayer comp;
                try {
                    comp = commLayer.getFieldLayer(field);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RobotException(e);
                }

                Optional<Class<? extends HardwareDevice>> compType = ServerConfig.deviceInfoMap.keySet().stream().filter(comp::instanceOf).findFirst();
                if (!compType.isPresent()) {
                    continue;
                }


                try {

                    JsonObject compInfo = new JsonObject();
                    GsonHelper.put(compInfo, "name", field);
                    GsonHelper.put(compInfo, "type", comp.getName());

                    // For loop instead of forEach to catch exceptions
                    for (Map.Entry<String, String> entry : ServerConfig.deviceInfoMap.get(compType.get()).entrySet()) {
                        GsonHelper.put(compInfo, entry.getKey(), comp.call(entry.getValue()));
                    }
                    info.add(compInfo);

                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
                    throw new RobotException(e);
                }

            }
            send(info.toString());

        } catch (IOException | RobotException e) {
            e.printStackTrace();
        }
    }


}

package org.wolfsonrobotics.RobotWebServer.server.sockets;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wolfsonrobotics.RobotWebServer.ServerConfig;
import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.fakerobot.HardwareDevice;
import org.wolfsonrobotics.RobotWebServer.server.api.exception.RobotException;

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

            JSONArray info = new JSONArray();
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

                JSONObject compInfo = new JSONObject();
                compInfo.put("name", field);
                compInfo.put("type", comp.getName());
                try {
                    // For loop instead of forEach to catch exceptions
                    for (Map.Entry<String, String> entry : ServerConfig.deviceInfoMap.get(compType.get()).entrySet()) {
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

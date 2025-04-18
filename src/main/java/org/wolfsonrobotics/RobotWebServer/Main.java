package org.wolfsonrobotics.RobotWebServer;

import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;
import org.wolfsonrobotics.RobotWebServer.server.RobotWebServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {

        // TODO (Reminder): When porting turn this into Environment.getExternalStorageDirectory().getPath()
        String filePath = "";
        String[] excludedMethods = { "getCameraFeed" };
        RobotWebServer ws = new RobotWebServer(
                8080,
                "website/",
                new CommunicationLayer(new FakeRobot(), null, excludedMethods),
                filePath
                );
        try {
            ws.start();
        } catch (IOException e) {
            ws.stop();
            if (ws.isAlive()) {
                System.out.println("Failed to close webserver after start failure");
            }
            throw new RuntimeException(e);
        }

    }

}
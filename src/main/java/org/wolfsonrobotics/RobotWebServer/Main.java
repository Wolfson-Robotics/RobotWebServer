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

        FakeRobot instance = new FakeRobot();

        RobotWebServer ws = new RobotWebServer(
                8080,
                "website/",
                new CommunicationLayer(instance, null, excludedMethods),
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

        // Loop to keep things updated for the FakeRobot for demonstration purposes.
        // Is not necessary once ported to robot code
        try {
            while (ws.isAlive()) {
                instance.populateTelemetry();
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

}
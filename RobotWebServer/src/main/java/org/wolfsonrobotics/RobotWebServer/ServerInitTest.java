package org.wolfsonrobotics.RobotWebServer;

import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.robot.FakeRobot;
import org.wolfsonrobotics.RobotWebServer.server.RobotWebServer;

import java.io.IOException;

public class ServerInitTest {

    public static void main(String[] args) {

        FakeRobot instance = new FakeRobot();
        RobotWebServer ws = new RobotWebServer(
//                new CommunicationLayer(instance, ServerConfig.COMM_METHODS, ServerConfig.EXCLUDED_COMM_METHODS)
                new CommunicationLayer(instance, null, new String[] { "getCameraFeed", "populateTelemetry "})
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
        // TODO: Is not necessary once ported to robot code
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
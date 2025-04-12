package org.wolfsonrobotics.RobotWebServer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;
import org.wolfsonrobotics.RobotWebServer.server.RobotWebServer;

public class Main {

    public static void main(String[] args) {

        Method[] methods = null;
        try {
            methods = new Method[]{ FakeRobot.class.getDeclaredMethod("getCameraFeed") };
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }

        RobotWebServer ws = new RobotWebServer(
                8080,
                "website/",
                new CommunicationLayer(new FakeRobot(), null, methods));
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
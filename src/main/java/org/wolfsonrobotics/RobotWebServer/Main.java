package org.wolfsonrobotics.RobotWebServer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;
import org.wolfsonrobotics.RobotWebServer.server.RobotWebServer;

public class Main {

    public static void main(String[] args) {

        ArrayList<Method> selectedMethods = new ArrayList<>();
        try {
            selectedMethods.add(FakeRobot.class.getDeclaredMethod("moveBot", int.class, int.class));
            selectedMethods.add(FakeRobot.class.getDeclaredMethod("turnBot", double.class));
            selectedMethods.add(FakeRobot.class.getDeclaredMethod("moveArm", double.class));
            selectedMethods.add(FakeRobot.class.getDeclaredMethod("downArm"));
            selectedMethods.add(FakeRobot.class.getDeclaredMethod("upArm"));
            selectedMethods.add(FakeRobot.class.getDeclaredMethod("stringTest", String.class));
        } catch (NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
            return;
        }

        Method[] methods = new Method[selectedMethods.size()];
        methods = selectedMethods.toArray(methods);

        RobotWebServer ws = new RobotWebServer(
                8080,
                "website/",
                new CommunicationLayer(new FakeRobot(), methods));
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
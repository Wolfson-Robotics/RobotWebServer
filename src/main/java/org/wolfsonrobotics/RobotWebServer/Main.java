package org.wolfsonrobotics.RobotWebServer;

import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;
import org.wolfsonrobotics.RobotWebServer.server.RobotWebServer;

import java.io.IOException;

public class Main {

    Thread server;

    public static void main(String[] args) {
        try {
            RobotWebServer ws = new RobotWebServer(8080, "website/", new FakeRobot());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
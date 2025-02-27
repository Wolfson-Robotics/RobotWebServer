package org.wolfsonrobotics.RobotWebServer;

import java.io.IOException;

import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;
import org.wolfsonrobotics.RobotWebServer.server.WebServer;

public class Main {

    Thread server;

    public static void main(String[] args) {


        try {
            WebServer ws = new WebServer(8080, "website/", new FakeRobot());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
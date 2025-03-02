package org.wolfsonrobotics.RobotWebServer;

import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;
import org.wolfsonrobotics.RobotWebServer.server.RobotWebServer;

import java.io.IOException;

public class Main {

    Thread server;

    public static void main(String[] args) {
        WebDashboard wd = new WebDashboard(
            "Wolfson Robotics", 
            19916, 
            8080, 
            "website/", 
            new FakeRobot());
        wd.start();
    }

}
package org.wolfsonrobotics.RobotWebServer;

import java.io.IOException;

import org.wolfsonrobotics.RobotWebServer.server.WebServer;


public class Main {

    public static void main(String[] args) {
        try {
            new WebServer(8080, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    

}
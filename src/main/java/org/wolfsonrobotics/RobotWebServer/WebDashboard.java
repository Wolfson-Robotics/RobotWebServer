package org.wolfsonrobotics.RobotWebServer;

import java.io.IOException;

import org.wolfsonrobotics.RobotWebServer.communication.CommunicationLayer;
import org.wolfsonrobotics.RobotWebServer.fakerobot.FakeRobot;
import org.wolfsonrobotics.RobotWebServer.server.DashboardRobot;
import org.wolfsonrobotics.RobotWebServer.server.RobotWebServer;

/*
 * Class to be called in the robot code for setting up the Web Server and it's dashboard
 */
public class WebDashboard {
    
    String teamName;
    int teamNumber;
    int port; //TODO: make everything in the codebase grab this port (especially the js)
    String webroot;
    DashboardRobot robotInstance;

    RobotWebServer ws;


    public WebDashboard(String teamName, int teamNumber, int port, String webroot, DashboardRobot robotInstance) {
        this.teamName = teamName;
        this.teamNumber = teamNumber;
        this.port = port;
        this.webroot = webroot;
        this.robotInstance = robotInstance;
    }

    /*
     * Start the Dashboard, return's true if succeeded
     */
    public boolean start() {

        try {
            //TODO: run on a seperate thread
            ws = new RobotWebServer(
                port, 
                webroot, 
                new CommunicationLayer(robotInstance));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /*
     * Stop the Dashboard, return's true if succeeded
     */
    public boolean stop() {
        ws.stop();
        return !ws.isAlive();
    }

}

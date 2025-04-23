package org.wolfsonrobotics.RobotWebServer.fakerobot.telemetry;

// Mimic Telemetry class in Control Hub SDK
public class Telemetry {

    private LogImpl log = new LogImpl();

    public void addLine(String line) {
        log.add(line);
    }
    public void update() {
        log.display();
        log.markClean();
    }

}

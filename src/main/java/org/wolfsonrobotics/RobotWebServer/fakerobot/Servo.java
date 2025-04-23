package org.wolfsonrobotics.RobotWebServer.fakerobot;

// Mimic DcMotorEx class in Control Hub SDK
public class Servo extends HardwareDevice {

    public double getPosition() {
        return Math.random();
    }

}

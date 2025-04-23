package org.wolfsonrobotics.RobotWebServer.fakerobot;

// Mimic DcMotorEx class in Control Hub SDK
public class DcMotorEx extends HardwareDevice {

    public double getPower() {
        return Math.random();
    }
    public int getPosition() {
        return (int) Math.floor(Math.random() * 10);
    }

}

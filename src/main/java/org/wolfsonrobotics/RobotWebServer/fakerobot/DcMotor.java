package org.wolfsonrobotics.RobotWebServer.fakerobot;

// Mimic DcMotor class in Control Hub SDK
public class DcMotor extends HardwareDevice {

    public double getPower() {
        return Math.random();
    }
    public int getPosition() {
        return (int) Math.floor(Math.random() * 10);
    }

}

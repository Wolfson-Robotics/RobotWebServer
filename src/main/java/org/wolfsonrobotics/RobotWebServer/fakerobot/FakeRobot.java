package org.wolfsonrobotics.RobotWebServer.fakerobot;


import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class FakeRobot {

    public double rot = 0; //degrees
    public int locationX = 0;
    public int locationY = 0;
    public double armPosY = 0.3;

    public void moveBot(int plusX, int plusY) {
        locationX += plusX;
        locationY += plusY;
    }

    public void turnBot(double degrees) {
        rot += degrees;
    }

    public void moveArm(double plusPosY) {
        armPosY += plusPosY;
        armPosY = armPosY > 1 ? 1 : armPosY;
        armPosY = armPosY < 0 ? 0 : armPosY;
    }

    public void downArm() {
        armPosY = 0.3;
    }

    public void upArm() {
        armPosY = 0.6;
    }

    // For testing with the code editor
    public void stringTest(String test) {
        System.out.println(test);
    }

    public void sleep(long ms) {
        try { Thread.sleep(ms); } catch (Exception ignored) {}
    }


    // String of pixels separated by commas whose elements themselves are
    // separated by dashes for rgb vals
    private int curFrame = 0;
    public Mat getCameraFeed() {
        Mat cameraFeed = Mat.eye(1080, 1920, CvType.CV_8UC3);

        if (curFrame >= cameraFeed.rows()) {
            curFrame = 0;
        }

        for (int f = curFrame; f <= curFrame + 20; f++) {
            if (f >= cameraFeed.rows()) {
                break;
            }
            for (int col = 0; col < cameraFeed.cols(); col++) {
                cameraFeed.put(f, col, 255, 255, 255);
            }
        }
        curFrame += 20;

        return cameraFeed;
    }

}

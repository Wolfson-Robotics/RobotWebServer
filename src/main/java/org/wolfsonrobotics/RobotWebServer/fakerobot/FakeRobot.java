package org.wolfsonrobotics.RobotWebServer.fakerobot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class FakeRobot {

    /*
     * What FakeRobot needs to be able to do:
     * Simple movement function
     * Simple rotation function
     * Simple vertical arm extension function
     * Return a discernable video feed using OpenCV
     */

    int rot = 0; //degrees
    int locationX = 0;
    int locationY = 0;
    double armPosY = 0.3;

    public void moveBot(int plusX, int plusY) {
        locationX += plusX;
        locationY += plusY;
    }

    public void turnBot(double degrees) {
        rot += degrees;
    }

    public void moveArm(int plusPosY) {
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


    // String of pixels separated by commas whose elements themselves are
    // separated by dashes for rgb vals
    int curFrame = 0;
    public Mat getCameraFeed() {
        Mat cameraFeed = Mat.eye(1920, 1080, CvType.CV_8UC1);

        if (cameraFeed.cols() == curFrame) {
            curFrame = 0;
        }
        for (int col = 0; col < cameraFeed.cols(); col++) {
            cameraFeed.put(curFrame, col, new double[] { 255, 255, 255 });
        }
        curFrame++;

        return cameraFeed;
    }


    public String stringifyMat(Mat input) {
        List<String> pixels = new ArrayList<>();
        for (int row = 0; row < input.rows(); row++) {
            for (int col = 0; col < input.cols(); col++) {

                pixels.add(String.join("-", 
                    Arrays.stream(input.get(row, col))
                        .mapToObj(String::valueOf)
                        .toArray(String[]::new))

                );
            }
        }
        return String.join(",", pixels);
    }

}

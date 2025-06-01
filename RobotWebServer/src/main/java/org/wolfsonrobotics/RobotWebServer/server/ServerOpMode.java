package org.wolfsonrobotics.RobotWebServer.server;

import org.opencv.core.Mat;

public interface ServerOpMode {
    String[] getExcludedMethods();
    Mat getCameraFeed();
}
package org.wolfsonrobotics.RobotWebServer;

import android.os.Environment;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorImpl;
import com.qualcomm.robotcore.hardware.DcMotorImplEx;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoImpl;
import com.qualcomm.robotcore.hardware.ServoImplEx;

import org.wolfsonrobotics.RobotWebServer.server.api.FileAPI;
import org.wolfsonrobotics.RobotWebServer.server.api.RobotAPI;
import org.wolfsonrobotics.RobotWebServer.server.api.file.DirectoryAction;
import org.wolfsonrobotics.RobotWebServer.server.api.file.FileAction;
import org.wolfsonrobotics.RobotWebServer.server.api.file.Listing;
import org.wolfsonrobotics.RobotWebServer.server.api.robot.AllMethods;
import org.wolfsonrobotics.RobotWebServer.server.api.robot.CallMethod;
import org.wolfsonrobotics.RobotWebServer.server.api.robot.CameraFeed;
import org.wolfsonrobotics.RobotWebServer.server.sockets.BaseSocket;
import org.wolfsonrobotics.RobotWebServer.server.sockets.CameraSocket;
import org.wolfsonrobotics.RobotWebServer.server.sockets.DeviceInfoSocket;
import org.wolfsonrobotics.RobotWebServer.server.sockets.TelemetrySocket;

import java.util.HashMap;
import java.util.Map;

public class ServerConfig {

    public static final String WEBROOT = Environment.getExternalStorageDirectory().getPath() + "/website/";
    public static final int PORT = 39537;
    public static final int SOCKET_PORT = 39538;
    public static final int SOCKET_START_TIMEOUT = 60000;

    public static final String CONTROL_HUB_STORAGE = Environment.getExternalStorageDirectory().getPath();
    public static final String[] COMM_METHODS = null;
    public static final String[] EXCLUDED_COMM_METHODS = { "getCameraFeed" };

    public static final int DEFAULT_SOCKET_MSG_FREQUENCY = 500;

    // TODO: Properly load NanoHTTPD properties files later
    public static final Map<String, String> mimeTypes = new HashMap<String, String>() {{
        put("html", "text/html");
        put("css", "text/css");
        put("js", "text/javascript");
    }};

    public static final Map<String, Class<? extends RobotAPI>> robotAPIMap = new HashMap<String, Class<? extends RobotAPI>>() {{
        put("/robot/all_methods", AllMethods.class);
        put("/robot/call_method", CallMethod.class);
        put("/robot/camera_feed", CameraFeed.class);
    }};

    public static final Map<String, Class<? extends BaseSocket>> socketMap = new HashMap<String, Class<? extends BaseSocket>>() {{
        put("robot/camera_feed", CameraSocket.class);
        put("robot/device_info", DeviceInfoSocket.class);
        put("robot/telemetry", TelemetrySocket.class);
    }};

    public static final Map<String, Class<? extends FileAPI>> fileAPIMap = new HashMap<String, Class<? extends FileAPI>>() {{
        put("/file/listing", Listing.class);
        put("/file/file_operation", FileAction.class);
        put("/file/directory_operation", DirectoryAction.class);
    }};



    public static final String CAMERA_FEED_METHOD = "getCameraFeed";
    public static final Map<String, String> motorProperties = new HashMap<String, String>() {{
        put("Power", "getPower");
        put("Position", "getCurrentPosition");
    }};
    public static final Map<String, String> servoProperties = new HashMap<String, String>() {{
        put("Position", "getPosition");
    }};
    public static final Map<Class<? extends HardwareDevice>, Map<String, String>> deviceInfoMap = new HashMap<Class<? extends HardwareDevice>, Map<String, String>>() {{
        put(DcMotor.class, motorProperties);
        put(DcMotorEx.class, motorProperties);
        put(DcMotorImpl.class, motorProperties);
        put(DcMotorImplEx.class, motorProperties);
        put(Servo.class, servoProperties);
        put(ServoImpl.class, servoProperties);
        put(ServoImplEx.class, servoProperties);
    }};

    public static final String TELEMETRY_FIELD = "telemetry";

}

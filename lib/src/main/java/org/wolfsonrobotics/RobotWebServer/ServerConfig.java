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
import org.wolfsonrobotics.RobotWebServer.server.api.robot.AllFields;
import org.wolfsonrobotics.RobotWebServer.server.api.robot.AllMethods;
import org.wolfsonrobotics.RobotWebServer.server.api.robot.CallMethod;
import org.wolfsonrobotics.RobotWebServer.server.api.robot.CameraFeed;
import org.wolfsonrobotics.RobotWebServer.server.sockets.BaseSocket;
import org.wolfsonrobotics.RobotWebServer.server.sockets.CameraSocket;
import org.wolfsonrobotics.RobotWebServer.server.sockets.DeviceInfoSocket;
import org.wolfsonrobotics.RobotWebServer.server.sockets.TelemetrySocket;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServerConfig {

    public static String WEBROOT = Environment.getExternalStorageDirectory().getPath() + "/website/";
    public static int PORT = 39537;
    public static int SOCKET_START_TIMEOUT = 60000;

    public static String CONTROL_HUB_STORAGE = Environment.getExternalStorageDirectory().getPath();
    public static String[] COMM_METHODS = null;
    public static String[] EXCLUDED_COMM_METHODS = { "getExcludedMethods", "getCameraFeed", "runOpMode", "idle", "init", "init_loop", "isStarted", "isStopRequested", "loop", "opModeInInit", "opModeIsActive", "start", "stop", "waitForStart", "getRuntime", "internalPostInitLoop", "internalPostLoop", "internalPreInit", "internalUpdateTelemetryNow", "resetRuntime", "terminateOpModeNow", "updateTelemetry", "requestOpModeStop", "clone", "equals", "finalize", "getClass", "hashCode", "notify", "notifyAll", "toString" };

    public static int DEFAULT_SOCKET_MSG_FREQUENCY = 100;

    // Hardcoded mime types, as no Android packages (even the NanoHTTPD version) serve
    // these particular mime types correctly
    public static Map<String, String> mimeTypes = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("js", "text/javascript");
    }});

    public static Map<String, Class<? extends RobotAPI>> robotAPIMap = Collections.unmodifiableMap(new HashMap<String, Class<? extends RobotAPI>>() {{
        put("/robot/all_methods", AllMethods.class);
        put("/robot/call_method", CallMethod.class);
        put("/robot/all_fields", AllFields.class);
        put("/robot/camera_feed", CameraFeed.class);
    }});

    public static Map<String, Class<? extends BaseSocket>> socketMap = Collections.unmodifiableMap(new HashMap<String, Class<? extends BaseSocket>>() {{
        put("robot/camera_feed", CameraSocket.class);
        put("robot/device_info", DeviceInfoSocket.class);
        put("robot/telemetry", TelemetrySocket.class);
    }});

    public static Map<String, Class<? extends FileAPI>> fileAPIMap = Collections.unmodifiableMap(new HashMap<String, Class<? extends FileAPI>>() {{
        put("/file/listing", Listing.class);
        put("/file/file_operation", FileAction.class);
        put("/file/directory_operation", DirectoryAction.class);
    }});



    public static String CAMERA_FEED_METHOD = "getCameraFeed";
    public static Map<String, String> motorProperties = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("Power", "getPower");
        put("Position", "getCurrentPosition");
    }});
    public static Map<String, String> servoProperties = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("Position", "getPosition");
    }});
    public static Map<Class<? extends HardwareDevice>, Map<String, String>> deviceInfoMap = Collections.unmodifiableMap(new HashMap<Class<? extends HardwareDevice>, Map<String, String>>() {{
        put(DcMotor.class, motorProperties);
        put(DcMotorEx.class, motorProperties);
        put(DcMotorImpl.class, motorProperties);
        put(DcMotorImplEx.class, motorProperties);
        put(Servo.class, servoProperties);
        put(ServoImpl.class, servoProperties);
        put(ServoImplEx.class, servoProperties);
    }});

    public static String TELEMETRY_FIELD = "telemetry";

}

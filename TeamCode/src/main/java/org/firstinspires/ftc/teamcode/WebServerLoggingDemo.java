package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.wolfsonrobotics.RobotWebServer.server.RobotWebServer;

import java.io.IOException;

@TeleOp(name = "WebServerLoggingDemo")
public class WebServerLoggingDemo extends LinearOpMode {

    private DcMotorEx lf_drive, lb_drive, rf_drive, rb_drive;
    private Servo arm, claw;

    private Mat currFeed;
    private OpenCvCamera camera;


    public void initMotors() {
        this.lf_drive = hardwareMap.get(DcMotorEx.class, "lf_drive");
        this.lb_drive = hardwareMap.get(DcMotorEx.class, "lb_drive");
        this.rf_drive = hardwareMap.get(DcMotorEx.class, "rf_drive");
        this.rb_drive = hardwareMap.get(DcMotorEx.class, "rb_drive");

        this.arm = hardwareMap.get(Servo.class, "arm");
        this.claw = hardwareMap.get(Servo.class, "claw");

        this.lf_drive.setDirection(DcMotorSimple.Direction.REVERSE);
        this.lb_drive.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    public void initCamera() {
        String cameraName = "Camera";
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        this.camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, cameraName), cameraMonitorViewId);
        camera.setPipeline(new OpenCvPipeline() {
            @Override
            public Mat processFrame(Mat input) {
                currFeed = input;
                return input;
            }
        });


        camera.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener()
        {
            @Override
            public void onOpened()
            {
                camera.startStreaming(432,240, OpenCvCameraRotation.UPRIGHT);
            }

            @Override
            public void onError(int errorCode) {
                telemetry.addData("Camera error code:", errorCode);
                telemetry.update();
            }
        });
    }

    public Mat getCameraFeed() {
        return this.currFeed;
    }


    @Override
    public void runOpMode() {
        this.initMotors();
        this.initCamera();

        RobotWebServer server = new RobotWebServer(this);
        try {
            server.start();
        } catch (IOException e) {
            server.stop();
            throw new RuntimeException(e);
        }
        waitForStart();

        while (opModeIsActive()) {
            this.lf_drive.setPower(gamepad1.right_stick_y + gamepad1.left_stick_x);
            this.lb_drive.setPower(gamepad1.right_stick_y + gamepad1.left_stick_x);
            this.rf_drive.setPower(gamepad1.right_stick_y + gamepad1.left_stick_x);
            this.rb_drive.setPower(gamepad1.right_stick_y + gamepad1.left_stick_x);

            this.arm.setPosition(-gamepad2.left_stick_y);
            this.claw.setPosition(gamepad2.right_bumper ? 0.2 : -0.2);

            telemetry.addData("lf_drive pos", this.lf_drive.getCurrentPosition());
            telemetry.addData("lf_drive power", this.lf_drive.getPower());
            telemetry.addData("lb_drive pos", this.lb_drive.getCurrentPosition());
            telemetry.addData("lb_drive power", this.lb_drive.getPower());
            telemetry.addData("rf_drive pos", this.rf_drive.getCurrentPosition());
            telemetry.addData("rf_drive power", this.rf_drive.getPower());
            telemetry.addData("rb_drive pos", this.rb_drive.getCurrentPosition());
            telemetry.addData("rb_drive power", this.rb_drive.getPower());
            telemetry.addData("arm pos", this.arm.getPosition());
            telemetry.addData("claw pos", this.claw.getPosition());
            telemetry.update();
        }
        server.stop();

    }


}

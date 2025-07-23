package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.opencv.core.Mat;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvPipeline;
import org.wolfsonrobotics.RobotWebServer.server.RobotWebServer;
import org.wolfsonrobotics.RobotWebServer.server.ServerOpMode;

import java.io.IOException;

@TeleOp(name = "WebServerDemo")
public class WebServerDemo extends RobotBaseStub implements ServerOpMode {

    private Mat currFeed;
    private OpenCvCamera camera;


    public void initCamera() {
        int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
        this.camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, this.cameraName), cameraMonitorViewId);
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


    // Implemented methods
    public Mat getCameraFeed() {
        return this.currFeed;
    }

    public String[] getExcludedMethods() {
        return new String[] { "initMotors", "initCamera", "a" };
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

            // WebServerDemo should not take controller input, only host the webserver
/*
            this.lf_drive.setPower(gamepad1.right_stick_y + gamepad1.left_stick_x);
            this.lb_drive.setPower(gamepad1.right_stick_y + gamepad1.left_stick_x);
            this.rf_drive.setPower(gamepad1.right_stick_y + gamepad1.left_stick_x);
            this.rb_drive.setPower(gamepad1.right_stick_y + gamepad1.left_stick_x);

            this.arm.setPosition(-gamepad2.left_stick_y);
            this.claw.setPosition(gamepad2.right_bumper ? 0.2 : -0.2);*/

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
        camera.closeCameraDevice();
        server.stop();

    }

    // Test for method exclusion per op mode
    public void a() {
        System.out.println("excluded");
    }



}

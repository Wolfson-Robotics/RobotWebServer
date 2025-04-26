package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import org.wolfsonrobotics.RobotWebServer.server.RobotWebServer;

import java.io.IOException;

@Autonomous(name = "WebServerInputDemo")
public class WebServerInputDemo extends LinearOpMode {

    private DcMotorEx lf_drive, lb_drive, rf_drive, rb_drive;
    private Servo arm, claw;

    @Override
    public void runOpMode() {
        this.lf_drive = hardwareMap.get(DcMotorEx.class, "lf_drive");
        this.lb_drive = hardwareMap.get(DcMotorEx.class, "lb_drive");
        this.rf_drive = hardwareMap.get(DcMotorEx.class, "rf_drive");
        this.rb_drive = hardwareMap.get(DcMotorEx.class, "rb_drive");

        this.arm = hardwareMap.get(Servo.class, "arm");
        this.claw = hardwareMap.get(Servo.class, "claw");

        this.lf_drive.setDirection(DcMotorSimple.Direction.REVERSE);
        this.lb_drive.setDirection(DcMotorSimple.Direction.REVERSE);

        RobotWebServer server = new RobotWebServer(this);
        try {
            server.start();
        } catch (IOException e) {
            server.stop();
            throw new RuntimeException(e);
        }
        waitForStart();

        while (opModeIsActive()) {

        }

    }

}

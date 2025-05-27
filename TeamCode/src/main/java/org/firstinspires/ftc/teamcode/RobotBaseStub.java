package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Intended for demonstration with mission control's ability to detect hardware components
 * and methods in the parent class.
 */
public abstract class RobotBaseStub extends LinearOpMode {

    protected DcMotorEx lf_drive, lb_drive, rf_drive, rb_drive;
    protected Servo arm, claw;

    protected final double intCon = 8.727272;
    protected final double powerFactor = 0.6;
    protected final double degConv = 2.5555555555555555555555555555556;

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




    protected void moveMotor(DcMotor motor, int targetPosition, double speed, boolean stay) {
        int oldTargetPosition = targetPosition;
        motor.setTargetPosition(targetPosition);
        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motor.setPower(speed);
        targetPosition = oldTargetPosition + targetPosition;
        if (targetPosition < 0) {
            targetPosition *= -1;
        }
        while (motor.isBusy() && motor.getCurrentPosition() < targetPosition) {
            idle();
        }
        if (!stay) {
            motor.setPower(0);
        }
    }

    protected void driveMotor(DcMotor motor, int targetPosition, double speed) {
        int oldTargetPosition = targetPosition;
        motor.setTargetPosition(targetPosition);
        motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        motor.setPower(speed);
        targetPosition = oldTargetPosition + targetPosition;
        if (targetPosition < 0) {
            targetPosition *= -1;
        }
    }
    protected void moveMotor(DcMotor motor, int targetPosition, double speed) {
        moveMotor(motor, targetPosition, speed, false);
    }



    public void moveBot(double distIN, double vertical, double pivot, double horizontal) {

        int motorTics;
        int posNeg = (vertical >= 0) ? 1 : -1;

        rf_drive.setPower(powerFactor * (-pivot + (vertical - horizontal)));
        rb_drive.setPower(powerFactor * (-pivot + vertical + horizontal));
        lf_drive.setPower(powerFactor * (pivot + vertical + horizontal));
        lb_drive.setPower(powerFactor * (pivot + (vertical - horizontal)));

        if (horizontal != 0) {
            posNeg = (horizontal > 0) ? 1 : -1;
            motorTics = lf_drive.getCurrentPosition() + (int) ((distIN * intCon) * (posNeg));
            if (posNeg == 1) {
                // right goes negative
                while ((lf_drive.getCurrentPosition() < motorTics) && opModeIsActive()) {
                    idle();
                }
            } else {
                // left goes positive
                while ((lf_drive.getCurrentPosition() > motorTics) && opModeIsActive()) {
                    idle();
                }
            }
        } else {
            posNeg = vertical >= 0 ? -1 : 1;
            motorTics = rf_drive.getCurrentPosition() + (int) ((distIN * intCon) * posNeg);
            if (posNeg == -1) {
                while (rf_drive.getCurrentPosition() > motorTics && opModeIsActive()) {
                    idle();
                }
            } else {
                while ((rf_drive.getCurrentPosition() < motorTics) && opModeIsActive()) {
                    idle();
                }
            }

        }
        removePower();

    }

    protected void turnBot(double degrees) {
        // 13.62 inches is default robot length
        double robotLength = 13.62;
        double distUnit = (robotLength) / (Math.cos(45));
        double distIN = (Math.abs((distUnit * ((degrees*1.75))) / 90))*degConv;
        int motorTics;
        int pivot = (degrees >= 0) ? 1 : -1;
        rf_drive.setPower(powerFactor * (-pivot));
        rb_drive.setPower(powerFactor * (-pivot));
        lf_drive.setPower(powerFactor * (pivot));
        lb_drive.setPower(powerFactor * (pivot));
        motorTics = lf_drive.getCurrentPosition() + (int) Math.round((distIN * intCon)* pivot);
        if (pivot == 1) {
            while ((lf_drive.getCurrentPosition() < motorTics) && opModeIsActive()) {
                idle();
            }
        }
        if (pivot == -1) {
            while ((lf_drive.getCurrentPosition() > motorTics) && opModeIsActive()) {
                idle();
            }
        }
        removePower();

    }

    protected void removePower() {
        lf_drive.setPower(0);
        lb_drive.setPower(0);
        rf_drive.setPower(0);
        rb_drive.setPower(0);
    }

}

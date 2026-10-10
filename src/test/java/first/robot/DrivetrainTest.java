// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.wpilib.hardware.hal.HAL;
import org.wpilib.hardware.hal.RobotMode;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.simulation.DriverStationSim;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DrivetrainTest {
  private Drivetrain drivetrain;

  @BeforeAll
  void setup() {
    HAL.initialize();
    DriverStationSim.setRobotMode(RobotMode.TELEOPERATED);
    DriverStationSim.setEnabled(true);
    DriverStationSim.notifyNewData();
    drivetrain = new Drivetrain();
  }

  @BeforeEach
  void reset() {
    drive(0, 0, 50);
    drivetrain.resetPose(Drivetrain.START_POSE);
  }

  private void drive(double forward, double rotation, int loops) {
    for (int i = 0; i < loops; i++) {
      drivetrain.arcadeDrive(forward, rotation);
      drivetrain.simulationPeriodic();
      drivetrain.periodic();
    }
  }

  @Test
  void forwardStickDrivesAlongHeading() {
    drive(1.0, 0.0, 50);

    Pose2d pose = drivetrain.getPose();
    assertTrue(pose.getX() > Drivetrain.START_POSE.getX() + 0.5, "x was " + pose.getX());
    assertEquals(Drivetrain.START_POSE.getY(), pose.getY(), 0.05);
  }

  @Test
  void backwardStickDrivesInReverse() {
    drive(-1.0, 0.0, 25);

    assertTrue(drivetrain.getPose().getX() < Drivetrain.START_POSE.getX() - 0.1);
  }

  @Test
  void positiveRotationTurnsCounterclockwise() {
    drive(0.0, 0.5, 10);

    double heading = drivetrain.getPose().getRotation().getRadians();
    assertTrue(heading > 0.05, "heading was " + heading);
  }

  @Test
  void robotStaysOnTheField() {
    drive(1.0, 0.0, 1000);

    Pose2d pose = drivetrain.getPose();
    assertTrue(pose.getX() < 16.54, "x was " + pose.getX());
    assertTrue(pose.getX() > 14.0, "x was " + pose.getX());
  }
}

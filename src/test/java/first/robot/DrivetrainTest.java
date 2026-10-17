// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
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
  void resetAfterDrivingKeepsThePoseWhereItWasPut() {
    drive(1.0, 0.0, 100);
    drive(0.0, 0.0, 50);

    drivetrain.resetPose(Drivetrain.START_POSE);
    drive(0.0, 0.0, 5);

    Pose2d pose = drivetrain.getPose();
    assertEquals(Drivetrain.START_POSE.getX(), pose.getX(), 0.01);
    assertEquals(Drivetrain.START_POSE.getY(), pose.getY(), 0.01);
  }

  @Test
  void recordedAutoRepeatsTheTeleopDrive(@TempDir Path dir) {
    DriveRecording recording = new DriveRecording(dir.resolve("recorded-auto.csv"));

    // Drive in "teleop" while recording: forward, an arc to the left, then stop.
    recording.startRecording();
    for (int i = 0; i < 150; i++) {
      double forward = i < 50 ? 0.6 : i < 100 ? 0.4 : 0.0;
      double rotation = i >= 50 && i < 100 ? 0.3 : 0.0;
      drivetrain.arcadeDrive(forward, rotation);
      recording.record(forward, rotation);
      drivetrain.simulationPeriodic();
      drivetrain.periodic();
    }
    recording.stopRecording();
    Pose2d driven = drivetrain.getPose();

    // Replay it from the same start in autonomous.
    drivetrain.resetPose(Drivetrain.START_POSE);
    SimpleAuto auto = new SimpleAuto(drivetrain, recording);
    auto.start(SimpleAuto.RECORDED);
    for (int i = 0; i < 200; i++) {
      auto.periodic();
      drivetrain.simulationPeriodic();
      drivetrain.periodic();
    }
    Pose2d replayed = drivetrain.getPose();

    assertTrue(driven.getX() > Drivetrain.START_POSE.getX() + 1.0, "x was " + driven.getX());
    assertEquals(driven.getX(), replayed.getX(), 0.02);
    assertEquals(driven.getY(), replayed.getY(), 0.02);
    assertEquals(driven.getRotation().getDegrees(), replayed.getRotation().getDegrees(), 1.0);
  }

  private Pose2d runAuto(String routine, int loops) {
    SimpleAuto auto = new SimpleAuto(drivetrain, new DriveRecording());
    auto.start(routine);
    for (int i = 0; i < loops; i++) {
      auto.periodic();
      drivetrain.simulationPeriodic();
      drivetrain.periodic();
    }
    return drivetrain.getPose();
  }

  @Test
  void driveForwardAutoDrivesAboutTwoMeters() {
    Pose2d pose = runAuto(SimpleAuto.DRIVE_FORWARD, 250);

    assertEquals(Drivetrain.START_POSE.getX() + 2.0, pose.getX(), 0.3);
    assertEquals(Drivetrain.START_POSE.getY(), pose.getY(), 0.05);
  }

  @Test
  void driveAndTurnAutoEndsUpLeftOfTheFirstLeg() {
    Pose2d pose = runAuto(SimpleAuto.DRIVE_AND_TURN, 500);

    assertEquals(Drivetrain.START_POSE.getX() + 2.0, pose.getX(), 0.4);
    assertEquals(Drivetrain.START_POSE.getY() + 1.0, pose.getY(), 0.4);
    assertEquals(90, pose.getRotation().getDegrees(), 15);
  }

  @Test
  void robotStaysOnTheField() {
    drive(1.0, 0.0, 1000);

    Pose2d pose = drivetrain.getPose();
    assertTrue(pose.getX() < 16.54, "x was " + pose.getX());
    assertTrue(pose.getX() > 14.0, "x was " + pose.getX());
  }
}

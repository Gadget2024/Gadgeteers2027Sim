// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import org.wpilib.math.geometry.Pose2d;

/**
 * Autonomous routines built from simple steps: drive a distance, turn an angle.
 *
 * <p>
 * Each routine is a numbered list of steps. A step keeps running every loop
 * until it reports
 * that it is done, then the routine moves on to the next step. When there are
 * no steps left, the
 * robot stops.
 */
public class SimpleAuto {
  public static final String DRIVE_FORWARD = "Drive Forward";
  public static final String DRIVE_AND_TURN = "Drive and Turn";
  public static final String SQUARE = "Square";

  private final Drivetrain drivetrain;

  private String routine = DRIVE_FORWARD;
  private int step;
  private Pose2d stepStartPose;

  /**
   * Creates the autonomous routines for a drivetrain.
   *
   * @param drivetrain the drivetrain to drive
   */
  public SimpleAuto(Drivetrain drivetrain) {
    this.drivetrain = drivetrain;
    stepStartPose = drivetrain.getPose();
  }

  /**
   * Starts a routine from its first step. Call this from autonomousInit().
   *
   * @param routine the name of the routine to run
   */
  public void start(String routine) {
    this.routine = routine != null ? routine : DRIVE_FORWARD;
    step = 0;
    stepStartPose = drivetrain.getPose();
  }

  /**
   * Runs the current step of the routine. Call this from autonomousPeriodic().
   */
  public void periodic() {
    switch (routine) {
      case DRIVE_AND_TURN -> driveAndTurn();
      case SQUARE -> square();
      default -> driveForward();
    }
  }

  /** Drives 2 meters forward, then stops. */
  private void driveForward() {
    switch (step) {
      case 0 -> {
        if (driveDistance(2.0, 0.5)) {
          nextStep();
        }
      }
      default -> drivetrain.stop();
    }
  }

  /**
   * Drives 2 meters forward, turns 90 degrees to the left, then drives 1 meter.
   */
  private void driveAndTurn() {
    switch (step) {
      case 0 -> {
        if (driveDistance(2.0, 0.5)) {
          nextStep();
        }
      }
      case 1 -> {
        if (turnDegrees(90, 0.4)) {
          nextStep();
        }
      }
      case 2 -> {
        if (driveDistance(1.0, 0.5)) {
          nextStep();
        }
      }
      default -> drivetrain.stop();
    }
  }

  /** Drives a 1.5 meter square, turning left at each corner. */
  private void square() {
    switch (step) {
      // Side 1, corner 1
      case 0 -> {
        if (driveDistance(1.5, 0.5)) {
          nextStep();
        }
      }
      case 1 -> {
        if (turnDegrees(90, 0.4)) {
          nextStep();
        }
      }
      // Side 2, corner 2
      case 2 -> {
        if (driveDistance(1.5, 0.5)) {
          nextStep();
        }
      }
      case 3 -> {
        if (turnDegrees(90, 0.4)) {
          nextStep();
        }
      }
      // Side 3, corner 3
      case 4 -> {
        if (driveDistance(1.5, 0.5)) {
          nextStep();
        }
      }
      case 5 -> {
        if (turnDegrees(90, 0.4)) {
          nextStep();
        }
      }
      // Side 4, corner 4
      case 6 -> {
        if (driveDistance(1.5, 0.5)) {
          nextStep();
        }
      }
      case 7 -> {
        if (turnDegrees(90, 0.4)) {
          nextStep();
        }
      }
      // All 8 steps done: stand still
      default -> drivetrain.stop();
    }
  }

  /**
   * Moves on to the next step and remembers where the robot was when it started.
   */
  private void nextStep() {
    drivetrain.stop();
    step++;
    stepStartPose = drivetrain.getPose();
  }

  /**
   * Drives straight until the robot has covered a distance since the step
   * started.
   *
   * @param meters   how far to drive
   * @param throttle how fast to drive, from 0 to 1; use a negative value to drive
   *                 backward
   * @return true once the distance has been covered
   */
  private boolean driveDistance(double meters, double throttle) {
    double traveled = drivetrain.getPose().getTranslation().getDistance(stepStartPose.getTranslation());
    if (traveled >= meters) {
      return true;
    }
    drivetrain.arcadeDrive(throttle, 0);
    return false;
  }

  /**
   * Turns in place until the robot has rotated an angle since the step started.
   *
   * @param degrees  how far to turn; positive turns left (counterclockwise),
   *                 negative turns right
   * @param throttle how fast to turn, from 0 to 1
   * @return true once the robot has turned far enough
   */
  private boolean turnDegrees(double degrees, double throttle) {
    double turned = drivetrain.getPose().getRotation().minus(stepStartPose.getRotation()).getDegrees();
    if (Math.abs(turned) >= Math.abs(degrees)) {
      return true;
    }
    drivetrain.arcadeDrive(0, Math.copySign(throttle, degrees));
    return false;
  }
}

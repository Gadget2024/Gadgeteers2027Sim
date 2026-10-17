// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import org.wpilib.drivers.motor.PWMSparkMax;
import org.wpilib.hardware.imu.OnboardIMU;
import org.wpilib.hardware.rotation.Encoder;
import org.wpilib.math.geometry.Pose2d;
import org.wpilib.math.geometry.Rotation2d;
import org.wpilib.math.kinematics.DifferentialDriveOdometry;
import org.wpilib.simulation.DifferentialDrivetrainSim;
import org.wpilib.simulation.DifferentialDrivetrainSim.KitbotGearing;
import org.wpilib.simulation.DifferentialDrivetrainSim.KitbotMotor;
import org.wpilib.simulation.DifferentialDrivetrainSim.KitbotWheelSize;
import org.wpilib.simulation.EncoderSim;
import org.wpilib.simulation.OnboardIMUSim;
import org.wpilib.smartdashboard.Field2d;
import org.wpilib.system.RobotController;
import org.wpilib.telemetry.Telemetry;

/** A kitbot-style differential drivetrain that is shown on a field map in simulation. */
public class Drivetrain {
  /** Where the robot is placed on the field at startup and after a reset. */
  public static final Pose2d START_POSE = new Pose2d(2.0, 4.0, Rotation2d.ZERO);

  // Field size in meters, used to keep the simulated robot on the map.
  private static final double FIELD_LENGTH = 16.54;
  private static final double FIELD_WIDTH = 8.07;
  private static final double ROBOT_RADIUS = 0.45;

  private static final double WHEEL_RADIUS = 0.0762;
  private static final int ENCODER_RESOLUTION = 4096;

  private final PWMSparkMax leftMotor = new PWMSparkMax(0);
  private final PWMSparkMax rightMotor = new PWMSparkMax(1);

  private final Encoder leftEncoder = new Encoder(0, 1);
  private final Encoder rightEncoder = new Encoder(2, 3);

  private final OnboardIMU imu = new OnboardIMU(OnboardIMU.MountOrientation.FLAT);

  private final DifferentialDriveOdometry odometry;

  // Simulation classes help us simulate our robot
  private final EncoderSim leftEncoderSim = new EncoderSim(leftEncoder);
  private final EncoderSim rightEncoderSim = new EncoderSim(rightEncoder);
  private final DifferentialDrivetrainSim drivetrainSimulator =
      DifferentialDrivetrainSim.createKitbotSim(
          KitbotMotor.DUAL_CIM_PER_SIDE, KitbotGearing.RATIO_10P71, KitbotWheelSize.SIX_INCH, null);
  private final Field2d field = new Field2d();

  /** Sets up the encoders and places the robot at the start pose. */
  public Drivetrain() {
    leftEncoder.setDistancePerPulse(2 * Math.PI * WHEEL_RADIUS / ENCODER_RESOLUTION);
    rightEncoder.setDistancePerPulse(2 * Math.PI * WHEEL_RADIUS / ENCODER_RESOLUTION);

    odometry =
        new DifferentialDriveOdometry(
            imu.getRotation2d(), leftEncoder.getDistance(), rightEncoder.getDistance());
    resetPose(START_POSE);
  }

  /**
   * Drives the robot with arcade controls.
   *
   * @param forward forward throttle from -1 to 1, positive is forward
   * @param rotation rotation throttle from -1 to 1, positive is counterclockwise
   */
  public void arcadeDrive(double forward, double rotation) {
    double left = forward - rotation;
    double right = forward + rotation;

    // Keep the stick ratio when the sum would exceed full throttle.
    double scale = Math.max(1.0, Math.max(Math.abs(left), Math.abs(right)));
    leftMotor.setThrottle(left / scale);
    rightMotor.setThrottle(right / scale);
  }

  /** Stops both sides of the drivetrain. */
  public void stop() {
    leftMotor.setThrottle(0);
    rightMotor.setThrottle(0);
  }

  /**
   * Moves the robot to a pose on the field.
   *
   * @param pose the new pose
   */
  public final void resetPose(Pose2d pose) {
    // setPose also zeroes the simulated wheel positions, so the encoders must follow before
    // odometry is reset. Otherwise odometry sees the jump as the robot driving backward.
    drivetrainSimulator.setPose(pose);
    leftEncoderSim.setDistance(drivetrainSimulator.getLeftPosition());
    rightEncoderSim.setDistance(drivetrainSimulator.getRightPosition());
    OnboardIMUSim.setYaw(pose.getRotation().getRadians());
    odometry.resetPosition(
        imu.getRotation2d(), leftEncoder.getDistance(), rightEncoder.getDistance(), pose);
  }

  /**
   * Returns the robot's pose on the field as measured by odometry.
   *
   * @return the pose
   */
  public Pose2d getPose() {
    return odometry.getPose();
  }

  /** Updates odometry and the field map. Call this every loop. */
  public void periodic() {
    odometry.update(imu.getRotation2d(), leftEncoder.getDistance(), rightEncoder.getDistance());
    field.setRobotPose(odometry.getPose());
    Telemetry.log("Field", field);
  }

  /** Updates the simulated drivetrain and feeds its state to the simulated sensors. */
  public void simulationPeriodic() {
    drivetrainSimulator.setInputs(
        leftMotor.getThrottle() * RobotController.getInputVoltage(),
        rightMotor.getThrottle() * RobotController.getInputVoltage());
    drivetrainSimulator.update(0.02);

    // The physics model has no walls, so keep the robot on the map.
    Pose2d pose = drivetrainSimulator.getPose();
    double x = Math.clamp(pose.getX(), ROBOT_RADIUS, FIELD_LENGTH - ROBOT_RADIUS);
    double y = Math.clamp(pose.getY(), ROBOT_RADIUS, FIELD_WIDTH - ROBOT_RADIUS);
    if (x != pose.getX() || y != pose.getY()) {
      resetPose(new Pose2d(x, y, pose.getRotation()));
    }

    leftEncoderSim.setDistance(drivetrainSimulator.getLeftPosition());
    leftEncoderSim.setRate(drivetrainSimulator.getLeftVelocity());
    rightEncoderSim.setDistance(drivetrainSimulator.getRightPosition());
    rightEncoderSim.setRate(drivetrainSimulator.getRightVelocity());
    OnboardIMUSim.setYaw(drivetrainSimulator.getHeading().getRadians());
  }
}

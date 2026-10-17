// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import org.wpilib.driverstation.Gamepad;
import org.wpilib.framework.TimedRobot;
import org.wpilib.tunable.Selectable;
import org.wpilib.tunable.Tunables;

/**
 * The methods in this class are called automatically corresponding to each mode, as described in
 * the TimedRobot documentation. If you change the name of this class or the package after creating
 * this project, you must also update the Main.java file in the project.
 */
public class Robot extends TimedRobot {
  private final Selectable<String> chooser = new Selectable<>();

  private final Gamepad controller = new Gamepad(0);
  private final Drivetrain drivetrain = new Drivetrain();
  private final SimpleAuto auto = new SimpleAuto(drivetrain);

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  public Robot() {
    chooser.addDefault(SimpleAuto.DRIVE_FORWARD, SimpleAuto.DRIVE_FORWARD);
    chooser.add(SimpleAuto.DRIVE_AND_TURN, SimpleAuto.DRIVE_AND_TURN);
    chooser.add(SimpleAuto.SQUARE, SimpleAuto.SQUARE);
    Tunables.publish("Auto choices", chooser);
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and utility.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    drivetrain.periodic();
  }

  /**
   * Starts the autonomous routine picked in the "Auto choices" selector. The robot is moved back
   * to its start position first, so every run begins from the same place.
   *
   * <p>The routines themselves are in SimpleAuto.java. To add one, write it there and add its name
   * to the chooser in the constructor above.
   */
  @Override
  public void autonomousInit() {
    String autoSelected = chooser.getSelected();
    System.out.println("Auto selected: " + autoSelected);
    drivetrain.resetPose(Drivetrain.START_POSE);
    auto.start(autoSelected);
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    auto.periodic();
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    // Left stick drives forward and back, right stick turns. Pushing a stick forward or left
    // reads negative, so both are inverted.
    drivetrain.arcadeDrive(-controller.getLeftY(), -controller.getRightX());

    // Start puts the robot back where it began.
    if (controller.getStartButtonPressed()) {
      drivetrain.resetPose(Drivetrain.START_POSE);
    }
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
    drivetrain.stop();
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when utility mode is enabled. */
  @Override
  public void utilityInit() {}

  /** This function is called periodically during utility mode. */
  @Override
  public void utilityPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {
    drivetrain.simulationPeriodic();
  }
}

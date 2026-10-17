// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package first.robot;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.wpilib.system.Filesystem;

/**
 * Records the driver's stick inputs in teleop and plays them back in autonomous.
 *
 * <p>Every loop (20 ms) while recording, the forward and rotation values sent to the drivetrain
 * are saved. Playback sends the same values back, one per loop, so the robot drives the way the
 * driver did. The recording is kept in a file so it survives restarting the robot program.
 */
public class DriveRecording {
  private final Path file;
  private final List<double[]> inputs = new ArrayList<>();
  private boolean recording;
  private int playbackIndex;

  /** Creates a recording stored in recorded-auto.csv in the robot's operating directory. */
  public DriveRecording() {
    this(Filesystem.getOperatingDirectory().toPath().resolve("recorded-auto.csv"));
  }

  /**
   * Creates a recording stored in a specific file.
   *
   * @param file where the recording is saved and loaded
   */
  public DriveRecording(Path file) {
    this.file = file;
  }

  /** Throws away any previous recording and starts recording. */
  public void startRecording() {
    inputs.clear();
    recording = true;
    System.out.println("Recording started");
  }

  /**
   * Adds one loop of driver input to the recording. Does nothing unless recording.
   *
   * @param forward the forward value sent to the drivetrain
   * @param rotation the rotation value sent to the drivetrain
   */
  public void record(double forward, double rotation) {
    if (recording) {
      inputs.add(new double[] {forward, rotation});
    }
  }

  /** Stops recording and saves it to the file. Does nothing unless recording. */
  public void stopRecording() {
    if (!recording) {
      return;
    }
    recording = false;

    List<String> lines = new ArrayList<>();
    for (double[] input : inputs) {
      lines.add(input[0] + "," + input[1]);
    }
    try {
      Files.write(file, lines);
      System.out.printf(
          "Recording saved: %.1f seconds to %s%n", inputs.size() * 0.02, file.toAbsolutePath());
    } catch (IOException e) {
      System.out.println("Could not save recording to " + file.toAbsolutePath() + ": " + e);
    }
  }

  /**
   * Returns whether a recording is in progress.
   *
   * @return true while recording
   */
  public boolean isRecording() {
    return recording;
  }

  /** Loads the saved recording from the file and rewinds to its beginning. */
  public void startPlayback() {
    playbackIndex = 0;
    inputs.clear();
    try {
      for (String line : Files.readAllLines(file)) {
        String[] parts = line.split(",");
        inputs.add(new double[] {Double.parseDouble(parts[0]), Double.parseDouble(parts[1])});
      }
      System.out.printf("Playing recording: %.1f seconds%n", inputs.size() * 0.02);
    } catch (IOException | RuntimeException e) {
      System.out.println("No recording to play from " + file.toAbsolutePath() + ": " + e);
    }
  }

  /**
   * Drives the next loop of the recording. Call this once per loop during autonomous.
   *
   * @param drivetrain the drivetrain to drive
   * @return true once the whole recording has been played
   */
  public boolean playNext(Drivetrain drivetrain) {
    if (playbackIndex >= inputs.size()) {
      drivetrain.stop();
      return true;
    }
    double[] input = inputs.get(playbackIndex++);
    drivetrain.arcadeDrive(input[0], input[1]);
    return false;
  }
}

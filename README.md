# Gadgeteers 2027 Sim (FRC Team 9670)

A minimal WPILib **2027 Alpha 7** Java robot project that runs in desktop simulation and
connects to the new **2027 FIRST Driver Station**. No robot hardware is needed.

This guide takes a clean Windows laptop to "Driver Station shows green comms with the
simulated robot".

## What you need

- 64-bit Windows 11 (x64)
- About 10 GB of free disk space
- Git, to clone this repo

Versions used here. The alpha releases are not interchangeable, so use these exactly:

| Component | Version |
| --- | --- |
| WPILib | `2027.0.0-alpha-7` |
| FIRST Driver Station | `2027.0.0-alpha-8` (patch update to the alpha 7 Driver Station) |
| GradleRIO (in `build.gradle`) | `2027.0.0-alpha-7` |

## 1. Install WPILib 2027 Alpha 7

1. Open the [WPILib 2027.0.0 Alpha 7 release](https://github.com/wpilibsuite/allwpilib/releases/tag/v2027.0.0-alpha-7)
   and download **Windows (x64)** (`WPILib_Windows-x64-2027.0.0-alpha-7.iso`, 2.4 GB).
2. Optional: check the download in PowerShell.

   ```powershell
   Get-FileHash .\WPILib_Windows-x64-2027.0.0-alpha-7.iso -Algorithm SHA256
   ```

   Expected: `05f5b3af9e2d3866b71cd8883833864d23917b5d316f8b10e2d76630e8b1a84e`
3. Double-click the ISO to mount it, then run `WPILibInstaller.exe`.
4. Accept the defaults and let it install its own copy of VS Code.

When it finishes you will have `C:\Users\Public\wpilib\2027_alpha7` and a
**2027 WPILib VS Code** shortcut on the desktop.

## 2. Install the 2027 FIRST Driver Station

The Driver Station is **not** part of the WPILib installer and is not in the `allwpilib`
releases. It has its own repository.

1. Open the [FirstDriverStation-Public alpha 8 release](https://github.com/wpilibsuite/FirstDriverStation-Public/releases/tag/v2027.0.0-alpha-8).
2. Download `FirstDriverStation-win-x64-2027.0.0-alpha-8.exe` (49 MB).
3. Optional: check the download.

   ```powershell
   Get-FileHash .\FirstDriverStation-win-x64-2027.0.0-alpha-8.exe -Algorithm SHA256
   ```

   Expected: `925d30d00d866596bb3fabd93d3561372eefa7898ea3f8e7f638928e4062b745`
4. Run the installer. It installs to `C:\Program Files\FIRST Driver Station` and adds a
   **FIRST Driver Station** shortcut to the desktop and Start Menu.

## 3. Get the project

```powershell
git clone <this repo's URL>
```

Open the cloned folder in **2027 WPILib VS Code** (File > Open Folder). Use the 2027
shortcut, not a regular VS Code install or an older WPILib year.

## 4. Run the simulation

1. Start **FIRST Driver Station** from the desktop shortcut.
2. In VS Code press `Ctrl+Shift+P` and run **WPILib: Simulate Robot Code**.
3. When asked which extensions to run, leave both ticked:
   - **Sim GUI**
   - **Use Real DriverStation**
4. The Sim GUI window opens and the terminal prints:

   ```
   DriverStationSocket Initialized!
   ********** Robot program startup complete **********
   ```

The Driver Station finds the simulated robot on localhost by itself. Its communications,
robot and robot-code indicators turn green. The gamepad indicator stays red until a
controller is plugged in.

Pick **Tele** and press **Enable** in the Driver Station to enable the simulated robot.

### Running without VS Code

There is no standalone `simulateJava` Gradle task in 2027 Alpha 7; VS Code launches the
sim itself. From PowerShell in the project folder you can do the same thing by hand:

```powershell
$env:JAVA_HOME = "C:\Users\Public\wpilib\2027_alpha7\jdk"
.\gradlew.bat simulateExternalJava
$jni = "$PWD\build\jni\release"
$env:HALSIM_EXTENSIONS = "$jni\halsim_gui.dll;$jni\halsim_ds_socket.dll"
$env:PATH = "$jni;$env:PATH"
.\gradlew.bat run
```

Close the Sim GUI window to stop it.

## Recreating this project from scratch

1. In 2027 WPILib VS Code run **WPILib: Create a new project**.
2. Choose **Template**, **Java**, **Timed Robot**, and enter team number `9670`.
3. In `build.gradle`, change

   ```groovy
   wpi.sim.addDriverstation()
   ```

   to

   ```groovy
   wpi.sim.addDriverstation().defaultEnabled = true
   ```

That last change is the only difference from the stock template. Without it the
"Use Real DriverStation" extension is unticked by default, the sim starts with only the
Sim GUI, and the Driver Station never gets comms.

## Troubleshooting

**Driver Station shows no communications**

- Check the sim terminal for `DriverStationSocket Initialized!`. If it is missing, the
  "Use Real DriverStation" extension was not loaded. Stop the sim and start it again with
  that extension ticked.
- Make sure only one simulation is running. A second copy cannot bind the same ports.
- With the extension loaded the sim listens on `127.0.0.1` TCP `1740` and UDP `1110`:

  ```powershell
  Get-NetTCPConnection -LocalPort 1740 -State Listen
  ```

**Build fails to find the GradleRIO plugin**

`settings.gradle` reads WPILib's offline Maven repo from
`C:\Users\Public\wpilib\2027_alpha7\maven`. If that folder is missing, WPILib Alpha 7 is
not installed (step 1).

**Wrong Java version**

The project needs the JDK that ships with WPILib 2027
(`C:\Users\Public\wpilib\2027_alpha7\jdk`). Building from 2027 WPILib VS Code uses it
automatically; on the command line set `JAVA_HOME` as shown above.

## Links

- [Systemcore / 2027 alpha testing hub](https://github.com/wpilibsuite/SystemcoreTesting)
- [WPILib 2027 documentation](https://docs.wpilib.org/en/2027/)
- [FIRST Driver Station releases and issues](https://github.com/wpilibsuite/FirstDriverStation-Public)

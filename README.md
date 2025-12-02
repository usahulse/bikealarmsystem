# Bicycle Alarm System

An Android-based application that turns your smartphone into a bicycle alarm system.

## Purpose

This project aims to provide a simple, yet effective, way to protect your bicycle from theft. By leveraging the sensors in your smartphone, this application can detect when your bike is being tamred with and sound an alarm to deter thieves and alert you.

## Features

*   **Motion Detection**: Uses the device's accelerometer to detect movement and trigger the alarm.
*   **GPS Tracking**: (Future feature) Will periodically report the location of your bike if it's moved.
*   **Loud Alarm**: Sounds a loud alarm through the phone's speaker to attract attention.
*   **Remote Arm/Disarm**: (Future feature) Will allow you to arm and disarm the alarm remotely.

## Setup

1.  **Prerequisites**:
    *   Android Studio
    *   An Android device or emulator
2.  **Building**:
    1.  Clone the repository: `git clone [your-repository-url-here]`
    2.  Open the project in Android Studio.
    3.  The main source code is located at `app/src/main/java/com/example/bicyclealarmsystem/MainActivity.java`.
    4.  Build the project to install dependencies.
    5.  Run the application on your device or emulator.
3.  **Importing from GitHub in Android Studio**:
    1.  Open Android Studio.
    2.  From the welcome screen, select "Get from VCS".
    3.  Alternatively, if you have a project open, go to `File > New > Project from Version Control...`.
    4.  Select "Git" from the version control dropdown.
    5.  Enter the repository URL: `[your-repository-url-here]`
    6.  Choose a directory for the project and click "Clone".
    7.  Android Studio will then clone the repository and open it as a new project.

## Usage

1.  Open the application.
2.  Place the phone securely on your bicycle.
3.  Arm the alarm using the on-screen button.
4.  If the bike is moved, the alarm will sound.
5.  Disarm the alarm using the on-screen button.

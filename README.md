# Bicycle Alarm System

An Android-based application that turns your smartphone into a bicycle alarm system.

## Purpose

This project aims to provide a simple, yet effective, way to protect your bicycle from theft. By leveraging the sensors in your smartphone, this application can detect when your bike is being tampered with and sound an alarm to deter thieves and alert you.

## Features

*   **Motion Detection**: Uses the device's accelerometer to detect movement and trigger the alarm.
*   **GPS Tracking**: (Future feature) Will periodically report the location of your bike if it's moved.
*   **Loud Alarm**: Sounds a loud alarm through the phone's speaker to attract attention.
*   **Remote Arm/Disarm**: (Future feature) Will allow you to arm and disarm the alarm remotely.
*   **Bike Ride Tracking**: Track your bike rides with two modes: "Free Ride" and "Destination Specific".
*   **Live Map**: See your location and route on a live map.
*   **Trip HUD**: View your average speed, distance traveled, trip time, and calories burned.
*   **Weather**: Get real-time weather information, including temperature and wind speed.
*   **Live Camera View**: See a live camera feed in a picture-in-picture window while you ride.

## Setup

1.  **Prerequisites**:
    *   Android Studio
    *   An Android device or emulator
2.  **Building**:
    1.  Clone the repository.
    2.  Open the project in Android Studio.
    3.  The main source code is located at `app/src/main/java/com/example/bicyclealarmsystem/MainActivity.java`.
    4.  Build the project to install dependencies.
    5.  Run the application on your device or emulator.
3.  **Importing from GitHub in Android Studio**:
    1.  Open Android Studio.
    2.  From the welcome screen, select "Get from VCS".
    3.  Alternatively, if you have a project open, go to `File > New > Project from Version Control...`.
    4.  Select "Git" from the version control dropdown.
    5.  Enter the repository URL.
    6.  Choose a directory for the project and click "Clone".
    7.  Android Studio will then clone the repository and open it as a new project.

## API Key Setup

This application uses the OpenWeatherMap API to provide weather data. To use this feature, you will need to obtain a free API key from [OpenWeatherMap](https://openweathermap.org/appid).

Once you have your API key, you will need to add it to the `strings.xml` file located at `app/src/main/res/values/strings.xml`. Replace the placeholder text with your API key:

```xml
<string name="openweathermap_api_key">YOUR_OPENWEATHERMAP_API_KEY</string>
```

## Usage

1.  Open the application.
2.  Place the phone securely on your bicycle.
3.  Arm the alarm using the on-screen button.
4.  If the bike is moved, the alarm will sound.
5.  Disarm the alarm using the on-screen button.
6.  To start a bike ride, choose between "Free Ride" and "Destination Specific" and follow the on-screen instructions.

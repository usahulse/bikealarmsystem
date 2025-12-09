# Bicycle Guardian: Android Bicycle Alarm System

Bicycle Guardian is an Android-based application designed to function as a comprehensive security system for your bicycle. It leverages the hardware capabilities of a modern smartphone to provide motion detection, location tracking, and a loud alarm to deter theft.

## Current Features

-   **Core Alarm System:** A robust security system that can be armed and disarmed with a single tap.
-   **Motion Detection:** Uses the device's accelerometer to detect unauthorized movement or tampering. When the system is armed, any significant motion will trigger the alarm.
-   **Siren & Flashing Lights:** The alarm consists of a loud siren using the system's default alarm sound and a flashing strobe using the camera's LED flashlight.
-   **Live Camera Preview:** The main dashboard includes a live feed from the device's rear-facing camera.
-   **GPS Location Services:** The app fetches and displays the device's last known GPS coordinates.
-   **Comprehensive Bike Profile:** A dedicated screen to store detailed information about the user and their bicycle, including:
    -   Owner's full name, contact information, and address.
    -   Detailed bike specifications (manufacturer, model, frame number, color, size, etc.).
    -   Insurance policy details.
    -   Police registration information.
    -   A photo of the bike, selected from the device's gallery and saved securely in the app's private storage.

## Required Resources (Prerequisites)

To build and run this application from the source code, you will need the following:

-   **Java Development Kit (JDK):** Version 1.8 (Java 8) or higher.
-   **Android SDK:** The project is configured to compile with SDK version 33. You can install this via Android Studio's SDK Manager or as a standalone command-line tool.
-   **Android Device or Emulator:** A device running Android 6.0 (API level 24) or higher.

---

## Project Setup and Build Instructions

This is a standard Gradle-based Android project. You can build it using the recommended Android Studio IDE or from the command line with Gradle, which is compatible with editors like Visual Studio Code.

### Option 1: Android Studio (Recommended)

1.  **Open the Project:**
    -   Launch Android Studio.
    -   Select "Open an Existing Project" and navigate to the root directory of this repository.
2.  **Sync Gradle:**
    -   Android Studio will automatically detect the Gradle files (`build.gradle`, `settings.gradle`) and sync the project. This will download all the necessary dependencies.
3.  **Build and Run:**
    -   Connect an Android device (with USB debugging enabled) or start an Android Virtual Device (AVD).
    -   Click the **Run 'app'** button (a green triangle) in the top toolbar, or use the menu: `Run > Run 'app'`.

### Option 2: Visual Studio Code and Command Line

While Android Studio is the standard, you can also build and run the project from the command line, which is useful for working with editors like VS Code.

1.  **VS Code Setup (Optional):**
    -   Install the **[Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack)**.
    -   Install the **[XML](https://marketplace.visualstudio.com/items?itemName=redhat.vscode-xml)** extension for better layout file support.
2.  **Build the Project:**
    -   Open a terminal in the root directory of the project.
    -   Run the following command to assemble the debug APK:
        ```bash
        ./gradlew assembleDebug
        ```
    -   The generated APK will be located at `app/build/outputs/apk/debug/app-debug.apk`.
3.  **Install and Run the App:**
    -   Make sure your Android device or emulator is running and connected (`adb devices`).
    -   Use the Android Debug Bridge (`adb`) to install the APK:
        ```bash
        adb install app/build/outputs/apk/debug/app-debug.apk
        ```
    -   Once installed, you can find and launch the "Bicycle Guardian" app on your device.

---

## Key Project Files and Structure

-   `app/build.gradle`: This file contains the core configuration for the Android app module, including the `applicationId`, SDK versions, and all project dependencies (e.g., CameraX, Google Play Services).
-   `app/src/main/AndroidManifest.xml`: Declares all essential components of the app, such as activities (`MainActivity`, `BikeProfileActivity`), and lists all the permissions the app requires to function.
-   `app/src/main/java/com/example/bicycleguardian/`: This is the main package for the application's Java source code.
    -   `MainActivity.java`: The main dashboard screen, which contains the security controls and the core alarm logic.
    -   `BikeProfileActivity.java`: The activity that manages the bike profile screen, including data input, saving, and loading.
-   `app/src/main/res/`: This directory contains all non-code resources.
    -   `layout/`: Contains all the UI layout files (e.g., `activity_main.xml`, `activity_bike_profile.xml`).
    -   `values/`: Contains resource files, most importantly `strings.xml` (for all user-facing text) and `colors.xml`/`themes.xml` (for styling).

## Android Permissions Used

-   `android.permission.CAMERA`: Required for the live camera preview and to control the flashlight for the alarm.
-   `android.permission.ACCESS_FINE_LOCATION` & `ACCESS_COARSE_LOCATION`: Used to fetch the device's GPS coordinates.
-   `android.permission.READ_MEDIA_IMAGES`: On modern Android devices, this permission is required to allow the user to select a photo from their gallery.
-   `android.permission.READ_EXTERNAL_STORAGE`: On older Android devices, this is the required permission for gallery access. Its use is capped at `maxSdkVersion="32"`.

## Future Features (Roadmap)

The following major features are planned for future development:

-   **Map & Geofence:** Integration of a live map to track the bicycle's location and the ability to set a geofence perimeter that triggers the alarm if crossed.
-   **Bike Ride Tracking:** A feature to record and display ride statistics, such as distance, speed, and time.
-   **Settings Panel:** A comprehensive settings screen to configure alarm sensitivity, siren sound, network settings, and more.

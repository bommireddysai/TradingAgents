# Quick Toggles

A floating, draggable on-screen panel (Android 8+; Wi-Fi ADB needs Android 11+) with three switches:

- **Private DNS** – sets Private DNS to a hostname (default `dns.google`, editable in the app) or off
- **USB debugging** – also enables Developer options when turned on
- **Wi-Fi ADB** – wireless debugging (turns USB debugging on first)

## Setup

1. Open the project in Android Studio (or add a Gradle wrapper and run `./gradlew assembleDebug`) and install it.
2. Grant the secure-settings permission once from a computer:

   ```
   adb shell pm grant com.example.quicktoggles android.permission.WRITE_SECURE_SETTINGS
   ```
3. Open the app, tap **Allow display over other apps**, then **Show on-screen toggles**.

Drag the panel by its title; ✕ hides it.

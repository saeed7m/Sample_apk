# Sample GPS APK

A minimal Android app written in Kotlin. It requests location permission and shows the phone's current latitude, longitude, accuracy, provider, and an approximate address when available.

## Build automatically on GitHub

Every push to `main` runs GitHub Actions and creates a downloadable artifact named `sample-gps-debug-apk`.

1. Open the repository on GitHub.
2. Open **Actions**.
3. Select the latest successful **Build Android APK** run.
4. Download `sample-gps-debug-apk` from the **Artifacts** section.
5. Extract the ZIP and install `app-debug.apk` on the Android phone.

Android may ask you to allow installation from the browser or file manager used to open the APK.

## Privacy

This test version only displays the location on the phone. It does not send location data to any server.

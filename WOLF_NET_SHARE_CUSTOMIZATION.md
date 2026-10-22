# Wolf Net Share customization

Changes made:
- Renamed the Android app label to **Wolf Net Share** (including the quick-settings tile label).
- Replaced launcher icons in all legacy density folders and the adaptive launcher foreground with the supplied wolf artwork.
- Updated the built-in light/dark palettes to a deep navy + electric blue/cyan look inspired by the reference screenshot.
- Preserved the existing theme selector, so System / Light / Dark appearance options remain available.

## Build APK
1. Open this project folder in Android Studio.
2. Allow Gradle sync to complete; install the Android SDK/NDK versions required by `app/build.gradle.kts`.
3. Use **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. For command line, run `./gradlew assembleDebug` from the project root. The debug APK is normally at `app/build/outputs/apk/debug/app-debug.apk`.

These changes have not been verified with a full Android build in this environment.

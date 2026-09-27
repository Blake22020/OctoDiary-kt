# Development guide

## Toolchain

- JDK 17
- Android SDK Platform 36
- Android Gradle Plugin and Kotlin versions declared in the version catalog and root Gradle build files

The app supports Android API 26 and later. Gradle wrapper files are committed; use `./gradlew` rather than installing a separate Gradle version.

## Build and checks

```bash
./gradlew testDebugUnitTest assembleDebug
```

The installable debug APK is at `app/build/outputs/apk/debug/`. To run instrumented tests on a connected emulator or device:

```bash
./gradlew connectedDebugAndroidTest
```

GitHub Actions runs `assembleDebug` on pushes to the app-maintenance branches and publishes the APK as a `OctoPlan-debug` workflow artifact. Artifacts are intended for testing, are signed with the standard debug key, and expire according to GitHub Actions artifact-retention settings.

## Project layout

- `app/src/main/java/org/bxkr/octodiary/screens` — diary screens, dashboard, and planner UI.
- `app/src/main/java/org/bxkr/octodiary/components` — reusable Compose components and settings.
- `app/src/main/java/org/bxkr/octodiary/network` — HTTP clients and school-service API interfaces.
- `app/src/main/java/org/bxkr/octodiary/models` — API response models.
- `app/src/main/res` — localized strings, themes, icons, and other Android resources.
- `api.md` — known service endpoints and links to their implementation.

The Kotlin namespace remains `org.bxkr.octodiary` to avoid a risky package-wide migration of the inherited code. The installed application ID is separately set to `app.octoplan.student`.

## Release builds

The automated workflow creates a debug APK only. A distributable release requires a maintainer-owned signing key and a release process configured outside this repository. Never commit keystores, passwords, or signing credentials. Increment `versionCode` and update `versionName` in `app/build.gradle.kts` for a new app release; also keep the artifact and release notes accurate.

## School API changes

These services are external and can change without notice. Keep region-specific behavior explicit, handle unavailable endpoints as unavailable rather than valid empty data, avoid logging authentication data or student records, and update [`api.md`](../api.md) when endpoint behavior changes.

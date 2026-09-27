# OctoPlan

**An independent Android school diary and study planner based on the open source OctoDiary project.** Maintained by Blake22020 for personal use. OctoPlan is not affiliated with the developers of Moscow Electronic School or My School.

[Русская версия](README.md) · [Download APK](#install-the-apk) · [Report an issue](https://github.com/Blake22020/OctoPlan/issues)

![APK build](https://github.com/Blake22020/OctoPlan/actions/workflows/build.yml/badge.svg?branch=feat/octoplan-student-fork)

## Features

- **Diary:** timetable, grades, attendance, homework, school information, and meal balances where the regional service supports them.
- **Home screen:** next lesson, unfinished assignments, personal tasks, and a compact grade summary.
- **Planner:** personal tasks with a subject and due date are stored on the device and work offline; diary assignments are shown separately.
- **Class ranking:** classmate names are loaded from school-service data when available. Search by name or student ID, and reveal IDs on demand.
- **Appearance:** light and dark themes, color palettes, typography and layout density controls, plus importing a custom TTF or OTF font file.
- **Widget:** a quick view of the school timetable on the Android home screen.

Diary data comes from the connected school service. Available sections and API responses vary by region, profile, and service availability; OctoPlan cannot guarantee third-party API uptime. Personal planner tasks are not sent to the school service.

## Install the APK

1. Open the [latest GitHub Actions builds](https://github.com/Blake22020/OctoPlan/actions/workflows/build.yml?query=branch%3Afeat%2Foctoplan-student-fork).
2. Choose a successful run and download the `OctoPlan-debug` artifact.
3. Extract the ZIP archive and install the APK on Android 8.0 or later.

This is a debug build for testing. It uses the separate application ID `app.octoplan.student.debug`, so it can be installed alongside the original OctoDiary app. GitHub Actions signs it with Android's test key; make sure the artifact came from this repository before installing it.

## Build from source

You need JDK 17 and Android SDK Platform 36. From the project root, run:

```bash
./gradlew testDebugUnitTest assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/`. Device tests are available through `connectedDebugAndroidTest`.

See the [development guide](docs/DEVELOPMENT.md) for project structure, checks, and build notes. The [API notes](api.md) describe the school-service endpoints used by the app.

## Compatibility

- Android 8.0 (API 26) and later.
- Main school services: Moscow Electronic School and My School in the Moscow region. Individual endpoints may differ by region.
- Release application ID: `app.octoplan.student`; debug builds add the `.debug` suffix.

## Contributing

Report bugs or suggest changes in [Issues](https://github.com/Blake22020/OctoPlan/issues). Read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a change.

## Origin and license

OctoPlan is based on [OctoDiary-kt](https://github.com/OctoDiary/OctoDiary-kt). Original attribution and copyright notices are preserved. The source is distributed under the MIT License; see [`LICENSE`](LICENSE). OctoPlan is an independent fork by Blake22020 and is not an official OctoDiary, MES, or My School product.

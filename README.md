# OctoPlan

OctoPlan is an independent Android fork of [OctoDiary](https://github.com/OctoDiary/OctoDiary-kt), focused on combining the school diary with a personal study planner.

## What makes this fork different

- A new home screen with the next lesson, unfinished school assignments, a personal-task count, and a compact grade overview.
- A planner with two lists: assignments from the connected diary and personal tasks stored locally on the device.
- Personal tasks work offline and are not uploaded to the school service.
- A separate Android application ID, name, launcher icon, theme defaults, and build artifact, so OctoPlan can be installed next to the original app.
- Existing diary functions remain available: schedule, marks, profile, school data, and the status widget.

The app reads school information from the selected school service. Availability depends on that service and region; unsupported sections should be reported as unavailable instead of being shown as empty data.

## Build

```bash
./gradlew assembleDebug
```

The installable debug APK is written to `app/build/outputs/apk/debug/`. Pushes to `feat/octoplan-student-fork` build and upload an `OctoPlan-v0.1.0-debug` artifact through GitHub Actions.

## Upstream and license

This project is derived from OctoDiary. The upstream MIT license and copyright notice are retained in [`LICENSE`](LICENSE). OctoPlan changes are maintained in this fork; upstream developers and contributors remain credited in the app.

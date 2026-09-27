# Contributing to OctoPlan

Thanks for helping improve OctoPlan. The app depends on external school services, so reports that include the selected region, the screen or action, and a redacted error message are especially useful.

## Before opening an issue

- Search existing [issues](https://github.com/Blake22020/OctoPlan/issues) for the same problem.
- Include the Android version, app version, school-service region, and reproducible steps.
- Remove names, student IDs, tokens, screenshots with personal data, and other private school information from logs and attachments.

## Code changes

1. Create a topic branch from `feat/octoplan-student-fork`.
2. Keep changes focused and preserve the upstream MIT copyright and license notices in reused code.
3. Run `./gradlew testDebugUnitTest assembleDebug` before submitting.
4. Describe user-visible changes and any region-specific API assumptions in the pull request.

Do not commit credentials, access tokens, personal student data, local SDK paths, or signing keys. Changes to school-service endpoints should include the affected region and a reference to the relevant implementation in [`api.md`](api.md).

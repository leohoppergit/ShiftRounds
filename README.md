# ShiftRounds

<p align="center">
  <img src="release-assets/rendered/shiftrounds-icon-1.0.0.png" alt="ShiftRounds Icon" width="128" />
</p>

<p align="center">
  <a href="https://apps.obtainium.imranr.dev/redirect.html?r=obtainium://add/https://github.com/leohoppergit/ShiftRounds">
    <img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="54" />
  </a>
</p>

ShiftRounds is a privately maintained shift calendar app based on ShiftCal / ShiftSwift by Nulide.

## Credit

Original concept and core foundation remain fully credited to [Nulide / ShiftSwift (f.k.a. ShiftCal)](https://gitlab.com/Nulide/ShiftCal).

ShiftRounds continues that base as a separately maintained app line with its own UI, release cycle, and feature direction.

## Current App Identity

- Package name: `app.shiftrounds.android`
- Current standalone version line: `1.x.x`
- First standalone release: `1.0.0`

## Features

- Create, edit, archive, duplicate, and reorder your shifts
- Assign shifts directly in the calendar
- Add day notes, monthly notes, overtime, and "left early" adjustments
- Optional overtime multiplier for manually entered overtime
- Configurable special accounts tracked separately from normal work time
- Local calendar export and sharing as PDF or ICS
- Full backup and restore of settings, shifts, notes, and calendar entries
- Austrian public holidays in the calendar
- Austrian school holidays by selected federal states
- Custom date markers for school breaks, kindergarten closures, and personal ranges
- Dark mode with the same core design language as the day theme
- Support for shifts longer than 24 hours

## Privacy / Data Handling

- No internet permission is currently required for the shipped app
- No cloud sync or remote telemetry is active
- Android system auto-backup is disabled
- Export, restore, and sharing are handled locally on-device
- Optional school holiday online update infrastructure exists in code, but is not active in the shipped app

## Release Signing

1. Copy `keystore.properties.example` to `keystore.properties`
2. Create your own release keystore
3. Fill in the values in `keystore.properties`
4. Build a release APK with `./gradlew assembleProdRelease`

If `keystore.properties` is missing, release builds fall back to the debug key. That is useful for local testing, but not for real distribution.

## Versioning

ShiftRounds now uses its own standalone versioning scheme:

- `versionName`: `1.0.0`
- `versionCode`: `10000000`

Recommended pattern:

- `1.0.0` -> `10000000`
- `1.0.1` -> `10000001`
- `1.1.0` -> `10100000`
- `2.0.0` -> `20000000`

Rules:

- Always increase `versionCode`
- Keep `versionName` human-readable
- Publish exactly one APK per GitHub Release

## Obtainium

The easiest update path is GitHub Releases:

1. Build and sign a release APK
2. Upload it to a GitHub Release
3. Add the repository to Obtainium
4. Publish future versions as new GitHub Releases

Recommended release tag format:

- `v1.0.0`
- `v1.0.1`

Recommended GitHub Release title:

- `ShiftRounds 1.0.0`

Recommended asset:

- `app-prod-release.apk`

## Development Notes

- `prod` is the main release flavor
- The app is intended to stay focused, offline-friendly, and practical in everyday use
- Cleanup of legacy code and dead UI paths is an active maintenance goal

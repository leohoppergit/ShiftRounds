# ShiftRounds

A privately maintained shift calendar fork based on ShiftCal / ShiftSwift by Nulide.

## Credit

ShiftRounds is based on the original ShiftCal / ShiftSwift project by Nulide.
Original concept and foundation by Nulide.

## Fork Notes

- This fork is intended as a personally maintained version.
- The app id of this fork is `app.shiftrounds.android`.
- Alarm functionality has been removed in this fork.

## Features

- Create your Shifts
- Add multiple shifts at once
- Synchronize with other people
- Personalize the calendar
- Enable DoNotDisturb while working
- See your shifts in your local calendar
- Add day notes and time adjustments
- Support shifts longer than 24 hours

## Release Signing

1. Copy `keystore.properties.example` to `keystore.properties`
2. Create your own release keystore
3. Fill in the values in `keystore.properties`
4. Build a release APK with `./gradlew assembleProdRelease`

If `keystore.properties` is missing, release builds fall back to the debug key. That is useful for local testing, but not for real distribution.

## Versioning

This fork uses its own versioning scheme:

- `versionName`: `3.0.1-shiftrounds.6`
- `versionCode`: `3001006`

Recommended pattern:

- `3.0.1-shiftrounds.1` -> `3001001`
- `3.0.1-shiftrounds.2` -> `3001002`
- `3.0.2-shiftrounds.1` -> `3002001`

Rule:

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

- `v3.0.1-shiftrounds.1`
- `v3.0.1-shiftrounds.2`

Recommended GitHub Release title:

- `ShiftRounds 3.0.1-shiftrounds.1`

Recommended asset:

- `app-prod-release.apk`

Suggested release notes template:

- `ShiftRounds private fork release`
- `Based on ShiftCal / ShiftSwift by Nulide`
- `Main changes in this release: ...`

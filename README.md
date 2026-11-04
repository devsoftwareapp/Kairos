# Kairos

**Geleceğe bir mektup.** Kairos is a Turkish-first Android time-capsule app.

## Current implementation
- Kotlin + Jetpack Compose interface (Turkish)
- Room database for persistent capsule metadata
- AES-256-GCM encryption for capsule titles and payload files
- Per-capsule content keys wrapped by a non-exportable Android Keystore AES key
- Letter text capsules
- Photo selection and encrypted photo storage
- Audio file import, microphone recording, encrypted audio storage and playback after opening
- BiometricPrompt gate before locally eligible capsule content is decrypted
- WorkManager reminders scheduled for the selected opening date
- Room 1-to-2 migration for the initial database schema

## Build
- JDK 17
- Android SDK Platform 36
- Gradle 8.14
- Android Gradle Plugin 8.13.2

GitHub Actions builds a debug APK artifact on pushes to `main`.

## Security limitations — read before use
- The current opening-date check uses the device clock. It is **not** a cryptographic time lock and can be bypassed by changing device time.
- BiometricPrompt is an app-level authentication gate; it is not itself a time-lock protocol.
- The wrapping key is device-bound. Clearing app data, uninstalling, losing the device, or resetting the device can make capsules unrecoverable.
- No recovery phrase, cross-device key recovery, Google Drive backup, or remote time-lock service is integrated yet.
- Capsule title, type, and date metadata are stored in Room; the title and payload bytes are encrypted.
- The project has not undergone an independent security audit. Do not use it as the only copy of irreplaceable data.

## Package
`com.devsoftware.kairos`

## License
All rights reserved until a license is added.

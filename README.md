# Kairos

**A letter to the future.** Kairos is an ad-free Android time-capsule app.

## Stack
- Kotlin + Jetpack Compose
- Room
- Android Keystore / AES-GCM foundation
- WorkManager (planned)
- BiometricPrompt (planned)
- Google Drive encrypted backup (planned)

## Build
Open this project in the latest stable Android Studio, allow Gradle sync, then run the `app` configuration.

Requirements:
- JDK 17
- Android SDK Platform 36
- Android Gradle Plugin 8.13.2

## Security status
This repository is the initial application scaffold. The current UI is a prototype shell.
It does **not** yet provide a cryptographically enforced time lock, remote time attestation,
Drive backup, biometric-gated decryption, or production-ready recovery. Do not store
irreplaceable private content in this initial build.

## Package
`com.kairos.app`

## License
All rights reserved until a license is added.

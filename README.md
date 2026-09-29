# Stickerport

Telegram → WhatsApp sticker pack transfer for Android. Native Android, Kotlin + Jetpack Compose,
no backend and no accounts: you supply your own Telegram bot token and the app talks to Telegram
directly.

## Status

Sprint 0 — project skeleton. See `plan.md` (the agile plan) and `spec.md` (the product spec).

## Modules

| Module | Purpose |
|---|---|
| `:app` | Everything Kotlin/Compose, organised by package. |
| `:native-webp` | CMake build of pinned libwebp + JNI wrapper. Isolated so NDK build times don't affect normal builds. |

## Requirements

- **JDK 17+** — nothing to install on a machine with mise; otherwise JDK 21 is what this is built
  against (AGP 9 requires 17 minimum).
- **Android SDK** with `platforms;android-37.2` and `build-tools;37.0.0`.
- Point Gradle at your SDK with either `ANDROID_HOME` or a `local.properties` containing
  `sdk.dir=/path/to/Android/Sdk`. `local.properties` is git-ignored; it is machine-specific.

No environment variables are needed on a machine with mise shims on `PATH` — `java` resolves from
the repo's `.mise.toml`, which pins `java = "21"`.

## Build

```bash
./gradlew assembleDebug      # debug APK  -> app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease    # minified + resource-shrunk unsigned release APK
./gradlew test               # unit tests (none yet — T0.5 wires up the libraries)
./gradlew lint               # Android Lint
```

The release variant is unsigned on purpose: signing is set up in T14.2 (Play readiness), not now.
T0.1 only requires that `assembleRelease` *builds* and R8 does not break the app.

## Run it

The AVD `sp_api26` (API 26, x86_64) already exists. If the emulator is not running:

```bash
~/Android/Sdk/emulator/emulator -avd sp_api26 -no-window -no-audio -no-boot-anim -no-snapshot
```

Then:

```bash
./gradlew installDebug
adb shell am start -n app.stickerport/.MainActivity
```

You should see the app title, a one-line description, and — on Android 12+ — a colour scheme
derived from your wallpaper (Material You), with a brand fallback on older releases. Everything
sits clear of the status bar and navigation bar. This is still a placeholder body; the real Home
screen arrives in T4.1 and navigation in T0.3.

## Test on a real phone

Requirements: **Android 8.0 (API 26) or newer** — that is the app's `minSdk`.

1. On the phone: Settings → About → tap **Build number** 7× to unlock Developer options, then
   Developer options → **USB debugging** on. (Xiaomi/MIUI also has a "Verify apps over USB"
   toggle that must be off or installs fail.)
2. Plug it in, accept the **Allow USB debugging?** fingerprint prompt on the phone.
3. Confirm the phone is actually visible:
   ```bash
   adb devices -l          # must say "device", not "unauthorized" or "offline"
   adb shell getprop ro.build.version.sdk   # must be >= 26
   adb shell getprop ro.product.cpu.abilist # arm64-v8a on any modern phone; fine, the APK is universal
   ```
4. Install and launch:
   ```bash
   ./gradlew installDebug
   adb shell am start -n app.stickerport/.MainActivity
   ```
   Or without Gradle: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
5. Watch it live:
   ```bash
   adb logcat --pid=$(adb shell pidof app.stickerport)   # app-only logs
   adb logcat -b crash                                   # fatal exceptions
   ```

**No cable? Android 11+ wireless debugging:** Settings → Developer options → Wireless debugging →
Pair device with pairing code, then `adb pair <ip:port>` and `adb connect <ip:port>`.

**Remove it again:** `adb uninstall app.stickerport`

### Do not install the release APK

`app-release-unsigned.apk` has no signing certificate, so it fails with
`INSTALL_PARSE_FAILED_NO_CERTIFICATES`. Use the **debug** APK on the phone. Release signing is
T14.2.

### There are two `adb` binaries on this machine

`/usr/bin/adb` (from the `android-tools` package, 37.0.0) and
`~/Android/Sdk/platform-tools/adb` (37.0.1). Gradle uses the SDK one. Mixing them can produce
"adb server version doesn't match" or devices vanishing. Prefer the SDK one:

```bash
export PATH="$HOME/Android/Sdk/platform-tools:$PATH"   # put this ahead of /usr/bin
hash -r
```

## Check the build did what it claims

```bash
# package name, min/target SDK — must not contain "WhatsApp"
~/Android/Sdk/build-tools/37.0.0/aapt2 dump badging \
  app/build/outputs/apk/release/app-release-unsigned.apk | head -5

# R8 actually ran
ls app/build/outputs/mapping/release/mapping.txt

# nothing crashed on launch
adb logcat -b crash
```

## Versions

Pinned in `gradle/libs.versions.toml`. Current: AGP 9.4.1, Gradle 9.8.0, Kotlin 2.4.20,
`minSdk` 26, `targetSdk` 36, `compileSdk` 37.2.

Google Play requires new apps and updates to target API 36 as of 2026-08-31, so 36 is the
`targetSdk`. We compile against 37.2 (Android 17.2) because `androidx.core:core-ktx` requires
`compileSdk >= 37`; compiling against a newer platform than you target is supported and keeps the
app off the Android 17 behaviour-change list until it has been tested.

AGP 9 has **built-in Kotlin** — do not apply `org.jetbrains.kotlin.android`. The Kotlin version is
set through a `buildscript` classpath in the root `build.gradle.kts`; without it AGP forces its own
bundled KGP.

## Notes

- The app name and the application ID must never contain "WhatsApp" (brand guidelines). The
  integration points are called "WhatsApp" in code, but the user-visible name and package are
  `Stickerport` / `app.stickerport`.
- The only network destination this app is allowed to contact is `api.telegram.org` (NFR-5).

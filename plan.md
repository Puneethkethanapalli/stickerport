# Stickerport — Agile Workflow Plan (`plan.md`)

> **Source of truth for *what* to build:** `spec.md` (the Stickerport spec v0.2). This file (`plan.md`) is the *how and when*: sprints → small tasks → checklists. Section refs like `§9.6` and IDs like `FR-7` point into **`spec.md`**.
> **This file is a living document.** Every chat must read it first and update it before ending.

---

## 0. START HERE (read this at the beginning of every new chat)

**Current position** _(update at the end of every session)_

- **Current sprint:** Sprint 0
- **Next task to do:** `T0.3`
- **Tasks done:** 2 / 66
- **Blocked on:** nothing
- **Last session:** 2026-09-29 — T0.2 done (Material 3 theme, edge-to-edge, predictive back; verified light+dark on API 26 and a real API 36 phone)

**This machine's toolchain** _(set up in T0.1; reuse it, don't rediscover it)_

**No environment variables are needed.** mise shims are already on `PATH` globally, so `java`
resolves to 21.0.2 (pinned by `.mise.toml`) in any shell, and the git-ignored `local.properties`
(`sdk.dir=/home/punee/Android/Sdk`) tells Gradle where the SDK is. Verified with
`env -i HOME=$PATH bash -lc './gradlew projects'`. Just run `./gradlew …`.

- **JDK 21.0.2** via mise at `~/.local/share/mise/installs/java/21.0.2`. There is **no system JDK**
  and **no passwordless sudo**, so use mise, not `pacman`. If the mise shims ever vanish from
  `PATH`, `export JAVA_HOME="$HOME/.local/share/mise/installs/java/21.0.2"` is the fallback.
- **Android SDK** at `~/Android/Sdk` (user-local): `platform-tools`, `platforms/android-36`,
  `platforms/android-37.2`, `build-tools/36.0.0`, `build-tools/37.0.0`,
  `ndk/28.2.13676358` (r28c), `emulator`, AVD `sp_api26` (API 26, google_apis x86_64).
  `/dev/kvm` is present, so the emulator runs at full speed.
- **Real phone (USB, always attached):** OnePlus **CPH2691**, **Android 16 / API 36**, `arm64-v8a`,
  1264×2780 @ 560 dpi, serial **`19eb9d18`**, not a low-RAM device. This is the primary manual-QA
  device and it covers the "API 34+" half of T0.2's matrix by itself; `sp_api26` covers the
  minSdk end. Re-check the serial with `adb devices -l` — it changes if the phone is re-paired.
- **Both are attached at once.** `./gradlew installDebug` installs to *all* connected devices
  (`Installed on 2 devices.`) rather than erroring. Target one with `adb -s <serial> …`, or set
  `ANDROID_SERIAL=19eb9d18` for Gradle tasks.
- Only needed when re-provisioning the SDK:
  `export PATH="$HOME/Android/Sdk/cmdline-tools/latest/bin:$PATH"` for `sdkmanager` / `avdmanager`.

### Session start protocol
1. Read this whole file, then the relevant sections of **`spec.md`** for the task you're about to do (use the `Refs` line on the task).
2. Read **§2 Global lessons** and the **"Things to remember"** of the last 3 completed tasks.
3. Pick the first unchecked task whose `Depends on` are all checked. Don't skip ahead unless the user says so.
4. Before writing code, do the task's **Context7 lookups** (see §1).
5. Work through the task's **Steps** checklist, ticking boxes as you go.

### Session end protocol (do this even if the task is unfinished)
1. Tick finished step boxes. Tick the task heading box **only** if every step is done **and** `How to test` passes.
2. Fill **Mistakes made during the task** honestly (wrong assumptions, wrong API usage, things that had to be redone). Empty is fine only if it's true.
3. Fill **Things to remember for future tasks** (gotchas, decisions, versions, commands). If a lesson applies beyond this task, also copy it into **§2 Global lessons**.
4. Update **Current position** above, the **§3 Dashboard**, and add a row to **§9 Session log**.
5. Commit with the message format `T<sprint>.<n>: <short summary>`.

### Task Definition of Done (every task)
- [ ] All Steps ticked; code compiles; `./gradlew test` (and `lint` if configured) green
- [ ] `How to test` executed and passing (state what you ran)
- [ ] No secrets, tokens, or full Telegram URLs in logs or commits
- [ ] Mistakes + Remember sections filled; dashboard and current position updated

### Legend
`[ ]` not done · `[x]` done · 🔬 spike (time-boxed experiment with pass/fail criteria) · 🧪 acceptance gate (must pass before the next sprint) · 📌 fact from `plan.md` · ➕ lesson learned while building

---

## 1. Context7 MCP rules (mandatory)

Android/Kotlin library APIs change often, and plan.md itself flags several items as "check current status". **Never write library API code from memory.** Use the Context7 MCP server for current docs.

**Procedure for every task with a `Context7:` line**
1. Resolve the library ID (`resolve-library-id`), then fetch the docs for the specific topic (`get-library-docs` / `query-docs`, depending on the Context7 version installed). Ask for the narrow topic, not the whole library.
2. Prefer the version in `gradle/libs.versions.toml`. If docs and the pinned version disagree, say so in the task's *Things to remember*.
3. Add `use context7` to your own working prompt so the lookup actually happens.
4. Record resolved IDs in the cache table below so later chats skip step 1.
5. If Context7 has no entry (likely for `WhatsApp/stickers`, Telegram Bot API), fall back to the official docs/README and note that in the task.

**Context7 library ID cache** _(fill in as you resolve; format is usually `/org/project`)_

| Library | Context7 ID | Used in |
|---|---|---|
| Android (official docs: AGP, Gradle, Jetpack, testing) | `/websites/developer_android` | T0.1–T0.5 and most later tasks |
| Android Gradle Plugin / Gradle Kotlin DSL / version catalogs | ⚠️ **not indexed** — `npx ctx7 library "Android Gradle Plugin"` returns only unrelated third-party plugins. Fall back to `developer.android.com` + the AGP 9.4.1 Gradle API reference + reading the build empirically | T0.1 |
| Jetpack Compose + Material 3 (BOM) | ⚠️ **effectively not indexed for the Compose APIs.** `ctx7 docs /websites/developer_android` returned the *Views* `DynamicColors` class and the *Fragments* `OnBackStackChangedListener`, not the Compose KTX equivalents. Use `developer.android.com/develop/ui/compose/...` and let the compiler check the code | T0.2 |
| Navigation Compose (type-safe routes) | — | T0.3 |
| Hilt | — | T0.4 |
| Room | — | T0.4, T3.1 |
| DataStore | — | T0.4, T1.3 |
| OkHttp | — | T1.2 |
| kotlinx.serialization | — | T1.2 |
| Tink (Android Keystore AEAD) | — | T1.3 |
| WorkManager | — | T6.1 |
| Coil 3 | — | T10.2 |
| lottie-android | — | T8.1 |
| MockK / Turbine / Kotest / JUnit5 | — | T0.5 |
| Android NDK / CMake / libwebp | — | T2.2 |
| WhatsApp/stickers (likely not indexed → use repo README) | — | T2.1 |

---

## 2. Global lessons (promote important lessons from tasks here)

_Newest first. One line each, with the task ID._

- ➕ **(T0.2) Edge-to-edge is *enforced* on Android 15+ once `targetSdk >= 35`** — no opt-out. That is why the T0.1 placeholder had its text under the status bar. Never put content directly under `setContent`; go through `AppShell`.
- ➕ **(T0.2) `Scaffold` already consumes `WindowInsets.systemBars`.** Adding `.systemBarsPadding()` on top double-insets. Let the `PaddingValues` it passes to `content` do the work.
- ➕ **(T0.2) The Compose compiler plugin is still required under AGP 9 built-in Kotlin** — `alias(libs.plugins.kotlin.compose)` + `buildFeatures { compose = true }`.
- ➕ **(T0.2) Context7 answers Compose questions with the *Views/Fragments* API.** Asked twice and got `DynamicColors` (Views) and `OnBackStackChangedListener` (Fragments) instead of the Compose KTX equivalents. Use developer.android.com + the compiler.
- ➕ **(T0.2) `adb exec-out screencap` can come back rotated** if the device has auto-rotate on. Not a layout bug — check `settings get system accelerometer_rotation` before debugging the app.

- ➕ **(T0.1) The two repo docs are named the opposite way to what this file's header claimed.** `plan.md` *is* this agile plan; the product spec v0.2 is **`spec.md`**. Every `§x` / `FR-x` / `NFR-x` / `Appendix` ref in this file points into `spec.md`. Header corrected in T0.1.
- ➕ **(T0.1) No system JDK, no Android SDK and no passwordless sudo on this machine.** Install the JDK with `mise install java@21` (`~/.local/share/mise/installs/java/21.0.2`) and the SDK user-locally into `~/Android/Sdk` from Google's `commandlinetools` zip. `pacman` needs a password.
- ➕ **(T0.1) AGP 9 has *built-in Kotlin*: never apply `org.jetbrains.kotlin.android`.** Put the Kotlin version in the root `buildscript { dependencies { classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:<v>") } }` instead — otherwise AGP silently forces its bundled KGP 2.2.10.
- ➕ **(T0.1) AGP 9's `android {}` is a new DSL with the legacy types hidden.** `minSdk = 26`, `targetSdk = 36`, `compileSdk = 37` + `compileSdkMinor = 2`, `applicationId`, `versionCode`/`versionName` are plain `Int?`/`String?` properties (inherited via `BaseFlavor` / `ApplicationBaseFlavor`) and still work. The old `applicationVariants` / `variantFilter` variant API is **gone** — use `androidComponents.onVariants`.
- ➕ **(T0.1) `compileSdk` 37.2 and `targetSdk` 36 are deliberately different.** Play mandates target 36 (since 2026-08-31), but `androidx.core:core-ktx:1.19.0` *requires* `compileSdk >= 37`. Compile against the newest platform, target 36. API 37 has minor versions, so the SDK package is `platforms;android-37.2` — there is no `platforms;android-37`.
- ➕ **(T0.1) The Gradle wrapper's default `networkTimeout=10000` is too short** for the 145 MB Gradle distribution on this connection. Bumped to `120000` with `retries=3`.
- ➕ **(T0.1) KSP no longer versions in lockstep with Kotlin** — `com.google.devtools.ksp` latest is `2.3.12` while Kotlin is `2.4.20`. Don't derive a KSP version from the Kotlin version. (T0.4 must confirm the pair actually builds.)
- ➕ **(T0.1) Verify every version-catalog coordinate against its real repository before committing it.** A guessed version only fails at *use* time, in a later sprint.
- ➕ **(T0.1) This machine needs no `JAVA_HOME` or `ANDROID_HOME` export** — mise shims are already global and `local.properties` carries `sdk.dir`. Don't re-add exports that aren't needed. Sanity check: `env -i HOME=$PATH bash -lc './gradlew projects'`.
- ➕ **(T0.1) Two `adb` binaries here:** `/usr/bin/adb` (pacman `android-tools` 37.0.0) and `~/Android/Sdk/platform-tools/adb` (37.0.1). Gradle uses the SDK one; mixing them can cause "server version doesn't match" or devices vanishing. Put `$HOME/Android/Sdk/platform-tools` ahead of `/usr/bin` on `PATH`.
- ➕ **(T0.1) `app-release-unsigned.apk` cannot be side-loaded** — fails with `INSTALL_PARSE_FAILED_NO_CERTIFICATES`. Test the **debug** APK on a device until T14.2 adds a signing config.
- ➕ **(T0.1) The manual-QA device floor is API 26 (Android 8.0).** AVD `sp_api26` covers that end; T0.2 also needs a second, API 34+ AVD.
- ➕ **(T0.1) `./gradlew installDebug` installs to *all* attached devices and does not error** — with the emulator and the phone both up it prints `Installed on 2 devices.` Disambiguate with `adb -s <serial>` or `ANDROID_SERIAL`.
- ➕ **(T0.1) The T0.1 placeholder does not handle window insets**, so on a real phone the label renders under the status bar. That is the visible symptom T0.2 must fix, not a regression.

---

## 3. Dashboard

| Sprint | Milestone | Focus | Tasks | Done |
|---|---|---|---|---|
| [ ] Sprint 0 | M0 | Project skeleton | T0.1–T0.5 | 2/5 |
| [ ] Sprint 1 | M1a | Link parsing, Telegram client, token store | T1.1–T1.5 | 0/5 |
| [ ] Sprint 2 | M1b | Native WebP + static conversion + validator | T2.1–T2.6 | 0/6 |
| [ ] Sprint 3 | M1c | Chunking, ContentProvider, WhatsApp handoff | T3.1–T3.6 | 0/6 |
| [ ] Sprint 4 | M1d 🧪 | Minimal UI + static end-to-end on real devices | T4.1–T4.5 | 0/5 |
| [ ] Sprint 5 | M2a | Token onboarding, share/deep-link entry | T5.1–T5.5 | 0/5 |
| [ ] Sprint 6 | M2b | Background work, notifications, resume | T6.1–T6.4 | 0/4 |
| [ ] Sprint 7 | M2c 🧪 | Preview deselection, error polish, **v1 DoD** | T7.1–T7.5 | 0/5 |
| [ ] Sprint 8 | M3a | TGS frame source, resampling | T8.1–T8.3 | 0/3 |
| [ ] Sprint 9 | M3b | Animated WebP encoder + budget fitting | T9.1–T9.4 | 0/4 |
| [ ] Sprint 10 | M3c 🧪 | Animated packs end-to-end | T10.1–T10.3 | 0/3 |
| [ ] Sprint 11 | M4 🧪 | Accessibility, settings, onboarding polish, perf | T11.1–T11.5 | 0/5 |
| [ ] Sprint 12 | M5a | WEBM alpha spike + frame source | T12.1–T12.2 | 0/2 |
| [ ] Sprint 13 | M5b 🧪 | WEBM integration | T13.1–T13.3 | 0/3 |
| [ ] Sprint 14 | M6 | Play Store readiness | T14.1–T14.5 | 0/5 |

**Release gates:** v1 = end of Sprint 7 · v2 (animated TGS) = end of Sprint 11 · v3 (WEBM) = end of Sprint 13 · Store = Sprint 14

---

## 4. Task template (copy for any new task)

```markdown
### [ ] T<sprint>.<n> — Title
**Refs:** §x, FR-x · **Context7:** libs to look up · **Depends on:** T…

**Goal of the task:** one or two sentences; what "done" means.

**Steps**
- [ ] …

**How to test:** exact commands / manual checks / expected result.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 …
- ➕ _Add lessons after finishing._
```

---

## 5. Sprints

# Sprint 0 — Project skeleton (M0)
**Sprint goal:** App launches with theme, navigation, DB, DI, and green CI. **Demo:** install debug APK, navigate between placeholder screens.

### [x] T0.1 — Project setup and version catalog
**Refs:** §6, §7.1, NFR-1 · **Context7:** Android Gradle Plugin, Gradle Kotlin DSL / version catalogs · **Depends on:** —

**Goal of the task:** A building multi-module Gradle project (`:app`, `:native-webp` stub) with a version catalog, package `app.stickerport`, minSdk 26 and the latest stable targetSdk.

**Steps**
- [x] Create project with Kotlin DSL, `gradle/libs.versions.toml`, package/applicationId `app.stickerport` (must not contain "WhatsApp")
- [x] Add empty `:native-webp` library module (no CMake yet)
- [x] Set minSdk 26; check current Play targetSdk requirement and set it
- [x] Enable R8/minify for release, keep debug fast
- [x] Add `.gitignore`, README stub

**How to test:** `./gradlew assembleDebug assembleRelease` succeeds; app installs and shows a blank screen on a minSdk-26 emulator.

**Actual result:** `./gradlew assembleDebug assembleRelease test lint` all green. Release APK 65 KB (R8 + resource shrinking, `mapping.txt` written); debug APK 2.9 MB. `aapt2 dump badging` on the release APK confirms `name='app.stickerport'`, `minSdkVersion:'26'`, `targetSdkVersion:'36'`, `compileSdkVersion='37'`, `application-label:'Stickerport'` — no "WhatsApp" anywhere user-visible.

Installed and launched on **two** targets, clean on both:
- AVD `sp_api26` (API 26, google_apis x86_64): `adb install -r` → `Success`; `am start -W` → `Status: ok`, `ThisTime: 258ms`; `mResumedActivity: app.stickerport/.MainActivity`; `logcat -b crash` empty; no `FATAL EXCEPTION`.
- Real phone OnePlus CPH2691 (Android 16 / API 36, arm64-v8a, serial `19eb9d18`): `./gradlew installDebug` → `Installed on 2 devices.`; `am start -W` → `Status: ok`; `topResumedActivity=app.stickerport/.MainActivity`; frame committed at 1264×2780; `logcat -b crash` empty.

Screenshots on both confirm the blank screen rendering the "Stickerport" label. On the phone the label
sits *under* the status bar — expected, because the T0.1 placeholder does not handle window insets.
Fixing exactly that is T0.2's job.

**Choices made (differ from a literal reading of the steps):**
- `compileSdk = 37` + `compileSdkMinor = 2` (Android 17.2), **`targetSdk = 36`**. Play requires 36; `androidx.core:core-ktx:1.19.0` refuses to build against 36. Compiling newer than you target is the normal, supported split.
- Kotlin **2.4.20** via a root `buildscript` classpath override, not AGP 9.4.1's bundled KGP 2.2.10.
- Release is deliberately **unsigned**; signing belongs to T14.2. `assembleRelease` still runs R8 + `lintVitalRelease`, which is the real proof the skeleton survives minification.
- `MainActivity` is a plain `android.app.Activity` showing a `TextView`. No Compose, no AppCompat, no third-party UI dependency — T0.2 owns the theme/shell and T0.4 the DI/Room stack. This keeps the skeleton honest about what it proves.
- `android:allowBackup="false"` for now. The token DataStore does not exist yet (T0.4) so there is nothing worth restoring, and T1.3 replaces this with explicit `dataExtractionRules` / `fullBackupContent` `<exclude>` entries once the path is concrete.

**Mistakes made during the task:**
- Assumed a JDK and an Android SDK existed. Neither did, and `sudo` needs a password. Cost ~10 minutes before I checked. **Always probe the toolchain before planning the build.**
- Wrote the whole version catalog from memory. `ksp = "2.4.20-2.0.4"`, `room = "2.9.0"`, `kotlinxSerialization = "1.9.1"` and `hilt = "2.57.2"` were wrong or stale. Wrote a script that HEADs every coordinate against Google Maven / Maven Central / the plugin portal, then fixed all four.
- Treated `compileSdk` as a plain number and left it at 36. The build failed with "requires … compile against version 37 or later". Android 17 was already out — I had assumed 36 was current because the Play requirement is 36. **compileSdk and targetSdk age differently.**
- Kept the Gradle wrapper's stock `networkTimeout=10000`, which timed out downloading Gradle itself.
- Wrote `data_extraction_rules.xml` excluding `domain="root"` (i.e. everything) while leaving `allowBackup="true"`. Contradictory and over-broad. Deleted the files and used `allowBackup="false"` instead.
- Wrote an `mipmap-anydpi-v26` adaptive icon only. Correct *because* `minSdk = 26`, but worth stating explicitly rather than leaving as a silent trap for a future minSdk change.

**Things to remember for future tasks:**
- 📌 App name and package ID must not contain "WhatsApp" (§ header, §19).
- 📌 Two Gradle modules only; don't add more for a solo build (§7.1).
- 📌 `spec.md` is the product spec; `plan.md` is this file. The header used to claim the opposite.
- 📌 AGP 9.4.1 / Gradle 9.8.0 / Kotlin 2.4.20 / KSP 2.3.12 / Hilt 2.60.1 / Room 2.8.5 / Compose BOM 2026.09.00 — all in `gradle/libs.versions.toml`, all verified to exist. `compileSdk` 37.2, `targetSdk` 36, `minSdk` 26.
- ➕ **AGP 9 built-in Kotlin: do not apply `kotlin("android")`.** It fails with *"The 'org.jetbrains.kotlin.android' plugin is no longer required"* and also *"Cannot add extension with name 'kotlin'"*. Use the root `buildscript` classpath to pick a KGP version.
- ➕ **kapt is incompatible with built-in Kotlin.** Use KSP, or `com.android.legacy-kapt` (same version as AGP). T0.4 needs this.
- ➕ **AGP 9 default behaviour changes to know about:** `android.newDsl=true`, `android.uniquePackageNames=true` (every module needs its own `namespace`), `android.useAndroidx=true`, `android.enableAppCompileTimeRClass=true`, default test runner is now `androidx.test.runner.AndroidJUnitRunner`, and `getDefaultProguardFile()` only accepts `proguard-android-optimize.txt`.
- ➕ **`applicationVariants` and `variantFilter` are removed in AGP 9.** Any plugin or build logic touching the old variant API must move to `androidComponents.onVariants` / `beforeVariants`.
- ➕ **R8 in AGP 9 is stricter:** `-keep class A` no longer implies `-keep class A { <init>(); }`, and `android.r8.strictFullModeForKeepRules` is on. Rules go in `app/proguard-rules.pro` as the reflection-heavy code lands; T14.2 smoke-tests the minified build.
- ➕ **The `native-webp` module has no CMake yet** (T2.2) and a `WebPNative` stub that reports `isAvailable = false`. Nothing in `:app` depends on it yet, so nothing breaks.
- ➕ `org.gradle.configuration-cache=true` is on and the build is config-cache clean so far. If a future plugin (Hilt, KSP, CMake) breaks it, that is the first thing to check.


### [x] T0.2 — Theme, edge-to-edge, app shell
**Refs:** §12 (global), §12.7 · **Context7:** Jetpack Compose, Material 3 · **Depends on:** T0.1

**Goal of the task:** Material 3 theme with dynamic color (Android 12+), light/dark, edge-to-edge, predictive back enabled.

**Steps**
- [x] Theme (color scheme, typography, shapes) with dynamic color fallback
- [x] `MainActivity` with `enableEdgeToEdge()` and a `Scaffold` shell
- [x] Enable predictive back in the manifest
- [x] Add a Compose `@Preview` for light and dark

**How to test:** Run on API 26 and API 34+ emulators; toggle dark mode; confirm content isn't hidden behind system bars.

**Actual result:** Verified on **both** required targets plus the real phone, light **and** dark, with no crash
on any of them (`logcat -b crash` = 0 lines everywhere).

| Target | Mode | Background pixel | Expected | Verdict |
|---|---|---|---|---|
| AVD `sp_api26` (API 26) | light | `#F2F4F3` | brand `CheckerLight` | ✅ brand fallback |
| AVD `sp_api26` (API 26) | dark | `#101413` | brand `CheckerDark` | ✅ brand fallback |
| OnePlus CPH2691 (API 36) | light | `#FAF9FF` | *not* the brand colour | ✅ dynamic colour active |
| OnePlus CPH2691 (API 36) | dark | — | — | ✅ dynamic dark, light glyphs |

The dynamic/fallback split is proven by **sampled pixel values**, not assumption: API 26 renders exactly
the hand-written brand palette (dynamic colour does not exist there), while the phone renders a
wallpaper-derived surface. Status-bar and navigation-bar insets are respected on all four — the T0.1
symptom (text painted under the clock) is gone. Three-button nav on API 26 and gesture nav on API 36
both lay out correctly.

`aapt2 dump xmltree` on the release APK confirms `android:enableOnBackInvokedCallback=true`,
`allowBackup=false`, and that both `dataExtractionRules` and `fullBackupContent` are wired.

Build: `./gradlew clean assembleDebug assembleRelease test lint` → **BUILD SUCCESSFUL**,
**0 lint errors, 2 warnings** (both deliberate, see below). Release APK 801 KB with Compose
(65 KB before) — that is the honest cost of Compose, and R8 already handles it.

**Choices made:**
- `MainActivity` is now a `ComponentActivity`, **not** AppCompat. The UI is 100 % Compose, so there
  are no platform widgets to theme; AppCompat would add a dependency and theme-mapping work for
  nothing.
- `StickerportTheme(darkTheme, dynamicColor)` takes both flags explicitly so previews and tests can
  render either scheme without touching device settings.
- Added `LocalCheckerColors` and `LocalUsesDynamicColor` composition locals now, while there is only
  one screen. spec §12.7 requires a checkerboard behind transparent art, and that only works if the
  checker tones are *independent of* the dynamic surface colour — which cannot be retrofitted
  cheaply once ten screens depend on the theme.
- `ui/components/AppShell.kt` holds the `Scaffold`; T0.3 puts a `NavHost` inside it.
- Bumped the 11 outdated catalog versions lint flagged (`OldTargetApi` was left alone).

**Mistakes made during the task:**
- Put `android:enforceNavigationBarContrast` / `enforceStatusBarContrast` in `values/themes.xml`.
  Both need API 29 and `minSdk` is 26, so **lint failed the build**. They were also redundant:
  `enableEdgeToEdge()` already owns the system-bar scrim and icon policy. Removed rather than moved
  into `values-v29/`.
- In T0.1 I wrote a `data_extraction_rules.xml` that excluded `domain="root"` while leaving
  `allowBackup="true"` — contradictory and over-broad. Lint correctly demanded
  `dataExtractionRules` in T0.2, and its follow-up warning correctly demanded `fullBackupContent`
  too (that one only governs pre-Android-12). Both now exist, agree, and the reasoning is written
  down.
- Left `mipmap-anydpi-v26` in place. A useless qualifier at `minSdk 26`; renamed to `mipmap-anydpi`.
- Chased Context7 for the Compose theming and `enableEdgeToEdge` APIs twice. Both times it returned
  the **Views/Fragments** equivalents (`DynamicColors`, `OnBackStackChangedListener`) instead of the
  Compose KTX ones. The real answers came from the official insets guide and from letting the
  compiler check the code. Recorded so future tasks don't burn time on it.
- Chased a rotated screenshot as if it were a layout bug. `screencap` returned a landscape buffer
  because the phone's **auto-rotate was on** and its sensor reported sideways. Fixed with
  `settings put system accelerometer_rotation 0` + `user_rotation 0`, then restored the setting.
- Bumped **JUnit 5.14.1 → 6.1.3**, a major version, with no tests in the project to validate it.
  Justified because T0.5 is the immediate consumer, but it is now a logged risk.

**Things to remember for future tasks:**
- 📌 Screens are stateless composables over `UiState` + event lambdas, with previews for every state (§12).
- 📌 Package layout is fixed in §7.2; keep to it.
- ➕ **Edge-to-edge is *enforced* on Android 15+ when `targetSdk >= 35`.** Anything that does not
  consume insets is visibly clipped on modern devices. Put content inside `AppShell`, never directly
  under `setContent`.
- ➕ **The Compose compiler plugin is required even with AGP 9 built-in Kotlin** —
  `alias(libs.plugins.kotlin.compose)` plus `buildFeatures { compose = true }`. Built-in Kotlin
  compiles the code; the plugin supplies the Compose compiler.
- ➕ **Compose artifacts must not carry versions.** `platform(libs.compose.bom)` then bare
  `androidx.compose.*` aliases. `compose-ui-tooling` is `debugImplementation` only.
- ➕ **`Scaffold` already consumes `WindowInsets.systemBars`** for its top/bottom bars and hands the
  remainder to `content` as `PaddingValues`. Do not also add `.systemBarsPadding()` or content gets
  double-inset.
- ➕ **The two accepted lint warnings — do not "fix" them blindly:**
  - `OldTargetApi` (targetSdk 36 vs compileSdk 37) — deliberate, Play's minimum. Revisit at T14.2.
  - `UnusedAttribute` on `enableOnBackInvokedCallback` — expected, the attribute is API 33+ and
    `minSdk` is 26. Harmless.
- ➕ **Both backup mechanisms are required** across the `minSdk` 26 range: `dataExtractionRules`
  (API 31+) *and* `fullBackupContent` (below 31). `allowBackup="false"` alone is not enough. T1.3
  adds the explicit token `<exclude>`.
- ➕ **The `values-night/colors.xml` + `window_background` pairing** is what stops a white flash before
  Compose's first frame. If the brand background changes, update `values/colors.xml` and `Color.kt`
  together or the flash comes back.
- ➕ **Jumper:** JUnit is now **6.1.3** (was 5.14.1). T0.5 must confirm `useJUnitPlatform()` and the
  JUnit Platform launcher version line up on Gradle 9.8.

### [ ] T0.3 — Navigation skeleton
**Refs:** §6, §7.2, §12 · **Context7:** Navigation Compose (type-safe routes), kotlinx.serialization · **Depends on:** T0.2

**Goal of the task:** Type-safe routes and placeholder screens for Home, Preview, Progress, Result, Library, Settings, Onboarding.

**Steps**
- [ ] `@Serializable` route objects/classes (Preview takes a set name, Result takes an import id)
- [ ] `NavHost` with placeholder composables in `ui/screens/<name>/`
- [ ] Home → Settings and Home → Library working

**How to test:** Manual navigation through every route; instrumented test that each route composes without crashing.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Package layout is fixed in §7.2; keep to it.
- ➕ _Add lessons after finishing._

### [ ] T0.4 — DI, Room, and DataStore skeleton
**Refs:** §6, §11 · **Context7:** Hilt, Room, DataStore · **Depends on:** T0.1

**Goal of the task:** Hilt wired up; Room database with the three entities from §11 (empty DAOs OK); DataStore instance for settings.

**Steps**
- [ ] Hilt application class, modules for DB and DataStore
- [ ] `ImportEntity`, `StickerEntity`, `PackEntity` with enums (STATIC|TGS|WEBM, statuses)
- [ ] Room schema export on; first migration strategy noted
- [ ] `SettingsRepository` stub over DataStore

**How to test:** Room in-memory instrumented test inserts and reads one row per entity; app launches with Hilt without crashing.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 `ADDED` is a cache of the whitelist check, not a source of truth (§11).
- 📌 Deleting an Import must cascade to Stickers and Packs (§11).
- ➕ _Add lessons after finishing._

### [ ] T0.5 — CI and quality gates
**Refs:** §16, §17 (M0) · **Context7:** JUnit5, MockK, Turbine, Kotest · **Depends on:** T0.1

**Goal of the task:** CI builds and runs unit tests on every push; test libraries wired up.

**Steps**
- [ ] GitHub Actions workflow: build, unit tests, lint
- [ ] Add JUnit5/Kotest, MockK, Turbine, Robolectric to the version catalog
- [ ] One sample unit test per library proves the setup

**How to test:** Push a branch; CI is green. Break a test on purpose; CI goes red.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

🧪 **Sprint 0 gate:** CI green, APK launches. → tick Sprint 0 in the dashboard.

---

# Sprint 1 — Input and Telegram client (M1a)
**Sprint goal:** Given a link and a valid token, the app can fetch a sticker set and download its files. **Demo:** a debug screen prints the set title and downloads all originals.

### [ ] T1.1 — Link parser
**Refs:** §8.1, FR-1, FR-2, FR-3 · **Context7:** — (pure Kotlin) · **Depends on:** T0.5

**Goal of the task:** A pure `ParseLink` use case that extracts a set name from every accepted link form and flags `addemoji` links.

**Steps**
- [ ] Sealed result: `StickerSet(name)` | `CustomEmoji` | `Invalid`
- [ ] Support `t.me/addstickers/NAME`, `telegram.me/...`, `tg://addstickers?set=NAME`, bare `NAME`, links inside longer text (with or without scheme; query/fragment ignored)
- [ ] Names are case-sensitive `[A-Za-z0-9_]+`
- [ ] ≥ 20 unit tests including Telegram's share text ("Check out this sticker pack: …")

**How to test:** `./gradlew test --tests "*ParseLinkTest"`; all ≥ 20 cases pass.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Do not lowercase set names; they are case-sensitive.
- ➕ _Add lessons after finishing._

### [ ] T1.2 — Telegram DTOs and API client
**Refs:** §8.2, §7.3 · **Context7:** OkHttp, kotlinx.serialization · **Depends on:** T0.4

**Goal of the task:** Typed client for `getMe`, `getStickerSet`, `getFile` plus the file download URL, with correct timeouts.

**Steps**
- [ ] DTOs: `StickerSet`, `Sticker` (`file_id`, `file_unique_id`, `type`, `is_animated`, `is_video`, `emoji`, `thumbnail`, `file_size`) with `ignoreUnknownKeys`
- [ ] Format mapping: static / animated (TGS) / video (WEBM); `sticker_type` must be `regular`
- [ ] OkHttp: connect 10 s, read 30 s
- [ ] `StickerSource` interface (`fetchSet`, `downloadFile`) with a `TelegramSource` skeleton
- [ ] Tests with `MockWebServer` and saved JSON fixtures

**How to test:** MockWebServer tests for success, `ok:false`, 401, 404 (set not found), 429 with `retry_after`, malformed JSON.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 The token lives in the URL path (`/bot<TOKEN>/…`, `/file/bot<TOKEN>/…`); nothing may log full URLs (§8.3).
- 📌 Only network destination allowed: `api.telegram.org` (NFR-5).
- ➕ _Add lessons after finishing._

### [ ] T1.3 — Token store and log redaction
**Refs:** §8.3, §15, FR-23 · **Context7:** Tink (Android Keystore AEAD), DataStore · **Depends on:** T1.2

**Goal of the task:** `TokenStore` that encrypts the token with a Keystore-backed AES-GCM key, keeps it out of backups, and a redacting logging interceptor.

**Steps**
- [ ] Check the current status of `androidx.security:security-crypto` (plan says deprecated) and choose Tink or direct Keystore
- [ ] `TokenStore` (`get`/`set`/`clear`) persisting ciphertext in DataStore
- [ ] Exclude from backup: `dataExtractionRules` + `fullBackupContent`
- [ ] OkHttp logging interceptor redacting `/bot<TOKEN>/` and `/file/bot<TOKEN>/`
- [ ] Network security config: `cleartextTrafficPermitted=false`

**How to test:** Instrumented round-trip test; unit test that redaction removes the token from both URL patterns; inspect DataStore file and confirm no plaintext token; `adb shell dumpsys` / backup check shows it excluded.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 `TelegramSource` reads from `TokenStore` on every call so replacing the token takes effect immediately (§8.3).
- 📌 The token is never logged, including in crash breadcrumbs.
- ➕ _Add lessons after finishing._

### [ ] T1.4 — Basic token entry in Settings
**Refs:** §8.3 (steps 2–4), FR-19, FR-20 (basic version) · **Context7:** Compose, Hilt ViewModel · **Depends on:** T1.3, T0.3

**Goal of the task:** A simple Settings field where the user pastes a token; **Verify** calls `getMe` and saves only on success. (The guided stepper comes in T5.1.)

**Steps**
- [ ] Masked field with reveal toggle and a **Paste** button (reads the clipboard only when tapped)
- [ ] Shape check `^\d{6,12}:[A-Za-z0-9_-]{30,}$` before any network call
- [ ] Verify: `ok:true` → save; `401` → "Telegram rejected this token"; network error → retry message
- [ ] Show saved token masked (bot ID + `••••`)

**How to test:** Unit tests for the shape check; manual test with a real BotFather token (valid), a mangled one, and airplane mode.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Save the token only after a successful `getMe`.
- ➕ _Add lessons after finishing._

### [ ] T1.5 — TelegramSource: downloads, concurrency, retry, cache
**Refs:** §8.4, FR-5 · **Context7:** OkHttp, kotlinx.coroutines (Semaphore) · **Depends on:** T1.2, T1.3

**Goal of the task:** Reliable bounded-concurrency downloads with retry/backoff and an on-disk cache.

**Steps**
- [ ] Semaphore ≤ 4 parallel downloads
- [ ] `429` → wait `parameters.retry_after`; 5xx/IO → exponential backoff, max 4 tries
- [ ] Cache to `cacheDir/tg/<set>/<file_unique_id>`; skip if present
- [ ] Download thumbnails separately for the preview grid
- [ ] Temp file + atomic rename so partial downloads are never treated as complete

**How to test:** MockWebServer tests for retry, 429 wait, and cache hit (second run makes zero requests); manual: download a real 40+ sticker pack.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Bot file downloads are limited to 20 MB (plenty).
- ➕ _Add lessons after finishing._

🧪 **Sprint 1 gate:** Real set fetched and fully downloaded with a real token; no token visible in logcat.

---

# Sprint 2 — Native WebP and static conversion (M1b)
**Sprint goal:** Any Telegram static sticker becomes a validated, WhatsApp-compliant WebP. **Demo:** a debug action converts a downloaded set and prints size and validation result per sticker.

### [ ] T2.1 — Verify WhatsApp constraints (Appendix A) and create constants
**Refs:** Appendix A, §9.9, §10.2 · **Context7:** try `WhatsApp/stickers` (likely not indexed; fall back to the repo README and the Android sample) · **Depends on:** T0.5

**Goal of the task:** Every number in Appendix A is checked against the official `WhatsApp/stickers` repo and stored in one `WhatsAppLimits` constants object with a source note.

**Steps**
- [ ] Read the repo README and Android sample; confirm sticker size limits, tray icon rules, min frame duration, max duration, emoji count, name/publisher length, identifier charset, transparency guidance
- [ ] Create `WhatsAppLimits` (single source of truth) with a comment per value
- [ ] Update Appendix A in `plan.md` (or note discrepancies in this task)
- [ ] Copy provider constants (column names, URI shapes) from the sample instead of retyping them

**How to test:** Review: every constant has a source link/line. Unit test asserts the constants object matches the values you verified.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Only "3–30 stickers per pack" and the `animated_sticker_pack` flag are marked verified in plan.md; everything else is "as recalled".
- ➕ _Add lessons after finishing._

### [ ] T2.2 — `:native-webp` module (libwebp via CMake)
**Refs:** §9.5, §7.1, §19 (16 KB) · **Context7:** Android NDK / CMake, libwebp · **Depends on:** T0.1

**Goal of the task:** Pinned libwebp builds for all ABIs inside `:native-webp` without slowing normal builds.

**Steps**
- [ ] Vendor libwebp at a pinned release tag (record the tag in this file)
- [ ] CMake build; ABIs `arm64-v8a`, `armeabi-v7a`, `x86_64`
- [ ] Enable 16 KB page-size compatibility in the linker/NDK config
- [ ] Load the library from Kotlin (`WebPNative` object, methods stubbed)

**How to test:** `assembleDebug` builds; `System.loadLibrary` succeeds on arm64 device and x86_64 emulator; check ELF alignment with the NDK/`zipalign -P 16` check.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 BSD-3 license → add to the OSS licenses list later (§19).
- 📌 Keep native build isolated so NDK build times don't affect normal builds.
- ➕ _Add lessons after finishing._

### [ ] T2.3 — JNI `encodeStatic` (premultiplied alpha)
**Refs:** §9.5, §9.2 step 4 · **Context7:** libwebp encoder API (`WebPConfig`, `WebPPicture`) · **Depends on:** T2.2

**Goal of the task:** `encodeStatic(bmp, quality, method, alphaQuality, targetSizeBytes)` returns lossy+alpha WebP bytes with no dark fringes.

**Steps**
- [ ] Lock pixels via `jnigraphics`
- [ ] **Un-premultiply** alpha before `WebPPictureImportRGBA`
- [ ] Set `method` 4–6, `alpha_quality` ≥ 80, `target_size`
- [ ] Always free encoder/picture in `finally` equivalents (no native leaks)

**How to test:** Instrumented test: encode a bitmap with soft alpha edges, decode with `BitmapFactory`, compare edge pixels (no darkening); leak check by encoding 500 times without memory growth.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Android bitmaps are premultiplied; libwebp expects straight RGBA. Skipping un-premultiply gives dark fringes on soft edges.
- ➕ _Add lessons after finishing._

### [ ] T2.4 — StaticConverter with pass-through
**Refs:** §9.2, FR-6, FR-9 · **Context7:** — · **Depends on:** T2.3, T2.1

**Goal of the task:** Convert a static Telegram WebP to 512×512 WebP, or pass it through untouched when already compliant.

**Steps**
- [ ] Decode with `BitmapFactory` (WebP alpha OK on API 26+)
- [ ] If exactly 512×512, WebP, within size limit → copy unchanged
- [ ] Else scale-to-fit (aspect preserved) and center on transparent 512×512 ARGB_8888 canvas, then encode
- [ ] Carry the sticker's emoji through (FR-9)

**How to test:** Fixture tests: already-compliant file is byte-identical after conversion; 512×300 sticker becomes 512×512 centered with transparency; output decodes fine.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Telegram statics are 512 on one side and ≤ 512 on the other.
- ➕ _Add lessons after finishing._

### [ ] T2.5 — BudgetFitter (static ladder)
**Refs:** §9.6, FR-7 · **Context7:** — · **Depends on:** T2.4

**Goal of the task:** A pure function that walks a quality ladder with an injected encoder until the result fits the size limit, or fails with the best attempt size.

**Steps**
- [ ] Static ladder `[90,80,70,60,50,40,30]` → stop at first result within limit
- [ ] Presets: Balanced starts at the top; "Smaller files" starts lower
- [ ] Failure result `FAILED_OVER_BUDGET` carrying the best attempt size
- [ ] Never reduce canvas below 512×512

**How to test:** Unit tests with a fake encoder (returns sizes by quality): first-hit stop, exhaustion → failure, preset start index.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 The animated ladder (quality, fps) is added in T9.3 using the same structure.
- ➕ _Add lessons after finishing._

### [ ] T2.6 — Tray icon and PackValidator
**Refs:** §9.8, §9.9, G4, FR-8, FR-12 · **Context7:** — · **Depends on:** T2.4, T2.1

**Goal of the task:** Generate the 96×96 tray icon and validate every output file and pack, returning typed errors.

**Steps**
- [ ] Tray: first sticker → 96×96 PNG on transparent canvas, within the tray size limit
- [ ] Validator: dimensions, size, format, emoji count, pack size 3–30, static/animated not mixed, name/publisher length
- [ ] Typed error list (not exceptions) usable by the UI

**How to test:** Fixtures: valid, oversize, wrong dimensions, mixed types, too few (2), too many (31); assert the exact error types.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 The ContentProvider refuses to list a pack that fails validation; validate before marking READY.
- ➕ _Add lessons after finishing._

🧪 **Sprint 2 gate:** A real static set converts, all outputs validate, no dark fringes on soft-edged stickers.

---

# Sprint 3 — Packaging and WhatsApp handoff (M1c)
**Sprint goal:** Converted stickers are chunked into packs, exposed via the ContentProvider, and WhatsApp can be launched to add them. **Demo:** a hard-coded pack is added to real WhatsApp.

### [ ] T3.1 — Room DAOs and PackFileStore
**Refs:** §11, §9.1, FR-18 · **Context7:** Room · **Depends on:** T0.4

**Goal of the task:** Full DAOs and a file store that writes packs into a temp dir and atomically renames to `filesDir/packs/<packId>/`.

**Steps**
- [ ] DAOs (insert/update status, query non-converted stickers, packs by status, cascade delete)
- [ ] `PackFileStore`: temp dir → atomic rename; `tray.png`, `01.webp`, `02.webp`…
- [ ] Delete removes DB rows **and** directories

**How to test:** Instrumented tests: cascade delete leaves no rows/dirs; a crash mid-write (simulated) leaves the previous READY snapshot intact.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 The provider must serve the last READY snapshot while a pack is being rewritten (§14).
- ➕ _Add lessons after finishing._

### [ ] T3.2 — Chunking and pack naming
**Refs:** §9.7, FR-10, FR-11, FR-12 · **Context7:** Kotest property testing (optional) · **Depends on:** T3.1

**Goal of the task:** Partition by static/animated, chunk each partition into balanced packs of 3–30, keep Telegram order, name packs.

**Steps**
- [ ] `chunkSizes(n, max=30, min=3)` as in plan.md
- [ ] Partition first, then chunk; never mix static and animated
- [ ] Names: `"<title> (1/3)"`, no suffix if only one pack; publisher from settings
- [ ] Block with an explanation when < 3 valid stickers remain

**How to test:** Property test: sizes sum to n, each 3–30 for n ≥ 3, max difference ≤ 1. Examples: 31 → [16,15]; 61 → [21,20,20]; 30 → [30]; 3 → [3]; 2 → error.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Set with exactly 3 and exactly 31 stickers are required manual QA cases (§16).
- ➕ _Add lessons after finishing._

### [ ] T3.3 — ConvertStickers orchestrator (no worker yet)
**Refs:** §9.1, §13 (idempotency), FR-6, FR-7 · **Context7:** kotlinx.coroutines · **Depends on:** T1.5, T2.5, T2.6, T3.2

**Goal of the task:** A use case that downloads → converts → validates → stores → chunks, writing per-sticker status to Room, written idempotently so the worker (T6.1) can wrap it.

**Steps**
- [ ] Iterate only stickers whose `convStatus` != CONVERTED
- [ ] Concurrency: downloads ≤ 4, conversions ≤ 2
- [ ] Persist status/`outputFile`/`outputBytes`/`failReason` per sticker
- [ ] After conversion, create `PackEntity` rows, bump `imageDataVersion`, mark READY only if the validator passes

**How to test:** Integration test with fake `StickerSource`: run to completion; run again and assert no re-download/re-convert; inject a failing sticker and assert `FAILED` with reason and the rest READY.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Progress lives in Room; UI observes DB Flows, no direct worker→UI channel (§13).
- ➕ _Add lessons after finishing._

### [ ] T3.4 — StickerContentProvider and manifest
**Refs:** §10.1, §10.2, FR-14, NFR-4, §15 · **Context7:** Android ContentProvider docs · **Depends on:** T3.1, T2.1

**Goal of the task:** Provider exposing `metadata`, `metadata/<id>`, `stickers/<id>`, and `stickers_asset/<id>/<file>` exactly as the official sample does.

**Steps**
- [ ] Manifest: `<queries>` for `com.whatsapp` and `com.whatsapp.w4b`; provider `exported=true` with `readPermission="com.whatsapp.sticker.READ"`
- [ ] Copy constants/column names from the official sample
- [ ] Only READY packs listed; synchronous DAO/file reads (no main-thread hops)
- [ ] Set `publisher_website` = source Telegram link; `image_data_version` from the pack
- [ ] Asset URIs return `AssetFileDescriptor` for stickers and tray

**How to test:** See T3.5 (contract test). Manually query with `adb shell content query --uri content://<authority>/metadata`.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Bump `image_data_version` whenever a pack's files change, or WhatsApp serves stale cache.
- 📌 Expose only sticker/pack data; nothing else in the provider.
- ➕ _Add lessons after finishing._

### [ ] T3.5 — Provider contract test
**Refs:** §16 (provider contract) · **Context7:** AndroidX Test · **Depends on:** T3.4

**Goal of the task:** Automated proof that the provider matches the contract WhatsApp expects.

**Steps**
- [ ] Insert a pack; query all four URI shapes through `ContentResolver`
- [ ] Assert columns, row counts, emoji format (comma-separated, 1–3)
- [ ] Open each asset file descriptor; assert byte length matches the DB
- [ ] Assert a pack failing validation is not listed; response time < 200 ms

**How to test:** `./gradlew connectedDebugAndroidTest --tests "*ProviderContractTest"` green; wire into CI if an emulator job is feasible.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T3.6 — WhatsAppLauncher and WhitelistChecker
**Refs:** §10.3, §10.4, FR-15, FR-16, FR-17, `PackExporter` · **Context7:** Activity Result API · **Depends on:** T3.4

**Goal of the task:** Launch the add-pack intent for consumer or Business WhatsApp, read `validation_error` on failure, and detect already-added packs.

**Steps**
- [ ] `PackExporter` implementation: `isAvailable`, `isAdded`, `buildAddIntent`
- [ ] Intent extras exactly as in plan.md §10.3; `setPackage(target)`
- [ ] `StartActivityForResult`; on `RESULT_CANCELED` read `validation_error`, show a Details dialog, log it, **never silently retry**
- [ ] Whitelist check (copy authorities from the sample's `WhitelistCheck`); refresh on `onResume` and after intent returns
- [ ] Both installed → let the user choose and remember it; none installed → disabled state

**How to test:** Manual on a device with WhatsApp: add a hand-made pack; cancel it; force a validation error (e.g. corrupt file) and confirm the details dialog. Unit-test intent extras.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Deleting a pack in Stickerport doesn't remove it from WhatsApp (§10.6).
- ➕ _Add lessons after finishing._

🧪 **Sprint 3 gate:** A converted pack appears in real WhatsApp's sticker tray with correct emojis and tray icon.

---

# Sprint 4 — Minimal UI and M1 acceptance (M1d)
**Sprint goal:** Paste link → preview → convert → result → add to WhatsApp, working end-to-end for static packs. **Demo:** the M1 acceptance run.

### [ ] T4.1 — Home screen (paste flow)
**Refs:** §12.1, Flow B · **Context7:** Compose, Hilt ViewModel · **Depends on:** T1.1, T1.4

**Goal of the task:** Home with paste field + Go, live link validation, and a Recent list.

**Steps**
- [ ] Text field + Paste button + Go, validated by `ParseLink` while typing
- [ ] Disabled state with "Set up your bot token" banner when no token
- [ ] Recent (last 5 imports with status chips) and empty state text

**How to test:** Compose UI tests for: empty, invalid link, valid link, no-token states.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T4.2 — Import Preview (basic)
**Refs:** §12.2, FR-4 · **Context7:** Compose (LazyVerticalGrid), Coil 3 · **Depends on:** T4.1, T1.5

**Goal of the task:** Fetch the set and show title (editable), format badge, count, and a thumbnail grid; **Convert** starts the pipeline.

**Steps**
- [ ] Loading skeleton, loaded, and error (retry + "Open link in Telegram") states
- [ ] Adaptive 88 dp grid on a checkerboard background
- [ ] Persist `ImportEntity` + `StickerEntity` rows on Convert
- [ ] Show unsupported type / WEBM-not-yet-supported warnings

**How to test:** Compose state tests for loading/loaded/error; manual test with a real set.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Thumbnails (small static WebP) are used for the grid; don't decode TGS/WEBM for previews.
- 📌 Deselection and the live summary line come in T7.1.
- ➕ _Add lessons after finishing._

### [ ] T4.3 — Progress and Result screens
**Refs:** §12.3, §12.4, §10.5 · **Context7:** Compose · **Depends on:** T4.2, T3.3, T3.6

**Goal of the task:** Show conversion progress from Room, then a Result screen with one card and **Add to WhatsApp** button per pack.

**Steps**
- [ ] Progress: overall bar, `12 / 48` counter, stage text, per-sticker status overlay
- [ ] Result: tray icon, name, count, total size, button states (Add / Added ✓ / WhatsApp not installed)
- [ ] Target picker when both WhatsApp variants are installed
- [ ] Do **not** chain multiple add intents automatically

**How to test:** Compose state tests; manual run with a 40-sticker set producing 2 packs, each added separately.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Each pack needs its own user confirmation inside WhatsApp (§10.5).
- ➕ _Add lessons after finishing._

### [ ] T4.4 — Library
**Refs:** §12.5, FR-18, Flow C · **Context7:** Compose, Room Flow · **Depends on:** T3.1, T3.6

**Goal of the task:** List past imports/packs, delete (files + rows) with undo, and re-add to WhatsApp.

**Steps**
- [ ] List with status chips and search by name
- [ ] Swipe-to-delete with undo snackbar (delete files only after undo window)
- [ ] Pack detail → Add to WhatsApp

**How to test:** Instrumented test: delete then undo restores; delete without undo removes directories; manual re-add after removing the pack from WhatsApp.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T4.5 — 🧪 M1 acceptance on real devices
**Refs:** §17 (M1), Appendix C · **Context7:** — · **Depends on:** T4.1–T4.4

**Goal of the task:** Prove the M1 acceptance: a static pack (including one > 30 stickers) lands in real WhatsApp on 2 devices.

**Steps**
- [ ] Device A + Device B with WhatsApp; at least one with a lower Android version
- [ ] Test sets: exactly 3 stickers, 31 stickers, a 100+ set
- [ ] Record results (device, Android version, WhatsApp version, outcome) in the task's notes
- [ ] File issues for every failure; fix blockers before Sprint 5

**How to test:** All test sets appear in WhatsApp with correct emojis, tray icon, and names.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

🧪 **Sprint 4 gate (M1):** Static end-to-end works on 2 devices.

---

# Sprint 5 — Token onboarding and entry points (M2a)
**Sprint goal:** A new user can set up a token with in-app guidance and import via Telegram's Share sheet. **Demo:** share a pack from Telegram before setup, finish setup, and the import resumes.

### [ ] T5.1 — Guided token setup stepper
**Refs:** §12.8, §8.3, FR-20, FR-21 · **Context7:** Compose, Android `FLAG_SECURE` · **Depends on:** T1.4

**Goal of the task:** Replace the basic entry with the 3-step guide: Open @BotFather → `/newbot` and copy token → Paste and verify.

**Steps**
- [ ] Stepper UI with button to `https://t.me/BotFather` (browser fallback)
- [ ] Copyable `/newbot` command; example token shape `123456789:ABC…`
- [ ] Masked input, reveal toggle, Paste, inline validation, Verify with loading state
- [ ] Reassurance line: token stays on device and is only sent to Telegram
- [ ] `FLAG_SECURE` on this screen
- [ ] Success → return to the pending import, or Home

**How to test:** Compose tests per step and per error; manual first-run walkthrough on a clean install.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 The bot is only an API key; it doesn't need to be added to any chat (§8.3).
- ➕ _Add lessons after finishing._

### [ ] T5.2 — Share and deep-link receiver
**Refs:** §10.1 (`ShareReceiverActivity`), FR-1, FR-21, Flow A · **Context7:** Android intent filters · **Depends on:** T5.1, T4.2

**Goal of the task:** `ACTION_SEND` (text/plain) and `ACTION_VIEW` for `t.me`/`telegram.me` `addstickers` links open the Preview; links received before setup are held and resumed.

**Steps**
- [ ] `ShareReceiverActivity` with the two intent filters from plan.md §10.1
- [ ] Parse with `ParseLink`; route to Preview, custom emoji message, or an error
- [ ] Persist a "pending link" across setup and process death
- [ ] Handle `onNewIntent` when the app is already open

**How to test:** `adb shell am start -a android.intent.action.SEND -t text/plain --es android.intent.extra.TEXT "..."` and `-a android.intent.action.VIEW -d https://t.me/addstickers/NAME`; manual share from real Telegram.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 `t.me` links can't be verified App Links, so Stickerport appears in the chooser rather than opening automatically. That's expected.
- ➕ _Add lessons after finishing._

### [ ] T5.3 — 401 handling and token management
**Refs:** FR-19, FR-22, FR-23, §8.3 · **Context7:** Compose · **Depends on:** T5.1

**Goal of the task:** Settings shows the masked token with Test / Replace / Remove; any `401` prompts a replacement while keeping queued work.

**Steps**
- [ ] Settings token row: bot ID + `••••`, Test (calls `getMe`), Replace, Remove
- [ ] Remove deletes the encrypted value
- [ ] Central 401 handler → prompt → resume queued imports after a new token verifies

**How to test:** Revoke the token in BotFather mid-import; app prompts and, after replacement, continues without re-downloading finished stickers.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T5.4 — Unsupported set types
**Refs:** FR-3, §12.2 warnings, §14 · **Context7:** — · **Depends on:** T5.2, T1.2

**Goal of the task:** Clear messages for custom emoji sets (`addemoji`), mask sets, and unknown formats.

**Steps**
- [ ] `addemoji` link → "Custom emoji packs aren't supported"
- [ ] `sticker_type` ≠ `regular` → unsupported message
- [ ] Unknown format → "unsupported format"; log raw `is_animated/is_video` flags in the debug log only

**How to test:** Unit tests with fixtures for each type; UI test for each message.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T5.5 — 🔬 Spike S1: token onboarding friction
**Refs:** §18 (S1) · **Context7:** — · **Depends on:** T5.1

**Goal of the task:** Learn whether non-technical users can finish token setup unaided.

**Steps**
- [ ] Recruit 3–5 people who have never used BotFather
- [ ] Observe without helping; time each run
- [ ] Record where they stalled and what wording confused them
- [ ] Decide fixes (copy, layout) and file them for T11.3

**How to test (pass criteria):** ≥ 80 % finish unaided in < 3 min. If not met, list concrete UX changes before Sprint 11.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

🧪 **Sprint 5 gate:** Share-before-setup → setup → import resumes automatically.

---

# Sprint 6 — Background work (M2b)
**Sprint goal:** Conversion survives leaving the app and process death. **Demo:** kill the app mid-conversion and watch it resume.

### [ ] T6.1 — ConversionWorker
**Refs:** §13, NFR-7 · **Context7:** WorkManager (`CoroutineWorker`, expedited work, `setForeground`) · **Depends on:** T3.3

**Goal of the task:** One unique idempotent worker per import wrapping the orchestrator.

**Steps**
- [ ] `enqueueUniqueWork(importId, KEEP)`
- [ ] Worker queries non-CONVERTED stickers and continues
- [ ] Concurrency: downloads ≤ 4, conversions ≤ 2, 1 for animated on `isLowRamDevice`
- [ ] Progress written to Room per sticker

**How to test:** Instrumented WorkManager test (`TestListenableWorkerBuilder`): stop mid-run, restart, assert finished stickers aren't reprocessed.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 The UI observes DB Flows; no direct worker→UI channel.
- ➕ _Add lessons after finishing._

### [ ] T6.2 — Foreground service, notification, permissions
**Refs:** §13, NFR-1 · **Context7:** WorkManager foreground, Android 14+ foreground service types, notifications · **Depends on:** T6.1

**Goal of the task:** Expedited + foreground execution with an ongoing progress notification; correct behavior on Android 13–16.

**Steps**
- [ ] Declare `FOREGROUND_SERVICE` + the `dataSync` type; check current Android 14–16 rules
- [ ] Notification channel and progress notification with a Cancel action
- [ ] Request `POST_NOTIFICATIONS` on Android 13+; work still runs if denied
- [ ] Handle `ForegroundServiceStartNotAllowedException` gracefully

**How to test:** Manual on Android 13, 14, and 15/16 with the app backgrounded; deny the permission and confirm conversion still completes.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Spike S6 (T6.4) validates this on Android 14–16.
- ➕ _Add lessons after finishing._

### [ ] T6.3 — Progress UI from DB, cancel, partial results
**Refs:** §12.3 · **Context7:** Compose, Room Flow · **Depends on:** T6.1, T4.3

**Goal of the task:** Progress screen driven by Room; **Cancel** keeps converted stickers and marks the import partial.

**Steps**
- [ ] Observe Flow from Room only
- [ ] Cancel → worker stops, import marked partial, converted files kept
- [ ] Leaving the screen keeps work running

**How to test:** UI test: cancel mid-run, reopen, finished stickers still shown as converted.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T6.4 — Resume test and 🔬 Spike S6
**Refs:** §16 (worker resume), §18 (S6), M2 acceptance · **Context7:** — · **Depends on:** T6.2, T6.3

**Goal of the task:** Prove resume-after-kill and long backgrounded conversions.

**Steps**
- [ ] Automated: kill mid-run, verify no re-download of finished items
- [ ] Manual: long conversion (100+ stickers) with the app backgrounded on Android 14, 15, 16
- [ ] Record notification/service behavior differences

**How to test (pass criteria S6):** Completes; no `ForegroundServiceStartNotAllowedException`; killing the app mid-conversion resumes correctly.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

🧪 **Sprint 6 gate:** Killing the app mid-conversion resumes correctly.

---

# Sprint 7 — Preview polish, errors, v1 Definition of Done (M2c)
**Sprint goal:** v1 release candidate meeting plan.md Appendix C. **Demo:** the full QA matrix on ≥ 2 devices.

### [ ] T7.1 — Preview deselection and live summary
**Refs:** §12.2, FR-13, §9.7 · **Context7:** Compose · **Depends on:** T4.2, T3.2

**Goal of the task:** Tap to toggle stickers; live summary like "48 stickers → 2 WhatsApp packs (24 + 24)".

**Steps**
- [ ] Tap toggles inclusion (dimmed = excluded); persist `selected`
- [ ] Recompute summary with `chunkSizes` on every change
- [ ] < 3 selected → Convert disabled with a banner

**How to test:** Unit test for the summary function; UI test toggling below 3.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T7.2 — Partial-failure UX
**Refs:** §12.3, §14, FR-7 · **Context7:** Compose · **Depends on:** T6.3

**Goal of the task:** When some stickers fail, show why and let the user continue without them if ≥ 3 remain.

**Steps**
- [ ] Banner "3 stickers couldn't be converted" with expandable reasons
- [ ] "Continue without them" re-chunks remaining stickers
- [ ] Over-budget failures show the size that was achieved

**How to test:** Inject failures (fake converter); assert the banner and re-chunk results including the < 3 remaining block.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T7.3 — Edge-case handling pass
**Refs:** §14 · **Context7:** — · **Depends on:** T6.4

**Goal of the task:** Implement every row of the §14 table not already covered.

**Steps**
- [ ] Disk-space precheck (≈ 0.1 MB static, 0.6 MB animated per sticker) with clear message
- [ ] `429` message "Telegram is rate-limiting, waiting Ns"
- [ ] Set not found / deleted message with Retry and Open in Telegram
- [ ] "Update WhatsApp to add animated packs" hint on animated failures
- [ ] WhatsApp not installed → disabled button + Play Store link
- [ ] Provider serves last READY snapshot during rewrites

**How to test:** Table-driven tests mapping each situation to its expected message/behavior; manual for disk-full and airplane mode.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T7.4 — Settings: publisher, quality preset, target
**Refs:** §12.6, FR-11, FR-19 · **Context7:** DataStore, Compose · **Depends on:** T1.4

**Goal of the task:** Working publisher name (default "Stickerport"), quality preset (Balanced / Smaller files), and WhatsApp target preference.

**Steps**
- [ ] DataStore-backed settings with validation (name length limit from `WhatsAppLimits`)
- [ ] Preset feeds `BudgetFitter` start index
- [ ] Remembered WhatsApp target used by Result screen

**How to test:** Unit tests for the repository; changing preset changes output size on a fixture.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T7.5 — 🧪 v1 Definition of Done audit
**Refs:** Appendix C, §16 (manual QA matrix), NFR-5 · **Context7:** — · **Depends on:** T7.1–T7.4

**Goal of the task:** Verify every Appendix C item and freeze v1.

**Steps**
- [ ] Static pack (incl. > 30) shared into app and added to WhatsApp consumer **and** Business with correct emojis, tray, names
- [ ] Validator failures are explained in the UI
- [ ] Interrupted conversions resume; deleting packs cleans up files
- [ ] Unit + provider-contract tests green in CI; manual QA on ≥ 2 real devices
- [ ] No secrets in the APK; release build minified; OSS licenses screen present (basic)
- [ ] Only `api.telegram.org` contacted (check with a proxy)

**How to test:** Run the QA matrix: Android 8, 11, 13, 15/16; low-RAM device; slow network; 100+ set; exactly-3 set; 31 set. Tick each item with device/version noted.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

🧪 **Sprint 7 gate = v1.** Tag `v1.0.0-rc1`.

---

# Sprint 8 — TGS frame source (M3a)
**Sprint goal:** Render any TGS to frames reliably. **Demo:** a debug screen scrubs a TGS frame by frame.

### [ ] T8.1 — 🔬 Spike S3: lottie-android vs rlottie
**Refs:** §18 (S3), §9.3, §6 · **Context7:** lottie-android · **Depends on:** T7.5

**Goal of the task:** Decide the TGS renderer.

**Steps**
- [ ] Collect 20 varied TGS files (masks, mattes, gradients, trim paths, expressions)
- [ ] Render with lottie-android; compare to Telegram's own rendering
- [ ] If differences are significant, prototype rlottie via NDK
- [ ] Record the decision and reasons in this task

**How to test (pass criteria):** ≥ 95 % visually identical; no crashes.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Fallback is rlottie via NDK if lottie-android renders any TGS feature incorrectly.
- ➕ _Add lessons after finishing._

### [ ] T8.2 — TgsFrameSource
**Refs:** §7.3 (`FrameSource`), §9.3 steps 1–3 · **Context7:** lottie-android (`LottieComposition`, `LottieDrawable`) · **Depends on:** T8.1

**Goal of the task:** `FrameSource` implementation: gunzip → Lottie JSON → render frame *i* into a cleared ARGB_8888 512×512 bitmap.

**Steps**
- [ ] Gunzip `.tgs`; parse into `LottieComposition`
- [ ] `renderFrame(index, into)`: clear → set frame → draw
- [ ] Expose `width`, `height`, `durationMs`, `frameRate`, `frameCount`; implement `close()`

**How to test:** Instrumented golden test: frames from 2–3 TGS fixtures have expected dimensions, non-empty alpha, and frame count.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 `FrameSource` is shared by TGS and WEBM, so downstream code must not care which one it is.
- ➕ _Add lessons after finishing._

### [ ] T8.3 — Timeline resampling
**Refs:** §9.3 step 4, Appendix A (frame duration, total duration) · **Context7:** — · **Depends on:** T8.2

**Goal of the task:** Resample a (often 60 fps) timeline to a target fps with integer-ms per-frame durations whose sum matches the source.

**Steps**
- [ ] Cumulative rounding so total duration matches
- [ ] Enforce min frame duration and total-duration limits from `WhatsAppLimits`
- [ ] Pure function, no Android dependencies

**How to test:** Unit tests: 60→30, 60→24, 60→12 fps; assert the sum equals source duration ±1 ms and every frame ≥ minimum.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 WebP timestamps are integer milliseconds.
- ➕ _Add lessons after finishing._

🧪 **Sprint 8 gate:** Spike S3 decision recorded; TGS renders correctly.

---

# Sprint 9 — Animated encoding (M3b)
**Sprint goal:** Produce budget-compliant animated WebPs from TGS. **Demo:** a converted TGS plays in a Compose preview and passes the validator.

### [ ] T9.1 — Native animated encoder JNI
**Refs:** §9.5 (`animBegin/AddFrame/Finish/Release`) · **Context7:** libwebp `WebPAnimEncoder` · **Depends on:** T2.3

**Goal of the task:** Streaming animated WebP encoding through JNI with safe handle lifecycle.

**Steps**
- [ ] `animBegin`, `animAddFrame` (un-premultiply as in T2.3), `animFinish`, `animRelease`
- [ ] Handle lifetime guarded so `animRelease` always runs (Kotlin `use`/`finally`)
- [ ] `kmin`/`kmax` exposed

**How to test:** Instrumented: encode 30 frames → decode header shows animation + frame count; abandon an encoder mid-way and confirm no leak.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Always release the encoder in `finally`.
- ➕ _Add lessons after finishing._

### [ ] T9.2 — AnimatedConverter (streaming)
**Refs:** §9.3 step 5, NFR-3 · **Context7:** — · **Depends on:** T9.1, T8.3

**Goal of the task:** Feed frames one at a time from a `FrameSource` into the encoder without ever buffering all frames.

**Steps**
- [ ] Reuse a single bitmap for rendering
- [ ] Apply per-frame durations from the resampler
- [ ] Emit the encoded bytes and total duration

**How to test:** Memory test: converting a long fixture keeps peak memory < 256 MB (heap dump/profiler); output frame count matches.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 Never hold all animation frames in memory (NFR-3).
- ➕ _Add lessons after finishing._

### [ ] T9.3 — Animated BudgetFitter and 🔬 Spike S4
**Refs:** §9.6, §18 (S4), FR-7 · **Context7:** — · **Depends on:** T9.2, T2.5

**Goal of the task:** The `(quality, fps)` ladder that fits animated stickers under the size limit.

**Steps**
- [ ] Ladder `(75,30) (65,30) (55,24) (45,20) (40,15) (30,12)` → stop at first fit
- [ ] Never drop below the plan's minimum fps or 512×512; frame duration ≥ minimum
- [ ] Run the corpus through the ladder and record the pass rate

**How to test (pass criteria S4):** ≥ 90 % of the corpus fits the limit without dropping below 12 fps; unit tests with a fake encoder for ladder logic.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T9.4 — Animated validation and tray icon
**Refs:** §9.8, §9.9, Appendix A · **Context7:** — · **Depends on:** T9.3, T2.6

**Goal of the task:** Extend the validator for animated files and generate the tray from the first frame.

**Steps**
- [ ] Validate animation flag, size, dimensions, frame durations, total duration
- [ ] Tray icon from frame 0, 96×96 PNG
- [ ] Fixtures for each failure type

**How to test:** Validator unit tests with animated fixtures (valid, too long, too big, frame < min duration).

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

🧪 **Sprint 9 gate:** ≥ 90 % of corpus converts within budget.

---

# Sprint 10 — Animated packs end-to-end (M3c)
**Sprint goal:** Animated Telegram packs land in WhatsApp and play correctly. **Demo:** the M3 acceptance run.

### [ ] T10.1 — Animated packs in pipeline and provider
**Refs:** §9.7, §10.2 (`animated_sticker_pack`), FR-10 · **Context7:** — · **Depends on:** T9.4, T3.4

**Goal of the task:** TGS sets flow through chunking, packaging, and the ContentProvider with the animated flag; static and animated never mix.

**Steps**
- [ ] Route TGS stickers to `AnimatedConverter`; mixed sets partition by type first
- [ ] Set `animated_sticker_pack` in provider rows from `PackEntity.animated`
- [ ] Extend provider contract test for an animated pack
- [ ] Remove "TGS not supported" warning on Preview

**How to test:** Provider contract test with an animated pack; mixed-set fixture yields separate packs named `(1/2)`, `(2/2)`.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T10.2 — Animated UI and low-RAM behavior
**Refs:** §12.4, §13, NFR-2 · **Context7:** Coil 3 (animated WebP) · **Depends on:** T10.1

**Goal of the task:** Animated badge, animated previews in Pack Detail, and safe concurrency on low-RAM devices.

**Steps**
- [ ] Coil 3 animated WebP decoding in Pack Detail
- [ ] Animated badge on Result cards
- [ ] Conversion concurrency = 1 for animated when `isLowRamDevice`
- [ ] Time a 30-sticker animated pack (soft target < 3 min)

**How to test:** Manual on a low-RAM device; profiler run showing no OOM.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T10.3 — 🔬 Spike S5 and 🧪 M3 acceptance
**Refs:** §18 (S5), §17 (M3) · **Context7:** — · **Depends on:** T10.1, T10.2

**Goal of the task:** Prove WhatsApp accepts our animated output across versions.

**Steps**
- [ ] Add animated packs on several WhatsApp versions, including Business
- [ ] Run a 10-pack TGS test corpus
- [ ] Record any `validation_error` text verbatim

**How to test (pass criteria):** No `validation_error`; smooth playback; ≥ 90 % of the 10-pack corpus converts within budget and plays correctly in WhatsApp.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

🧪 **Sprint 10 gate = v2.** Tag `v2.0.0-rc1`.

---

# Sprint 11 — Polish (M4)
**Sprint goal:** Accessible, fast, well-explained app. **Demo:** a first-time user completes token setup and an import in < 3 min.

### [ ] T11.1 — Accessibility pass
**Refs:** NFR-6, FR-19 · **Context7:** Compose accessibility semantics · **Depends on:** T10.3

**Goal of the task:** TalkBack labels on all actions, touch targets ≥ 48 dp, dynamic type, and populated `accessibility_text`.

**Steps**
- [ ] Content descriptions/semantics on every actionable element
- [ ] Verify 48 dp targets; test at largest font scale
- [ ] `accessibility_text` = emoji description or "Sticker n of pack"

**How to test:** TalkBack walkthrough of the main flow; Accessibility Scanner; Compose accessibility checks in UI tests.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T11.2 — Settings, About, licenses, theme
**Refs:** §12.6, §19 (licensing), FR-19 · **Context7:** — · **Depends on:** T7.4

**Goal of the task:** Finish Settings: theme (System/Light/Dark), OSS licenses screen, privacy policy link, app version, about/legal.

**Steps**
- [ ] Theme setting applied app-wide
- [ ] OSS licenses screen (libwebp BSD-3, lottie-android Apache-2.0, others)
- [ ] Privacy policy link and app version

**How to test:** Manual: each setting persists across restarts; licenses list matches the dependency report.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T11.3 — Onboarding and empty-state UX refinement
**Refs:** §12.1, §12.7, S1 results · **Context7:** Compose · **Depends on:** T5.5

**Goal of the task:** Apply Spike S1 findings; polish onboarding, empty states, and error screens.

**Steps**
- [ ] Onboarding: what it does, WhatsApp must be installed, IP/personal-use notice
- [ ] Home empty-state 3-step illustration
- [ ] Designed error/empty composables instead of toasts
- [ ] Checkerboard behind transparent art everywhere

**How to test:** Re-run the S1 test with 3 new people; target ≥ 80 % unaided in < 3 min.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T11.4 — Performance and memory
**Refs:** NFR-2, NFR-3, NFR-4 · **Context7:** Android profiling tools · **Depends on:** T10.2

**Goal of the task:** Confirm performance targets on a mid-range device.

**Steps**
- [ ] 30 static stickers < 20 s; 30 animated < 3 min
- [ ] Peak memory < 256 MB during conversion
- [ ] Provider queries < 200 ms
- [ ] Record numbers and fix regressions

**How to test:** Timed runs and profiler traces; store results in this task.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T11.5 — 🧪 M4 acceptance
**Refs:** §17 (M4) · **Context7:** — · **Depends on:** T11.1–T11.4

**Goal of the task:** A new user completes token setup and a first import in < 3 min using only in-app guidance.

**Steps**
- [ ] Clean-install run with a fresh tester and a stopwatch
- [ ] Fix blockers found

**How to test:** Timed usability run passes; accessibility checks pass.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

🧪 **Sprint 11 gate:** M4 acceptance passed.

---

# Sprint 12 — WEBM alpha spike (M5a)
**Sprint goal:** Decide and prototype alpha-preserving VP9 WEBM decoding. **Demo:** frames with correct alpha from 5 sample stickers.

### [ ] T12.1 — 🔬 Spike S2: alpha-preserving VP9 decode
**Refs:** §18 (S2), §9.4 · **Context7:** Android MediaCodec/MediaExtractor, FFmpeg build docs, libvpx/libwebm · **Depends on:** T11.5

**Goal of the task:** Choose the WEBM decode approach.

**Steps**
- [ ] Try MediaCodec (expect opaque frames)
- [ ] Try minimal FFmpeg (Matroska demuxer + libvpx-vp9 only, dynamically linked)
- [ ] Try libvpx + libwebm via JNI
- [ ] Measure APK size increase per ABI and correctness of alpha on 5 samples
- [ ] Record decision, or choose fallback (thumbnail-as-static / mark unsupported)

**How to test (pass criteria):** Correct alpha in frames; APK size increase < ~5 MB/ABI.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- 📌 FFmpeg is LGPL: dynamic linking + attribution required (§19).
- 📌 `MediaCodec` VP9 decoders generally ignore alpha.
- ➕ _Add lessons after finishing._

### [ ] T12.2 — WebmFrameSource
**Refs:** §7.3, §9.4 · **Context7:** depends on the T12.1 decision · **Depends on:** T12.1

**Goal of the task:** Implement `FrameSource` for WEBM using the winning approach (16 KB page-size compatible).

**Steps**
- [ ] Implement `FrameSource` (dimensions, duration, frame rate, `renderFrame`)
- [ ] Verify alpha is preserved in output bitmaps
- [ ] 16 KB page-size compatibility for any new native libs

**How to test:** Golden test: 2–3 WEBM fixtures incl. alpha → frames with transparency, expected count/duration.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

---

# Sprint 13 — WEBM integration (M5b)
**Sprint goal:** Video stickers convert and keep transparency in WhatsApp. **Demo:** M5 acceptance.

### [ ] T13.1 — Pipeline integration
**Refs:** §9.1, §9.4, FR-6 · **Context7:** — · **Depends on:** T12.2, T9.3

**Goal of the task:** Route WEBM stickers through the animated pipeline unchanged downstream.

**Steps**
- [ ] Classifier routes `is_video` → `WebmFrameSource` → `AnimatedConverter`
- [ ] Respect Telegram limits (≤ 3 s, ≤ 30 fps) in the resampler
- [ ] Remove "WEBM not yet supported" warning; keep fallback path if the spike chose it

**How to test:** End-to-end fixture test: WEBM → validated animated WebP.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T13.2 — Golden tests and UI flags
**Refs:** §16 (conversion golden tests) · **Context7:** — · **Depends on:** T13.1

**Goal of the task:** Golden tests for static, TGS, and WEBM (incl. alpha) plus UI format badges.

**Steps**
- [ ] Golden set: static WebP; 2–3 TGS; 2–3 WEBM including alpha
- [ ] Assert 512×512, size within budget, sane frame count/duration, alpha present
- [ ] "Video" badge on Preview/Result

**How to test:** `connectedDebugAndroidTest` golden suite green.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T13.3 — 🧪 M5 acceptance
**Refs:** §17 (M5) · **Context7:** — · **Depends on:** T13.2

**Goal of the task:** Video stickers keep transparency in WhatsApp.

**Steps**
- [ ] Add several video packs on consumer and Business WhatsApp
- [ ] Visually check transparent backgrounds during playback

**How to test:** No opaque backgrounds; no `validation_error`.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

🧪 **Sprint 13 gate = v3.** Tag `v3.0.0-rc1`.

---

# Sprint 14 — Play Store readiness (M6)
**Sprint goal:** Store-ready build and listing. **Demo:** beta track live with a clean pre-launch report.

### [ ] T14.1 — Privacy policy and Data safety form
**Refs:** §15, §19 · **Context7:** — · **Depends on:** T13.3

**Goal of the task:** Publish a privacy policy and complete the Data safety form accurately.

**Steps**
- [ ] Policy states: data fetched directly from Telegram with the user's own token, processed on-device; developer runs no backend and receives no data
- [ ] Data safety: developer collects no data; token stays on device
- [ ] Recheck the dependency list for any SDK that collects data

**How to test:** Review against the final APK's network behavior (only `api.telegram.org`).

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T14.2 — Technical compliance
**Refs:** §19 · **Context7:** Android Gradle Plugin (target API, 16 KB) · **Depends on:** T13.3

**Goal of the task:** Meet Play technical requirements.

**Steps**
- [ ] Target API compliance at release time
- [ ] 16 KB page-size support verified for all native libs
- [ ] R8 minified release; smoke test for reflection/serialization issues
- [ ] Content rating questionnaire

**How to test:** Release AAB installs and runs the full flow; Play pre-launch checks pass.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T14.3 — Store listing and legal notices
**Refs:** §19 · **Context7:** — · **Depends on:** T14.1

**Goal of the task:** Listing copy and legal positioning that follow plan.md.

**Steps**
- [ ] No "WhatsApp" in app name or listing title; descriptive use with correct capitalization only
- [ ] Personal-use IP notice in onboarding and listing
- [ ] Review Telegram Bot API terms and WhatsApp acceptable-use policy; get a legal read before any commercial use

**How to test:** Checklist review against §19; second pair of eyes on the listing text.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T14.4 — Beta track and pre-launch report
**Refs:** §17 (M6) · **Context7:** — · **Depends on:** T14.2, T14.3

**Goal of the task:** Beta release with a clean pre-launch report.

**Steps**
- [ ] Upload to an internal/closed testing track
- [ ] Fix crashes/accessibility issues from the pre-launch report
- [ ] Gather feedback from testers

**How to test:** Pre-launch report shows no critical issues; listing approved.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

### [ ] T14.5 — Docs and alternative distribution
**Refs:** §19 (alternative distribution) · **Context7:** — · **Depends on:** T14.4

**Goal of the task:** README/docs and GitHub Releases APK (and optional F-Droid) path.

**Steps**
- [ ] README: build instructions, token setup, architecture overview
- [ ] GitHub Releases workflow producing a signed APK
- [ ] F-Droid feasibility note (no backend, no embedded secrets)

**How to test:** A fresh clone builds following the README only.

**Mistakes made during the task:**
- _None recorded yet._

**Things to remember for future tasks:**
- ➕ _Add lessons after finishing._

🧪 **Sprint 14 gate:** Store listing approved. 🎉

---

## 6. Risk register (from plan.md §18 and §14)

| Risk | Where handled | Status |
|---|---|---|
| [ ] Token onboarding friction | T5.5, T11.3 | open |
| [ ] Alpha WEBM decode | T12.1 | open |
| [ ] TGS fidelity | T8.1 | open |
| [ ] Animated size budget | T9.3 | open |
| [ ] WhatsApp acceptance of animated output | T10.3 | open |
| [ ] Foreground service on Android 14–16 | T6.4 | open |
| [ ] Appendix A numbers unverified | T2.1 | open |
| [ ] `security-crypto` deprecation | T1.3 | open |
| [x] KSP 2.3.12 + Kotlin 2.4.20 + Hilt 2.60.1 compatibility | T0.4 | open — every version exists, but nothing has compiled them together yet |
| [x] JUnit **6.1.3** (major bump adopted in T0.2 with no tests to validate it) | T0.5 | open — confirm `useJUnitPlatform()` + Platform launcher versions line up on Gradle 9.8 |

## 7. Decisions log
_Record decisions that change plan.md or this file (date, decision, reason)._

| Date | Decision | Reason |
|---|---|---|
| 2026-09-29 | Use **AGP 9.4.1** (latest stable) rather than AGP 8.13.x | Greenfield project; AGP 8 is in maintenance and AGP 10 removes the `newDsl` / `builtInKotlin` opt-outs we would otherwise carry forever. |
| 2026-09-29 | Pin **Kotlin 2.4.20** via a root `buildscript` classpath, overriding AGP's bundled KGP 2.2.10 | Latest stable Kotlin, and the override is the documented AGP 9 mechanism. |
| 2026-09-29 | `compileSdk = 37` (+ `compileSdkMinor = 2`) but **`targetSdk = 36`** | Play mandates 36; `androidx.core:core-ktx:1.19.0` refuses `compileSdk < 37`. Compiling newer than you target is supported and keeps us off the untested Android 17 behaviour-change list. |
| 2026-09-29 | Release build is **unsigned** for now | Signing is a Play-readiness concern (T14.2). `assembleRelease` still proves R8 + `lintVital` work. |
| 2026-09-29 | `MainActivity` is a plain `Activity` + `TextView`; no Compose or AppCompat in T0.1 | T0.2 owns the theme/app shell and T0.4 the DI/Room stack. Keeps the skeleton minimal so a green build actually means something. |
| 2026-09-29 | `android:allowBackup="false"` instead of backup-rule XML | The token DataStore does not exist yet, so there is nothing worth restoring. T1.3 replaces this with explicit `<exclude>` entries once the path is concrete. |
| 2026-09-29 | Install the JDK with **mise** and the SDK **user-locally**; no `pacman` | No system JDK, no Android SDK, and `sudo` requires a password on this machine. |
| 2026-09-29 | `MainActivity` is a plain `ComponentActivity`, **not** AppCompat | The UI is 100 % Compose, so there are no platform widgets to theme. AppCompat would add a dependency and theme-mapping work for nothing. |
| 2026-09-29 | Expose `LocalCheckerColors` / `LocalUsesDynamicColor` from the theme from the start | spec §12.7 needs a checkerboard behind transparent art, and that only works if the checker tones are *independent of* the dynamic surface colour. Retrofitting that after ten screens depend on the theme is expensive. |
| 2026-09-29 | Bump JUnit 5.14.1 → **6.1.3** | Adopt a new major now rather than mid-project; T0.5 is the immediate consumer so any breakage surfaces inside Sprint 0. Logged as a risk. |

## 8. Pinned versions
_Fill in as tasks pin them._ AGP: **9.4.1** · Gradle: **9.8.0** · Kotlin: **2.4.20** · KSP: **2.3.12** · Compose BOM: **2026.09.00** (material3 1.4.0) · JUnit: **6.1.3** · Room: **2.8.5** · Hilt: **2.60.1** · libwebp tag: — · lottie-android: 6.7.1 · Coil: 3.6.3 · WorkManager: 2.12.0 · Navigation: 2.10.2 · OkHttp: 5.5.0 · kotlinx.serialization: 1.11.0 · coroutines: 1.11.0
compileSdk: **37.2** (Android 17.2) · targetSdk: **36** (Play minimum since 2026-08-31) · minSdk: **26** · NDK: **28.2.13676358** (r28c) · buildTools: **37.0.0** · JDK: **21**

## 9. Session log

| # | Date | Tasks touched | Result | Next task |
|---|---|---|---|---|
| 1 | 2026-09-29 | **T0.1** | ✅ Project skeleton: AGP 9.4.1 + Gradle 9.8.0 + Kotlin 2.4.20, `:app` + `:native-webp`, `app.stickerport`, minSdk 26 / target 36 / compile 37.2. `assembleDebug`, `assembleRelease` (R8), `test`, `lint` all green. Toolchain installed from scratch (mise JDK 21, user-local Android SDK, NDK r28c, API 26 AVD). Also fixed the plan.md/spec.md naming mix-up. | T0.2 |
| 2 | 2026-09-29 | **T0.2** | ✅ Material 3 theme (`ui/theme/Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt`), `enableEdgeToEdge()` + `Scaffold` in `ui/components/AppShell.kt`, predictive back in the manifest, 3 `@Preview`s. Dynamic colour proven by pixel sample (`#FAF9FF` on API 36); brand fallback proven on API 26 (`#F2F4F3` light / `#101413` dark). Light + dark on two targets, 0 crashes, 0 lint errors. Release APK 65 KB → 801 KB with Compose. | T0.3 |

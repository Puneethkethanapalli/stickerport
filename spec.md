# Stickerport — Telegram → WhatsApp Sticker Pack Transfer (Android)

**Spec v0.2** · Native Android · Kotlin + Jetpack Compose
*App name: **Stickerport** (final). The app name and package ID must not contain "WhatsApp" (WhatsApp brand guidelines).*

> **How to read this spec.** Numeric limits for WhatsApp stickers (sizes, durations) are from the official `WhatsApp/stickers` sample repo as I recall it. The pack size (3–30) and the `animated_sticker_pack` flag were re-checked; **re-verify every number in Appendix A against the repo README before implementing the validator.** Items marked **[SPIKE]** are unproven and have their own pass/fail criteria in §18.

---

## 1. Summary

An Android app that takes a Telegram sticker pack link, downloads the stickers, converts them to WhatsApp's format, splits them into WhatsApp-sized packs, and hands them to WhatsApp through WhatsApp's official third-party sticker API (ContentProvider + `ENABLE_STICKER_PACK` intent). No bot UI, no accounts, and no backend server: the user supplies their own Telegram bot token (§8.3) and the app talks to Telegram directly.

**Why an app:** WhatsApp has no server-side way to add a sticker pack. Packs can only be added by an app on the user's phone that exposes a ContentProvider WhatsApp reads from. So the app is the product.

## 2. Goals and non-goals

### Goals
- G1. Share a Telegram sticker pack link into the app and end up with the pack in WhatsApp in ≤ 4 taps.
- G2. Static stickers work in v1. Animated (TGS) in v2. Video (WEBM) in v3.
- G3. Automatic handling of the 3–30 stickers-per-pack rule (split large sets, reject too-small ones with a clear reason).
- G4. Every produced sticker is validated against WhatsApp's constraints *before* handoff, so failures are explained by us, not by an opaque WhatsApp error.
- G5. Works offline after download; the library persists packs so they can be re-added.

### Non-goals (v1–v3)
- iOS (see §19 for why).
- Telegram custom-emoji sets and mask sets (detect and show an "unsupported" message).
- Creating stickers from scratch / sticker editor.
- Publishing or sharing packs publicly, cloud sync, user accounts.
- Any WhatsApp Business/Cloud API usage.
- A hosted backend, proxy, or shared bot token (every user brings their own token).
- Signal/other messenger targets (design should not preclude this; see §7 `PackExporter`).

## 3. Personas and primary flows

**Persona:** someone with a Telegram sticker pack they like who wants it in WhatsApp. Non-technical.

### Flow A — Share from Telegram (primary)
1. In Telegram, open a sticker pack → tap **Share** (or copy link) → choose Stickerport.
2. App opens the **Import Preview** screen for that pack (title, count, format, thumbnails grid).
3. User optionally edits pack name → taps **Convert**.
4. **Progress** screen shows per-sticker status; runs in the background if the user leaves.
5. **Result** screen lists the resulting WhatsApp packs (e.g. "Cats (1/2)", "Cats (2/2)"), each with **Add to WhatsApp**.
6. Tapping it launches WhatsApp's own confirm dialog. On return, the pack shows "Added ✓".

### Flow B — Paste a link
Home screen → text field / paste button → same as Flow A from step 2.

### Flow C — Re-add from library
Library → pack → **Add to WhatsApp** (e.g. after WhatsApp reinstall).

## 4. Functional requirements

**Input**
- FR-1. Accept `ACTION_SEND` (`text/plain`) and `ACTION_VIEW` for `https://t.me/addstickers/<name>` and `https://telegram.me/addstickers/<name>`.
- FR-2. Parse these link forms (see §8.1): bare name, `t.me/addstickers/NAME`, `tg://addstickers?set=NAME`, and links embedded in longer shared text.
- FR-3. Recognize `t.me/addemoji/…` and show "Custom emoji packs aren't supported".

**Fetching**
- FR-4. Fetch pack metadata and the sticker list; show title, count, sticker format (static / animated / video), and thumbnails.
- FR-5. Download original sticker files with bounded concurrency and retry/backoff (§8.4).

**Conversion**
- FR-6. Convert every sticker to a WhatsApp-compliant WebP (static or animated). Pass through untouched if the original already complies.
- FR-7. Enforce the **size budget** per sticker with an adaptive quality loop (§9.4). If the budget cannot be met, mark the sticker failed with a reason.
- FR-8. Generate the 96×96 tray icon from the first sticker (first frame if animated).
- FR-9. Preserve each sticker's emoji (Telegram gives one; WhatsApp accepts 1–3).

**Packaging**
- FR-10. Split into packs of 3–30 stickers (§9.6). Never mix static and animated stickers in a pack.
- FR-11. Pack name defaults to the Telegram title (with " (n/m)" suffix when split); user-editable. Publisher defaults to a settings value.
- FR-12. If < 3 valid stickers remain, block with an explanation instead of producing a pack.
- FR-13. Allow the user to deselect individual stickers on the preview screen.

**Handoff**
- FR-14. Expose all ready packs through the sticker ContentProvider (§10).
- FR-15. Launch the add-pack intent per pack; support consumer (`com.whatsapp`) and Business (`com.whatsapp.w4b`) WhatsApp; if both are installed, let the user choose (remember choice).
- FR-16. Detect whether a pack is already added (whitelist check) and reflect it in the UI.
- FR-17. If WhatsApp isn't installed, disable the button and explain.

**Library & settings**
- FR-18. Persist imports and packs; allow delete (removes files and DB rows) and re-add.
- FR-19. Settings: publisher name, WhatsApp target preference, quality preset (Balanced / Smaller files), Telegram bot token (masked; test / replace / remove, §8.3), theme (System/Light/Dark), about/legal.

**Telegram token setup**
- FR-20. First-run **token setup** flow (§12.8): short in-app guide to creating a bot with @BotFather (`/newbot`) and copying the token; paste field; **Verify** calls `getMe`; the token is saved only on success.
- FR-21. Import flows are blocked until a valid token exists. Share/deep-link intents received before setup are held and resumed once setup completes.
- FR-22. On any `401 Unauthorized` from Telegram, prompt the user to replace the token; keep queued work.
- FR-23. The token can be removed at any time from Settings (deletes the encrypted value).

## 5. Non-functional requirements

- NFR-1. **minSdk 26**, targetSdk = latest stable at build time (36 at time of writing — check Play requirements).
- NFR-2. A 30-sticker static pack converts in < 20 s on a mid-range device; a 30-sticker animated pack in < 3 min (soft target).
- NFR-3. Peak memory during conversion < 256 MB; never hold all animation frames in memory.
- NFR-4. ContentProvider queries return in < 200 ms (WhatsApp calls them on its own thread but times out).
- NFR-5. No analytics, ads, or trackers in v1. Only network destination: `api.telegram.org`.
- NFR-6. Accessibility: TalkBack labels on all actions, touch targets ≥ 48 dp, dynamic type. Populate WhatsApp's optional `accessibility_text` with the emoji description or "Sticker n of pack".
- NFR-7. Offline-tolerant: interrupted conversions resume (already-converted stickers are kept).

## 6. Tech stack

| Concern | Choice | Notes |
|---|---|---|
| Language / UI | Kotlin, Jetpack Compose, Material 3 | Compose BOM; edge-to-edge |
| Navigation | Navigation Compose (type-safe routes via kotlinx.serialization) | |
| DI | Hilt | Koin is fine if preferred |
| Async | Coroutines + Flow | `StateFlow` in ViewModels |
| Networking | OkHttp + kotlinx.serialization | Retrofit optional |
| Persistence | Room (packs, stickers, imports) + DataStore (settings) | |
| Background | WorkManager (`CoroutineWorker`, expedited + foreground) | |
| Images | Coil 3 (+ animated WebP decoding) | For converted output previews |
| Lottie (TGS) | `lottie-android` for rendering frames **[SPIKE S3]** | rlottie via NDK as fallback |
| WebP encode | **libwebp via NDK** (static + `WebPAnimEncoder`) | BSD-3; single code path on all API levels |
| WEBM decode | TBD **[SPIKE S2]** | Alpha handling is the risk |
| Secrets | Android Keystore AES-GCM key encrypting the bot token stored in DataStore (e.g. via Tink) | `androidx.security:security-crypto` is deprecated; check current status. Token only ever goes to `api.telegram.org` |
| Build | Gradle KTS, version catalog, CMake for the native module | R8 on for release |
| Testing | JUnit5/Kotest, Turbine, MockK, Robolectric, Compose UI tests, Roborazzi/Paparazzi (optional) | |

## 7. Architecture

Unidirectional data flow (MVVM/MVI-lite): UI observes `StateFlow<UiState>`; user actions are events to the ViewModel; ViewModels call use cases; repositories hide network/DB/files.

### 7.1 Modules (keep it to two Gradle modules for a solo build)
- `:app` — everything Kotlin/Compose, organised by package (below).
- `:native-webp` — CMake build of pinned libwebp + JNI wrapper (§9.5). Isolated so NDK build times don't affect normal builds.

### 7.2 Package layout (`:app`)
```
app.stickerport
├── ui/
│   ├── theme/ · navigation/ · components/          (StickerGrid, PackCard, StatusChip, ...)
│   └── screens/  home/ preview/ progress/ result/ library/ settings/ onboarding/
├── domain/
│   ├── model/    (Pack, Sticker, ImportJob, ConversionResult, ...)
│   └── usecase/  (ParseLink, FetchStickerSet, ConvertStickers, ChunkPacks, ExportToWhatsApp, ...)
├── data/
│   ├── telegram/ (TelegramApi, TelegramSource, dto/)
│   ├── db/       (Room: entities, DAOs, migrations)
│   ├── files/    (PackFileStore)
│   └── settings/ (SettingsRepository, TokenStore)
├── convert/      (StaticConverter, AnimatedConverter, TgsFrameSource, WebmFrameSource, BudgetFitter, Validator)
├── whatsapp/     (StickerContentProvider, WhatsAppLauncher, WhitelistChecker, PackValidator)
└── work/         (ConversionWorker, notifications)
```

### 7.3 Key interfaces
```kotlin
interface StickerSource {                    // Telegram now; others later
    suspend fun fetchSet(name: String): StickerSetInfo
    suspend fun downloadFile(fileId: String, dest: File)
}

interface FrameSource : AutoCloseable {      // TGS and WEBM implement this
    val width: Int; val height: Int
    val durationMs: Int; val frameRate: Float
    fun renderFrame(index: Int, into: Bitmap)   // draws onto a cleared ARGB_8888 bitmap
    val frameCount: Int
}

interface PackExporter {                     // WhatsApp today
    suspend fun isAvailable(): Boolean
    suspend fun isAdded(packId: String): Boolean
    fun buildAddIntent(pack: Pack, target: Target): Intent
}
```

## 8. Telegram integration

### 8.1 Link parsing
Accept (case-sensitive set names, `[A-Za-z0-9_]+`):
- `https://t.me/addstickers/NAME` and `https://telegram.me/addstickers/NAME` (with or without scheme, trailing query/fragment ignored)
- `tg://addstickers?set=NAME`
- A bare `NAME` typed in the paste field
- Any of the above inside shared text (extract with a regex over the whole string)

Reject `addemoji` links with a specific message. Unit-test with ≥ 20 cases including share text from Telegram ("Check out this sticker pack: …").

### 8.2 Bot API calls used
All under `https://api.telegram.org/bot<TOKEN>/…`:
- `getMe` → used only to verify the token during setup.
- `getStickerSet?name=NAME` → `StickerSet { name, title, sticker_type, stickers[] }`. Each `Sticker`: `file_id`, `file_unique_id`, `type`, `width`, `height`, `is_animated`, `is_video`, `emoji`, `thumbnail{file_id,…}`, `file_size`.
- `getFile?file_id=…` → `file_path`; download from `https://api.telegram.org/file/bot<TOKEN>/<file_path>` (bot downloads limited to 20 MB, more than enough).

Format mapping: `!is_animated && !is_video` → **static WebP**; `is_animated` → **TGS**; `is_video` → **WEBM**. `sticker_type` must be `regular`; else unsupported.

### 8.3 Bot token (user-supplied)
The Bot API needs a bot token. Stickerport uses **the user's own token**: no shared token, no backend. The bot is never used for messaging; it is only an API key, so it doesn't need to be added to any chat.

**Setup flow** (first run; also reachable from Settings):
1. In-app guide: open @BotFather (`https://t.me/BotFather`), send `/newbot`, pick a name and username, copy the token BotFather returns.
2. Paste into the field. The **Paste** button reads the clipboard only when tapped (no background clipboard reads).
3. Loose shape check before any network call: `^\d{6,12}:[A-Za-z0-9_-]{30,}$` (Telegram remains the authority).
4. **Verify** calls `getMe`: `ok:true` → save; `401` → "Telegram rejected this token"; network error → retry.

**Storage and handling**
- Encrypted at rest with a Keystore-backed AES-GCM key; decrypted only when a request is made.
- The token is part of the Bot API URL path (`/bot<TOKEN>/…`, `/file/bot<TOKEN>/…`), so: never log full URLs; the OkHttp logging interceptor must redact both patterns; keep it out of crash reports and breadcrumbs.
- Excluded from Android backup/device transfer (`dataExtractionRules` + `fullBackupContent`).
- Settings shows the token masked (bot ID + `••••`), with **Test**, **Replace**, **Remove**.
- If the user revokes the token in BotFather, the next call returns `401` → FR-22.

```kotlin
interface TokenStore {
    suspend fun get(): String?
    suspend fun set(token: String)
    suspend fun clear()
}
```
`TelegramSource` reads from `TokenStore` on every call, so replacing the token takes effect immediately.

**Trade-off:** onboarding costs the user roughly 1–2 minutes. Mitigate with a clear stepper (§12.8), copyable command text, and good error messages.

### 8.4 Networking behaviour
- Concurrency: ≤ 4 parallel downloads (semaphore).
- Handle `429` by honouring `parameters.retry_after`; retry 5xx/IO with exponential backoff (max 4 tries).
- Timeouts: connect 10 s, read 30 s.
- Cache downloaded originals in `cacheDir/tg/<set>/<file_unique_id>` so re-runs and quality changes don't re-download.
- Thumbnails (`thumbnail.file_id`, small static WebP) are used for the preview grid, so no need to decode TGS/WEBM for previews.

## 9. Conversion pipeline

### 9.1 Overview
```
original file ──► classify ──► [static | TGS | WEBM] converter ──► BudgetFitter ──► Validator ──► store
                    │                                                                   │
                    └── already compliant? ─────────── pass through ────────────────────┘
```
Output: `filesDir/packs/<packId>/<index>.webp` plus `tray.png`.

### 9.2 Static stickers
1. Decode with `BitmapFactory` (WebP with alpha is supported on API 26+).
2. If already exactly 512×512, WebP, ≤ 100 KB → **pass through** unchanged.
3. Otherwise: scale to fit 512×512 (aspect preserved), center on a transparent 512×512 ARGB_8888 canvas (Telegram stickers are 512 on one side, ≤ 512 on the other).
4. Encode via native libwebp (lossy + alpha, `method` 4–6, `alpha_quality` ≥ 80). Use libwebp's `target_size` to hit ≤ 100 KB; if the result is still over, step quality down (§9.4).

### 9.3 Animated stickers (TGS)
1. Gunzip `.tgs` → Lottie JSON.
2. Load into `LottieComposition`; build a `LottieDrawable` at 512×512 **[SPIKE S3]** (fallback: rlottie via NDK if lottie-android renders any TGS feature incorrectly).
3. `TgsFrameSource.renderFrame(i, bitmap)`: clear bitmap → set frame → draw.
4. Resample the timeline (TGS is often 60 fps) to the target fps chosen by the BudgetFitter; compute per-frame durations with cumulative rounding so the total matches the source (WebP timestamps are integer ms).
5. Feed frames one at a time to `WebPAnimEncoder` (never buffer all frames).

### 9.4 Animated stickers (WEBM) — v3
Telegram video stickers are VP9 WEBM, usually with an **alpha channel**. Android's `MediaCodec` VP9 decoders generally ignore alpha, so a naive `MediaExtractor` approach yields opaque frames. Options to evaluate in **[SPIKE S2]**:
- a) Minimal FFmpeg build (Matroska demuxer + libvpx-vp9 decoder only) via NDK, dynamically linked (LGPL obligations).
- b) libvpx + libwebm directly via JNI.
- c) Fallback: ship WEBM as "static from thumbnail" or mark unsupported.

Whichever wins implements `FrameSource`, so downstream code is identical to TGS.

### 9.5 Native WebP module (`:native-webp`)
Vendor libwebp (pinned release tag) and expose:
```kotlin
object WebPNative {
    external fun encodeStatic(bmp: Bitmap, quality: Float, method: Int,
                              alphaQuality: Int, targetSizeBytes: Int): ByteArray?
    external fun animBegin(w: Int, h: Int, quality: Float, method: Int,
                           kmin: Int, kmax: Int): Long
    external fun animAddFrame(handle: Long, bmp: Bitmap, timestampMs: Int): Boolean
    external fun animFinish(handle: Long, endTimestampMs: Int): ByteArray?
    external fun animRelease(handle: Long)
}
```
Implementation notes:
- Use `jnigraphics` (`AndroidBitmap_lockPixels`). **Android bitmaps are premultiplied alpha; libwebp expects straight (non-premultiplied) RGBA** — un-premultiply before `WebPPictureImportRGBA`, or you get dark fringes on soft edges.
- Build for `arm64-v8a` and `armeabi-v7a` (+ `x86_64` for emulators). Enable 16 KB page-size compatibility in the NDK/linker config (required for newer targetSdk).
- Always release the encoder in `finally`.

### 9.6 Budget fitting
Pure, testable function; the encoder is injected.
```
Static:   quality ladder  [90, 80, 70, 60, 50, 40, 30] → stop at first result ≤ 100 KB
Animated: attempts (quality, fps) = (75,30) (65,30) (55,24) (45,20) (40,15) (30,12)
          → stop at first result ≤ 500 KB; frame durations ≥ 8 ms; total ≤ 10 s
Fail:     mark sticker FAILED_OVER_BUDGET (keep best attempt size for the message)
```
"Balanced" preset starts at the top of each ladder; "Smaller files" starts lower. Canvas size never drops below 512×512 (WhatsApp requires it).

### 9.7 Splitting into packs
```kotlin
fun chunkSizes(n: Int, max: Int = 30, min: Int = 3): List<Int> {
    require(n >= min)
    val parts = ceil(n / max.toDouble()).toInt()
    val base = n / parts
    val extra = n % parts
    return List(parts) { if (it < extra) base + 1 else base }   // 31 → [16, 15], 61 → [21, 20, 20]
}
```
- Partition first by static vs animated (a pack can't mix), then chunk each partition.
- Naming: `"<title> (1/3)"`; if a partition is the only one, no suffix.
- Preserve original Telegram order.

### 9.8 Tray icon
First sticker (first frame if animated) → 96×96 PNG, ≤ 50 KB, centered on transparent canvas.

### 9.9 Validator (mirrors WhatsApp's rules — see Appendix A)
Runs on every output file and every pack; returns a list of typed errors. The ContentProvider **refuses to list** a pack that fails validation (the official sample does the same), so validate before marking a pack READY.

## 10. WhatsApp integration

### 10.1 Manifest
```xml
<queries>                                   <!-- Android 11+ package visibility -->
    <package android:name="com.whatsapp" />
    <package android:name="com.whatsapp.w4b" />
</queries>

<provider
    android:name=".whatsapp.StickerContentProvider"
    android:authorities="${applicationId}.stickercontentprovider"
    android:exported="true"
    android:readPermission="com.whatsapp.sticker.READ" />

<activity android:name=".ShareReceiverActivity" android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.SEND" />
        <category android:name="android.intent.category.DEFAULT" />
        <data android:mimeType="text/plain" />
    </intent-filter>
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="https" android:host="t.me" android:pathPrefix="/addstickers/" />
        <data android:scheme="https" android:host="telegram.me" android:pathPrefix="/addstickers/" />
    </intent-filter>
</activity>
```
Note: `t.me` links can't be verified App Links for us, so Stickerport appears in the chooser rather than opening automatically. That's expected.

### 10.2 ContentProvider contract
Follow the official `WhatsApp/stickers` **Android sample as the source of truth** (copy the constants; don't retype them from this spec). Shape:

| URI | Returns |
|---|---|
| `content://<authority>/metadata` | Cursor, one row per READY pack |
| `content://<authority>/metadata/<packId>` | Cursor, single pack |
| `content://<authority>/stickers/<packId>` | Cursor, one row per sticker |
| `content://<authority>/stickers_asset/<packId>/<fileName>` | `AssetFileDescriptor` for the file (sticker or tray) |

Pack columns: identifier, name, publisher, tray icon file name, store links, publisher email/website, privacy policy, license, `image_data_version`, `whatsapp_will_not_cache_stickers` (leave false), `animated_sticker_pack`.
Sticker columns: file name, emojis (comma-separated, 1–3), accessibility text.

Rules:
- `packId` = stable UUID string (also the directory name); charset restricted to what the sample allows.
- **Bump `image_data_version` whenever a pack's files change**, otherwise WhatsApp serves a stale cache.
- `publisher_website` = the source Telegram link (attribution).
- Queries run on binder threads: use direct, synchronous DAO/file reads; no main-thread hops.
- Only expose sticker data; nothing else in the provider.

### 10.3 Add-to-WhatsApp intent
```kotlin
Intent().apply {
    action = "com.whatsapp.intent.action.ENABLE_STICKER_PACK"
    putExtra("sticker_pack_id", pack.id)
    putExtra("sticker_pack_authority", "${BuildConfig.APPLICATION_ID}.stickercontentprovider")
    putExtra("sticker_pack_name", pack.name)
    setPackage(target.packageName)              // com.whatsapp | com.whatsapp.w4b
}
```
Launch with `ActivityResultContracts.StartActivityForResult`. On `RESULT_CANCELED`, read the `validation_error` extra (if any) and show it in a "Details" dialog (and log it) — this is the only diagnostic WhatsApp gives.

### 10.4 Already-added check
Query WhatsApp's sticker whitelist provider (consumer and business have separate authorities; copy from the sample's `WhitelistCheck`). Re-check on `onResume` and after the intent returns.

### 10.5 Multi-pack results
Each chunk is its own pack and its own intent. The Result screen lists them with individual buttons; do **not** chain them automatically (each requires a user confirm in WhatsApp).

### 10.6 Limits worth telling users
- Deleting a pack from Stickerport does not remove it from WhatsApp; WhatsApp keeps its own copy.
- Animated packs need a reasonably recent WhatsApp version; on failure, suggest updating.

## 11. Data model (Room)

```
ImportEntity        id, telegramSetName, title, stickerFormat (STATIC|TGS|WEBM), totalCount,
                    status (FETCHED|CONVERTING|DONE|FAILED), createdAt

StickerEntity       id, importId, orderIndex, telegramFileId, fileUniqueId, emoji,
                    sourceFormat, selected (bool),
                    convStatus (PENDING|DOWNLOADED|CONVERTED|FAILED), failReason?,
                    outputFile?, outputBytes?, packId? (assigned after chunking)

PackEntity          id (UUID, stable), importId, name, publisher, animated (bool),
                    trayFile, imageDataVersion (int), status (DRAFT|READY|ADDED),
                    sourceLink, createdAt
```
- `ADDED` is a cache of the whitelist check, not a source of truth; refresh on resume.
- Files: `filesDir/packs/<packId>/{tray.png, 01.webp, 02.webp, …}`.
- Deleting an Import cascades to Stickers/Packs and removes directories.

## 12. UI specification (Compose)

Global: Material 3, dynamic color on Android 12+, edge-to-edge, predictive back. Screens are stateless composables over `UiState` + event lambdas; previews for every state.

### 12.1 Home
- Top: app title; overflow → Settings, About.
- Card: **"Paste a Telegram sticker link"** (text field + paste button + Go). Validates with §8.1 as the user types.
- Section: **Recent** (last 5 imports with status chips) → Library.
- Empty state: 3-step illustration — *Open a pack in Telegram → Share to Stickerport → Add to WhatsApp*.
- First run: onboarding (what it does, that WhatsApp must be installed), followed by **Bot token setup** (§12.8). Until a valid token exists, Home shows a "Set up your bot token" banner and the paste field is disabled.

### 12.2 Import Preview
- Loading: skeleton grid while `getStickerSet` runs.
- Loaded: title (editable field), format badge (Static/Animated/Video), count, `LazyVerticalGrid` (adaptive 88 dp) of Telegram thumbnails; tap toggles inclusion (dimmed = excluded).
- Summary line: *"48 stickers → 2 WhatsApp packs (24 + 24)"*, recalculated live as the selection changes.
- Warnings (inline banners): < 3 selected (Convert disabled), unsupported type, WEBM-not-yet-supported (until v3).
- CTA: **Convert**. Error state: retry, and "Open link in Telegram to check it exists".

### 12.3 Progress
- Overall bar + counter (`12 / 48`), current stage text (Downloading → Converting → Packaging).
- Grid of stickers with per-item status overlay (spinner/check/warning).
- Leaving the screen keeps work running; a notification shows progress.
- **Cancel** (keeps already-converted stickers, marks import as partial).
- On completion → Result. On partial failure: banner *"3 stickers couldn't be converted"* + expandable reasons + option to continue without them (if ≥ 3 remain).

### 12.4 Result
- Card per pack: tray icon, name, count, size total, animated badge, **Add to WhatsApp** button (state: *Add* / *Added ✓* / *WhatsApp not installed*).
- If both WhatsApp variants are installed, a segmented control or dialog picks the target.
- Tap card → Pack Detail (grid with real converted output, animated previews via Coil).

### 12.5 Library
- List of imports/packs with status; swipe-to-delete with undo snackbar; search by name.

### 12.6 Settings
- Publisher name (default "Stickerport"), WhatsApp target, quality preset, Telegram bot token (masked; Test / Replace / Remove), theme, licenses (OSS notices), privacy policy link, app version.

### 12.7 Design notes
- Sticker grids should use a subtle checkerboard behind transparent art.
- Never block the UI thread with conversion; show real progress, not indeterminate spinners, wherever counts are known.
- Empty/error states get their own designed composables, not just toasts.

### 12.8 Bot token setup
- 3-step stepper: **1 Open @BotFather** (button → `https://t.me/BotFather`, browser fallback) → **2 Send `/newbot` and copy the token** (copyable command; token looks like `123456789:ABC…`; keep it private) → **3 Paste and verify**.
- Masked input with reveal toggle, **Paste** button, inline validation, **Verify** button with loading state; error copy per §8.3.
- Success: confirmation, then return to the pending import (if any) or Home.
- Reassurance line: "Your token stays on this device and is only sent to Telegram."
- Optional: `FLAG_SECURE` on this screen so the token doesn't appear in screenshots or the recents preview.

## 13. Background work

- One unique `ConversionWorker` per import (`enqueueUniqueWork(importId, KEEP)`).
- Expedited + foreground (`setForeground`) with an ongoing progress notification. Declare `FOREGROUND_SERVICE` and the appropriate foreground service type for Android 14+ (`dataSync`); request `POST_NOTIFICATIONS` on Android 13+, with graceful behaviour if denied.
- Worker is idempotent: it queries the DB for non-CONVERTED stickers and continues, so process death or cancel/resume is safe.
- Concurrency: downloads ≤ 4; conversions ≤ 2 (memory), 1 for animated on low-RAM devices (`ActivityManager.isLowRamDevice`).
- Progress is written to Room per sticker; the UI observes DB Flows (no direct worker→UI channel).

## 14. Errors and edge cases

| Situation | Behaviour |
|---|---|
| Set not found / deleted | "This pack doesn't exist or was removed." Retry / open in Telegram |
| No network / timeout | Retry with backoff; then error with Retry button |
| `429` from Telegram | Honour `retry_after`; show "Telegram is rate-limiting, waiting Ns" |
| `401 Unauthorized` | Token invalid or revoked: prompt to replace it; keep queued work |
| No token configured | Route to token setup (§12.8); resume the pending import afterwards |
| Sticker over budget after all attempts | Mark FAILED with size; offer to continue without it |
| Fewer than 3 valid stickers | Block; explain WhatsApp's 3-sticker minimum |
| Set > 30 stickers | Auto-split (§9.7), shown on preview |
| Mixed static + animated set | Split into separate packs by type |
| Disk full | Detect `IOException`/low storage before starting (estimate ≈ 0.6 MB per animated, 0.1 MB static sticker), show clear message |
| WhatsApp not installed | Disable button; link to Play Store |
| WhatsApp returns `validation_error` | Show details dialog; log; **do not silently retry** |
| WhatsApp too old for animated packs | "Update WhatsApp to add animated packs" |
| Provider called for pack being rewritten | Serve last READY snapshot; write packs into a temp dir then atomically rename |
| Process death mid-conversion | Resume via WorkManager (§13) |
| Custom emoji / mask sets | Unsupported message (FR-3) |
| Telegram changes API/format | Fail with "unsupported format" including the raw `is_animated/is_video` flags in the debug log |

## 15. Security and privacy

- The ContentProvider is `exported=true` **by necessity**; restrict with `readPermission="com.whatsapp.sticker.READ"` and expose only sticker/pack data.
- HTTPS only (`cleartextTrafficPermitted=false` in the network security config).
- Bot token: encrypted at rest with a Keystore-backed key, excluded from backups, never logged (URL redaction, §8.3), and sent only to `api.telegram.org`.
- Downloaded originals live in `cacheDir` (OS may purge); converted packs in `filesDir`. Exclude neither from backup by default, but exclude `cacheDir`.
- No analytics or crash SDKs in v1. If crash reporting is added later, disclose it in the Play Data safety form and privacy policy.
- Privacy policy: states that sticker data is fetched directly from Telegram using the user's own bot token and processed on-device; the developer operates no backend and receives no data.

## 16. Testing strategy

**Unit**
- Link parser (≥ 20 cases), `chunkSizes` (property test: sums to n, each 3–30 for n ≥ 3, differences ≤ 1), `BudgetFitter` with a fake encoder, `PackValidator` against fixtures (valid, oversize, wrong dimensions, mixed types, too few/many).

**Instrumented / integration**
- **Provider contract test:** insert a pack, query all four URIs through `ContentResolver`, assert columns, row counts, and that asset file descriptors open and match byte lengths.
- **Conversion golden tests:** a small fixture set (static WebP; 2–3 TGS; 2–3 WEBM incl. alpha) → assert output dimensions 512×512, size within budget, frame count/duration sane, alpha channel present.
- Worker resume test (kill mid-run, verify no re-download of finished items).

**UI (Compose)**
- State-based tests for Home / Preview / Progress / Result (loading, error, partial-failure, WhatsApp-missing states).

**Manual QA matrix**
- WhatsApp consumer and Business; Android 8, 11, 13, 15/16; a low-RAM device; a slow network; a 100+ sticker set; a set with exactly 3 stickers; a set with 31 stickers; an animated set with heavy vector content.

## 17. Milestones

| M | Scope | Acceptance |
|---|---|---|
| **M0** | Project skeleton, theme, navigation, Room, DI, CI (build + unit tests) | App launches; CI green |
| **M1** | **Static end-to-end.** Link parsing, user-token Telegram source (basic token entry in Settings), static conversion via `:native-webp`, chunking, validator, ContentProvider, add-to-WhatsApp, Result + Library | A static pack (incl. > 30 stickers) lands in real WhatsApp on 2 devices |
| **M2** | Guided token setup (§12.8), share-sheet/deep-link entry, WorkManager + notification, Preview with deselection, resume/cancel, polished errors | Killing the app mid-conversion resumes correctly |
| **M3** | **Animated (TGS):** frame source, animated encoder, budget fitting, animated packs | ≥ 90 % of a 10-pack TGS test corpus converts within budget and plays correctly in WhatsApp |
| **M4** | Settings polish, accessibility pass, onboarding and token-setup UX refinement | A new user completes token setup and a first import in < 3 min using only in-app guidance |
| **M5** | **WEBM (alpha)** per spike outcome | Video stickers keep transparency in WhatsApp |
| **M6** | Play Store readiness (§19), beta track, docs | Passes pre-launch report; store listing approved |

## 18. Spikes and open risks

| ID | Question | How to test | Pass criteria |
|---|---|---|---|
| S1 | Token onboarding friction | Have 3–5 people who have never used BotFather follow the in-app guide | ≥ 80 % finish setup unaided in < 3 min |
| S2 | Alpha-preserving VP9 WEBM decode on Android | Try MediaCodec, minimal FFmpeg, libvpx+libwebm on 5 alpha sample stickers | Frames come out with correct alpha; APK size increase acceptable (< ~5 MB/ABI) |
| S3 | TGS fidelity with `lottie-android` vs rlottie | Render 20 varied TGS files, diff against Telegram's own rendering | ≥ 95 % visually identical; no crashes |
| S4 | Animated WebP budget on real stickers | Encode corpus through the fitting ladder | ≥ 90 % ≤ 500 KB without dropping below 12 fps |
| S5 | WhatsApp acceptance of our animated output | Add packs on several WhatsApp versions incl. Business | No `validation_error`; animation plays smoothly |
| S6 | Foreground-service behaviour on Android 14–16 | Long conversion with app backgrounded | Completes; no `ForegroundServiceStartNotAllowedException` |

## 19. Distribution, legal, and platform notes

- **Android only.** iOS uses a different pasteboard-based handoff, and WhatsApp itself warns Apple may reject apps that only export stickers; revisit after Android is proven.
- **Naming/branding:** no "WhatsApp" in the app name or Play listing title; use "WhatsApp" only descriptively, with correct capitalization.
- **IP:** sticker artwork belongs to its creators. Position the app as *personal-use conversion*; keep packs private on device; never add public sharing/upload features; put a short notice in onboarding and the store listing; include `publisher_website` attribution back to the source pack. Get a proper legal read before commercial use.
- **Terms:** review Telegram's Bot API terms and WhatsApp's terms/acceptable-use policy for sticker apps.
- **Play Store:** privacy policy URL, Data safety form (developer collects no data; the token stays on-device), target API compliance, 16 KB page-size support for native libs, content rating.
- **Licensing:** libwebp (BSD-3), lottie-android (Apache-2.0), FFmpeg (LGPL, only if chosen — needs dynamic linking + attribution). Ship an OSS licenses screen.
- **Alternative distribution:** GitHub Releases APK / F-Droid (no backend or embedded secrets, so it fits well).

---

## Appendix A — WhatsApp sticker constraints (VERIFY against `WhatsApp/stickers` README before coding)

| Item | Value (as recalled unless marked ✔) |
|---|---|
| Stickers per pack | 3–30 ✔ |
| Pack type | Static or animated, not mixed; `animated_sticker_pack` flag for animated ✔ |
| Static sticker | 512×512 px, WebP, ≤ 100 KB |
| Animated sticker | 512×512 px, animated WebP, ≤ 500 KB, min frame duration 8 ms, total ≤ 10 s |
| Tray icon | 96×96 px, PNG (or WebP), ≤ 50 KB |
| Emojis per sticker | 1–3 |
| Pack name / publisher | length-limited (128 chars in the sample); identifier restricted charset |
| Links | must start with `http`/`https` ✔ |
| Transparency | Transparent background expected; a small transparent margin around the artwork is recommended (check current guidance) |

## Appendix B — Telegram sticker formats (input side)

| Type | Container | Typical limits (Telegram side) |
|---|---|---|
| Static | WebP | one side 512 px, ≤ 512 KB |
| Animated | `.tgs` (gzipped Lottie JSON) | 512×512, ≤ 3 s, small file (tens of KB) |
| Video | `.webm` (VP9, usually alpha) | one side 512 px, ≤ 3 s, ≤ 30 fps, ≤ 256 KB |

## Appendix C — Definition of done (v1 = M0–M2)

- A static Telegram pack (including one > 30 stickers) can be shared into the app and added to WhatsApp (consumer and Business) with correct emojis, tray icon, and names.
- All conversions pass the in-app validator; failures are explained in the UI.
- Interrupted conversions resume; deleting packs cleans up files.
- Unit + provider-contract tests green in CI; manual QA matrix (§16) passed on ≥ 2 real devices.
- No secrets in the APK; release build minified with R8; OSS licenses screen present.

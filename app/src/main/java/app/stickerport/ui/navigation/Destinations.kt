package app.stickerport.ui.navigation

import kotlinx.serialization.Serializable

/**
 * Every destination in the app, as a `@Serializable` type (spec §6: "Navigation Compose
 * type-safe routes via kotlinx.serialization").
 *
 * This file is the **single source of truth for navigation**. There are deliberately no string
 * route templates anywhere in the codebase: a route is a Kotlin class, so a rename is a compile
 * error rather than a runtime `IllegalArgumentException`, and arguments are checked by the
 * compiler instead of being parsed out of a URL at runtime.
 *
 * ## Why `data object` for argument-less destinations
 * `data object` gives each one a unique type with no instances to allocate, so
 * `composable<Home> { … }` and `navigate(Home)` read the same as a value. Using `data object`
 * rather than `enum class` is the current recommendation — serializing an enum instance leaks the
 * enum's `name()` into the route pattern, so renaming the enum breaks deep links.
 *
 * ## Route argument conventions
 * - `setName` — a Telegram set name such as `PackByPack_Animals`. This is a *name*, never a full
 *   `t.me` URL: §14 forbids logging or storing full Telegram links, and a name is all
 *   `getStickerSet` needs.
 * - `importId` — `ImportEntity.id` (Room auto-generated `Long`).
 * - `packId` — `PackEntity.id`, a stable UUID string.
 */
@Serializable
data object Home

/** First-run flow (§12.1 → §12.8). Shown once, then never again unless asked. */
@Serializable
data object Onboarding

/** All past imports and packs (§12.5). */
@Serializable
data object Library

@Serializable
data object Settings

/**
 * Import Preview (§12.2): fetch a set and choose which stickers to convert.
 *
 * Navigated to from Home when a link is submitted, or from the Telegram share sheet / `t.me` deep
 * link (T5.2). Takes the *set name* because that is what the Telegram API needs and it keeps
 * user-supplied URLs out of the back stack.
 */
@Serializable
data class Preview(val setName: String)

/** Conversion progress (§12.3). Work continues if the user leaves. */
@Serializable
data class Progress(val importId: Long)

/** Per-pack results after conversion (§12.4). */
@Serializable
data class Result(val importId: Long)

/**
 * Pack Detail (§12.4): the real converted output for one pack, with animated previews.
 *
 * Not in the original T0.3 list of seven, but §12.4 requires it and the route set is cheap to
 * extend now and expensive to extend once real screens are wired to it.
 */
@Serializable
data class PackDetail(val packId: String)

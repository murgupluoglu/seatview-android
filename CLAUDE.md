# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

`seatview-android` is an Android library that renders an interactive, pannable/zoomable seat map on a `Canvas` (e.g. for cinema/venue seat selection). It is published via JitPack (`com.github.murgupluoglu:seatview-android`). The repo is a two-module Gradle project:

- `:seatview` — the library (module published to consumers). All reusable code lives here.
- `:app` — a sample app demonstrating the library. Never put library code here; the app exists to exercise `:seatview`.

The library is at **v4**, a deliberately breaking rewrite of the v3 API (see `CHANGELOG.md` for the migration table).

## Commands

```bash
# Build everything (compile + lint + JVM unit tests)
./gradlew build

# Build the library AAR only
./gradlew :seatview:assembleRelease

# Install & run the sample app on a connected device/emulator
./gradlew :app:installDebug

# JVM unit tests (host machine, no device) — the bulk of the suite, via Robolectric
./gradlew :seatview:testDebugUnitTest
./gradlew :seatview:testDebugUnitTest --tests "com.murgupluoglu.seatview.SeatViewLogicTest"

# Instrumented tests — REQUIRE a connected device/emulator
./gradlew :seatview:connectedDebugAndroidTest   # accessibility + instance state + real Canvas
./gradlew :app:connectedDebugAndroidTest        # gestures through the real touch pipeline

# Single instrumented test class
./gradlew :app:connectedDebugAndroidTest \
  -Pandroid.testInstrumentationRunnerArguments.class=com.murgupluoglu.seatviewsample.SeatViewGestureTest
```

Toolchain: Android Gradle Plugin 9.3.1, Gradle 9.5, **AGP built-in Kotlin** (there is no standalone `org.jetbrains.kotlin.android` plugin — do not re-add it, it collides with AGP's `kotlin` extension), Java 17 toolchain via `kotlin { jvmToolchain(17) }`, `compileSdk`/`targetSdk` 37, `minSdk` 21. `local.properties` must point `sdk.dir` at the Android SDK.

`gradle.properties` intentionally contains **no `android.*` flags** — AGP 9 defaults (including `useAndroidX`) are correct. Don't reintroduce legacy compatibility flags.

`:seatview` depends only on `androidx.core` + `androidx.customview` — deliberately *not* appcompat, so consumers aren't forced to take it. Keep library dependencies narrow.

## Architecture

The public surface is one generic custom `View` (`SeatView<SEAT : Seat>`) plus pluggable strategy interfaces. Three collaborators were extracted from the view to keep it testable; they are `internal`.

### `SeatView<SEAT : Seat>` (`seatview/.../SeatView.kt`)
The core `View`. Consumers give it a 2D grid `Array<Array<SEAT>>` via `initSeatView(list, optionalConfig)`. **The grid is `[row][column]`** — outer array = rows, inner = columns. `initSeatView` validates that the grid is non-empty and rectangular and that the config's width bounds are ordered, throwing `IllegalArgumentException` otherwise.

Public API:
- `selectSeat(SeatPosition)` / `selectSeat(id: String)` → `SEAT?` — rejects invalid positions, invisible seats, non-selectable seats (`Seat.canSelect()`), and already-selected seats.
- `releaseSeat(SeatPosition)` / `releaseSeat(id: String)` → `Boolean`
- `clearSelection()`, `isSelected(id)`
- `selectedSeatIds: Set<String>` — an **independent snapshot** on every read, so `selectedSeatIds.forEach { releaseSeat(it) }` is safe.
- `seatAt(position)`, `positionOf(id)`, `seatRect(position)` (on-screen rect, e.g. to anchor a tooltip)
- `rowCount` / `columnCount`
- `isZoomEnabled` — runtime pinch toggle and the **single source of truth** for zoom; deliberately not in `SeatViewConfig`, and never reset by `initSeatView`
- `seatDrawer`, `extensions`, `seatViewListener`

`seatViewListener` defaults to a **no-op**, so interacting before assigning one never crashes. `seats` is private; there is no public unchecked accessor — use `seatAt`, which is bounds-checked and returns null.

### Two coordinate rectangles (the heart of pan/zoom)
- `parameters.windowRectF` — the on-screen viewport (view bounds minus config paddings). Computed by `updateWindowRect(w, h)`, called from both `onSizeChanged` **and** `initSeatView` (a config supplied after layout must drive the paddings).
- `parameters.virtualRectF` — the full seat-grid rectangle in canvas space; it is the thing that moves and scales.

`SeatViewport` (internal) owns all of it: `rebuildVirtualRect()` centers the grid, `pan(dx, dy)` offsets then clamps, `clamp()` centers on an axis when the grid is smaller than the window and otherwise prevents the edges drifting inside the window. Pan and zoom share this single clamp model — don't add a second one.

Seat screen positions come from `getSeatRect`, which adds the per-seat rect additions; hit-testing (`positionAt`) inverts that: an exact draw-rect hit wins, otherwise it falls back to the pitch-based cell that owns the surrounding gap (so inter-seat gaps stay tappable).

### `SeatSelection<SEAT>` (internal)
Pure state holder for selected ids: `initFrom` (pre-selected seats, skipping invisible ones), `select`, `release`, `clear`, `restore`. It knows nothing about `View`; `SeatView` fires listener callbacks and calls `invalidate()` based on the booleans it returns.

### `Seat` interface (`Seat.kt`) — the model contract
Only `id()` and `name()` are required; **every other member has a default**, and the library is compiled with `-Xjvm-default=all` so Java consumers inherit them as real JVM default methods.
- `id()` — must be unique; it is the key in the selection set.
- `isVisible()` (default `true`) — invisible seats model grid gaps: never drawn, never tappable, never selectable, and skipped by pre-selection.
- `canSelect()` (default `true`) — static model-level eligibility, enforced by `selectSeat`. Distinct from `SeatViewListener.canSelectSeat`, which is the dynamic interaction-time veto.
- `isPreSelected()` (default `false`)
- `allConnectedSeatIds()` (default empty) — multi-seat grouping; selecting/releasing a seat also selects/releases every connected id. May or may not include the seat's own id; both are tolerated.
- `leftRectAddition()` / `rightRectAddition()` (default `0f`) — per-seat horizontal adjustments used to visually merge multi-seats.

### `SeatDrawer` strategy (`seatdrawer/SeatDrawer.kt`)
`fun draw(scope: SeatDrawScope)` — a single parameter object (canvas, seat, rect, `isSelected`, `parameters`, `config`, `context`) so the contract can gain fields without breaking custom drawers. Default is `NumberSeatDrawer`. The sample's `CachedMultipleSeatDrawer` shows the bitmap-caching pattern (and is why `context` is part of the scope).

### `SeatViewExtension` strategy (`extensions/SeatViewExtension.kt`)
Overlays drawn after seats. `init()` is guaranteed to run before the first `draw()` — enforced by `ensureExtensionsInitialized()`, which is keyed on `(config, windowRect)` so the normal "layout then initSeatView" path does not double-init, while a config or size change re-inits. Built-in: `CenterLinesExtension`, `DebugExtension`.

### Accessibility (`SeatViewAccessibilityHelper.kt`, internal)
An `ExploreByTouchHelper` exposes each visible seat as a virtual node (name, checked state, click action), so TalkBack can explore and toggle canvas-drawn seats. Virtual ids are `row * columnCount + column`. Selecting a seat invalidates its node **and every connected seat's node**; a pan/zoom invalidates the tree once when the gesture ends.

### Instance state
`onSaveInstanceState`/`onRestoreInstanceState` persist the selection across rotation. `SavedState` must stay **non-private** (`internal`): a Kotlin `private` nested class compiles to a package-private JVM class and `Parcel` resolves `CREATOR` reflectively, which can fail with `BadParcelableException`.

Restore rules (each one exists because violating it produced a real bug):
- An id survives only if it belongs to a seat that is currently **visible and selectable**, or is a connected id of such a seat — the same bar `selectSeat` enforces, so a restore can never create a selection the user could not have made or cannot release.
- The result is announced **once** via `SeatViewListener.selectionRestored(ids)` — *not* per-seat `seatSelected`, which would fire N times for one connected group and double-count.
- That notification is queued until a real listener is assigned, so `initSeatView(...)` followed by `seatViewListener = ...` still receives it.
- If the grid has not arrived yet the ids stay in `pendingSelection`; `onSaveInstanceState` persists that pending set (so a second rotation does not lose it) and `clearSelection()` discards it (so clearing while loading is not undone).

### Config vs Parameters (deliberate split)
- `SeatViewConfig` — immutable (`val`) consumer inputs: paddings, seat min/default/max width in **dp** (`seatMinWidthDp`, …), width:height and gap ratios, background color. No `Resources.getSystem()`; dp→px conversion happens in `initSeatView` against the view's own density. `initSeatView` validates the width bounds are ordered and that `seatWidthHeightRatio > 0` (it is a divisor).
- `SeatViewParameters` — runtime-computed state in **px**: the two rectangles, current seat width/height/gaps, `seatMinWidth`/`seatMaxWidth`, grid size.

## Testing

- **JVM unit tests** (`seatview/src/test`, Robolectric + Google Truth) are the bulk of the suite and cover selection, validation, geometry (`SeatViewportTest`), the draw loop, and one regression test per reviewed defect (`SeatViewRegressionTest`).
  - The SDK levels are set once in `seatview/src/test/resources/robolectric.properties` (`sdk=34,35,36,37`) — not with `@Config` per class. **Every test runs once per level**, so the reported count is 4× the number of test methods.
  - SDK 36+ requires the tests to run on **Java 21** and needs `--add-exports=java.base/jdk.internal.access=ALL-UNNAMED` (Robolectric reaches `SharedSecrets` via `ApplicationSharedMemory`). Both are configured in `seatview/build.gradle.kts` on `tasks.withType<Test>` only, so the library itself still compiles to Java 17 bytecode and consumers are not pushed onto a newer JDK.
- **Instrumented tests** cover what the JVM can't: real `Canvas`, accessibility node tree, Parcel round-trip of instance state (`:seatview`), and gestures through `onTouchEvent` (`:app`).
- All assertions use **Google Truth** (`assertThat`), not JUnit asserts.
- When adding a test, make sure it can fail: prefer strict assertions over "did not get worse", and check the XML for `<skipped/>` — an `assumeTrue` that never holds is a silently vacuous test.

## Notes / gotchas

- `seats` is `lateinit` internally; every public entry point tolerates being called before `initSeatView` (returns null / no-ops / draws nothing) rather than throwing.
- Selection count reflects connected seats: selecting one multi-seat can add several ids.
- The sample app's cinema data comes from `app/src/main/assets/sample.json`, parsed with `org.json` in `CinemaScreenActivity`.
- The sample app uses **Timber** for logging, planted in `App.onCreate` behind `BuildConfig.DEBUG` (which requires `buildFeatures { buildConfig = true }`).
- `AccessibilityNodeInfoCompat.setBoundsInParent` is deprecated but required by `ExploreByTouchHelper`; the suppression in `SeatViewAccessibilityHelper` is deliberate.

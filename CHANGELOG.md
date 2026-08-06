# Changelog

## 4.0.0

A breaking API release. The public surface was reshaped for safety and Kotlin idiom, the
view was split into testable collaborators, and accessibility plus state restoration were
added. Highlights first, then the full migration table.

### Added

- **Accessibility**: each visible seat is exposed as a virtual node via `ExploreByTouchHelper`,
  so TalkBack can explore the map, announce a seat's name and selected state, and toggle it.
- **Instance state**: `SeatView` saves and restores its selection across configuration
  changes on its own. Restored ids are validated against the current grid and `seatSelected`
  is re-emitted so consumer-derived state (seat count, price) stays in sync.
- **Selection by id**: `selectSeat(id)`, `releaseSeat(id)`, `isSelected(id)`, `clearSelection()`.
- `SeatViewListener.selectionRestored(selectedSeats)` — fired once after instance state is restored,
  so consumers rebuild derived state without `seatSelected` being synthesised per seat.
- `seatRect(position)` for anchoring tooltips/popups to a seat.
- `rowCount` / `columnCount`, and `seatAt(position)` which is bounds-checked.
- `SeatPosition(row, column)` value type, replacing ambiguous raw `(x, y)` / `Pair` coordinates.
- Grid and config validation in `initSeatView` (non-empty, rectangular, ordered width bounds).
- Interface defaults on `Seat` and `SeatViewListener`, compiled with `-Xjvm-default=all` so
  **Java** consumers inherit them too.

### Changed (breaking)

| v3 | v4 |
| --- | --- |
| `selectSeat(xIndex, yIndex)` | `selectSeat(SeatPosition(row, column))` or `selectSeat(id)` |
| `releaseSeat(xIndex, yIndex)` | `releaseSeat(SeatPosition(row, column))` or `releaseSeat(id)` |
| `selectedSeats: HashSet<String>` | `selectedSeatIds: Set<String>` (independent snapshot per read) |
| `getSeat(x, y)` (unchecked) | `seatAt(position): SEAT?` (bounds-checked) |
| `getSeatPosition(id): Pair<Int, Int>?` | `positionOf(id): SeatPosition?` |
| `isSafeSelect(x, y)` | removed — `seatAt` returns null instead |
| `config.isZoomActive` | `seatView.isZoomEnabled` (runtime property) |
| `SeatViewConfig(seatMinWidth = 12f.dp2px(), …)` | `SeatViewConfig(seatMinWidthDp = 12f, …)` — dp in, converted internally |
| `Float.dp2px()` | removed (it read the global `Resources.getSystem()`) |
| `SeatDrawer.draw(context, params, config, canvas, seat, seatRectF, isSelected)` | `SeatDrawer.draw(scope: SeatDrawScope)` |
| `SeatViewListener` methods take `HashSet<String>` | they take `Set<String>`, and all have defaults |
| `seatViewListener` is `lateinit` (crashes if unset) | defaults to a no-op |
| `SeatViewConfig(zoomEnabled = …)` | removed — `seatView.isZoomEnabled` is the single source of truth and survives `initSeatView` |
| `seats`, `parameters`, `config` public and mutable | `private` / `internal` |
| `SeatViewConfig` all `var` | all `val` (immutable) |

Behavioral changes worth knowing:

- `selectSeat` now honors `Seat.canSelect()` **and** `Seat.isVisible()`. A seat that models a
  gap or is not selectable can no longer be force-selected programmatically. `isPreSelected()`
  on an invisible seat is likewise ignored.
- `Seat.isVisible()`, `canSelect()`, `isPreSelected()`, `allConnectedSeatIds()`,
  `leftRectAddition()`, `rightRectAddition()` all have defaults — only `id()` and `name()`
  must be implemented.

### Fixed

- Crash (`ArrayIndexOutOfBoundsException`) on the first frame from an off-by-one in the draw loop.
- Crash (`BadParcelableException`) restoring instance state, caused by a package-private
  `SavedState` whose `CREATOR` could not be resolved reflectively.
- Crash (`IllegalArgumentException: Cannot coerce value to an empty range`) on the first pinch
  when a config's min width exceeded its max width; such a config is now rejected up front.
- Extensions could have `draw()` called before `init()` when the grid was supplied asynchronously.
- A config supplied after layout did not recompute the window rect, so paddings were ignored
  and the grid was centered against stale values.
- Tap targets shrank to the drawn seat rect, making the gaps between seats dead zones.
- `ConcurrentModificationException` from the natural `selectedSeats.forEach { releaseSeat(it) }`
  idiom, because the live internal set was exposed.
- Connected seats' accessibility nodes kept advertising a stale checked state.
- Pinch-zoom now clamps around the focal point with a single shared clamp model (pan and zoom
  no longer use two different ones); the `//TODO must be improved` zoom math is gone.

### Internal / build

- Migrated to AGP 9.3.1 + Gradle 9.5 with **AGP built-in Kotlin** (the standalone
  `org.jetbrains.kotlin.android` plugin is gone), Kotlin DSL build scripts, and a version catalog.
- `compileSdk`/`targetSdk` 37, Java 17 toolchain, `minSdk` 21.
- Removed all legacy `android.*` flags from `gradle.properties` (AGP 9 defaults are correct).
- `:seatview` now depends on `androidx.core` + `androidx.customview` instead of the whole of
  `appcompat`, so consumers are not forced onto appcompat.
- `SeatView` was split into `SeatSelection`, `SeatViewport`, and `SeatViewAccessibilityHelper`
  (all `internal`), making selection and pan/zoom geometry unit-testable without a device.
- Test suite: 68 JVM unit tests run against **four SDK levels** (Robolectric `sdk=34,35,36,37`, so 272
  executions) plus 24 instrumented tests, all using Google Truth. Unit tests run on a Java 21
  launcher (required by Robolectric for SDK 36+) while the library still targets Java 17 bytecode.
- Publishing: added the `maven-publish` plugin with a `release` publication and a sources jar, plus
  `jitpack.yml` pinning JDK 17 — JitPack requires both (it defaults to Java 8 and needs
  `maven-publish` to produce an installable artifact).
- Sample app: replaced `com.blankj:utilcodex` with Timber for logging and plain
  `startActivity(Intent(...))`, which also removed a `NotificationPermission` lint suppression.

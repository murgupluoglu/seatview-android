<img src="github/sample.gif" width="300" height="666"/>

# Installation

[![](https://jitpack.io/v/murgupluoglu/seatview-android.svg)](https://jitpack.io/#murgupluoglu/seatview-android)

```gradle
dependencyResolutionManagement {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.murgupluoglu:seatview-android:lastVersion'
}
```

Requires `minSdk` 21.

> Upgrading from v3? See [CHANGELOG.md](CHANGELOG.md) for the full migration table — v4 is a breaking release.

# Usage

```xml
    <com.murgupluoglu.seatview.SeatView
    android:id="@+id/seatView"
    android:layout_width="0dp"
    android:layout_height="0dp" />
```

Implement `Seat` on your own model. Only `id()` and `name()` are required — everything
else has a sensible default:

```kotlin
class BasicSeat(private val id: String) : Seat {
    override fun id(): String = id      // must be unique
    override fun name(): String = id
}
```

The grid is `[row][column]`:

```kotlin
val seatList: Array<Array<BasicSeat>> = Array(10) { row ->
    Array(10) { column -> BasicSeat("$column-$row") }
}

seatView.initSeatView(seatList)
```

The listener is optional, and so is each of its methods:

```kotlin
seatView.seatViewListener = object : SeatViewListener<BasicSeat> {
    override fun seatSelected(selectedSeat: BasicSeat, selectedSeats: Set<String>) {
    }

    override fun seatReleased(releasedSeat: BasicSeat, selectedSeats: Set<String>) {
    }

    // Return false to veto a tap (e.g. a "max 6 seats" rule).
    override fun canSelectSeat(clickedSeat: BasicSeat, selectedSeats: Set<String>): Boolean {
        return selectedSeats.size < 6
    }

    // Fired once after a rotation restores the selection — rebuild your total/order here.
    // (Not seatSelected, which would fire once per seat of a connected group.)
    override fun selectionRestored(selectedSeats: Set<String>) {
    }
}
```

## Selection API

```kotlin
seatView.selectSeat("A-3")                            // by id
seatView.selectSeat(SeatPosition(row = 0, column = 2)) // by position
seatView.releaseSeat("A-3")
seatView.clearSelection()

seatView.isSelected("A-3")        // Boolean
seatView.selectedSeatIds          // Set<String> — a snapshot, safe to iterate while mutating
seatView.positionOf("A-3")        // SeatPosition?
seatView.seatAt(position)         // SEAT? (bounds-checked)
seatView.seatRect(position)       // RectF? — e.g. to anchor a tooltip
seatView.rowCount / columnCount
```

Selecting a seat also selects every id from `allConnectedSeatIds()`, so a couch sold as a
unit is selected and released atomically.

## Configuration

`SeatViewConfig` is immutable and sizes are in **dp**:

```kotlin
seatView.initSeatView(
    seatList,
    SeatViewConfig(
        seatMinWidthDp = 12f,
        seatDefaultWidthDp = 13f,
        seatMaxWidthDp = 30f,
        topPadding = 300f,       // room for a screen banner, etc.
    )
)

// Zoom is a runtime property, not part of the config — it is never reset by initSeatView.
seatView.isZoomEnabled = false
```

The selection survives configuration changes automatically — give the view an
`android:id` and it saves/restores its own state.

## Accessibility

Seats are drawn on a `Canvas`, so `SeatView` exposes each visible seat as a virtual
accessibility node: TalkBack can explore the map, hear each seat's name and selected
state, and toggle seats. No extra setup required.

# Seat Drawer (Optional)

You can create a custom seat drawer, otherwise the default is `NumberSeatDrawer`:

 ```kotlin
class CustomSeatDrawer : SeatDrawer {

    override fun draw(scope: SeatDrawScope) {
        // scope.canvas, scope.seat, scope.seatRect, scope.isSelected,
        // scope.parameters, scope.config, scope.context
    }
}
 ```
Add to SeatView
 ```kotlin
seatView.seatDrawer = CustomSeatDrawer()
 ```

# Extensions (Optional)

You can create your own extensions like drawing center lines or drawing some debug points on SeatView.
`init()` is always called before the first `draw()`.

 ```kotlin
class CustomExtension : SeatViewExtension() {

    override fun isActive(): Boolean {
        return true
    }

    override fun init(params: SeatViewParameters, config: SeatViewConfig) {
    }

    override fun draw(
        canvas: Canvas,
        params: SeatViewParameters,
        config: SeatViewConfig
    ) {
    }

}
  ```
Add to SeatView
 ```kotlin
    seatView.extensions.add(DebugExtension())
    seatView.extensions.add(CenterLinesExtension())
 ```

# Support

If you like the project please give it a star.

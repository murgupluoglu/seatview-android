<img src="github/sample.gif" width="300" height="666"/>

# Installation

[![](https://jitpack.io/v/murgupluoglu/seatview-android.svg)](https://jitpack.io/#murgupluoglu/seatview-android)

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}

dependencies {
    implementation 'com.github.murgupluoglu:seatview-android:lastVersion'
}
```

# Usage

```xml
    <com.murgupluoglu.seatview.SeatView
    android:id="@+id/seatView"
    android:layout_width="0dp"
    android:layout_height="0dp" />
```

```kotlin
   seatView.seatViewListener = object : SeatViewListener<BasicSeat> {
    override fun seatReleased(releasedSeat: BasicSeat, selectedSeats: HashSet<String>) {
    }

    override fun seatSelected(selectedSeat: BasicSeat, selectedSeats: HashSet<String>) {
    }

    override fun canSelectSeat(
        clickedSeat: BasicSeat,
        selectedSeats: HashSet<String>
    ): Boolean {
        return true
    }
   }
    
    seatView.initSeatView(seatList)
```
# Seat Drawer (Optional)

You can create a custom seat drawer otherwise the default is NumberSeatDrawer

 ```kotlin
class CustomSeatDrawer : SeatDrawer {

    override fun <SEAT : Seat> draw(
        context: Context,
        params: SeatViewParameters,
        config: SeatViewConfig,
        canvas: Canvas,
        seat: SEAT,
        seatRectF: RectF,
        isSelected: Boolean
    ) {
    }
}
 ```
Add to SeatView
 ```kotlin
seatView.seatDrawer = CustomSeatDrawer()
 ```

# Extensions (Optional)

You can create your own extensions like drawing center lines or drawing some debug points on SeatView

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
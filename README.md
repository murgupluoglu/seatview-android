![](github/sample.gif)

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
        android:layout_height="0dp"
        android:layout_margin="20dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:seatViewBackgroundColor="#F4F4F4" />
```

```kotlin
    seatView.seatViewListener = object : SeatViewListener {

        override fun seatSelected(selectedSeat: Seat, selectedSeats: HashMap<String, Seat>) {
            Toast.makeText(this@NumbersActivity, "Selected->" + selectedSeat.seatName, Toast.LENGTH_SHORT).show()
        }

        override fun seatReleased(releasedSeat: Seat, selectedSeats: HashMap<String, Seat>) {
            Toast.makeText(this@NumbersActivity, "Released->" + releasedSeat.seatName, Toast.LENGTH_SHORT).show()
        }

        override fun canSelectSeat(clickedSeat: Seat, selectedSeats: HashMap<String, Seat>): Boolean {
            return clickedSeat.type != Seat.TYPE.UNSELECTABLE
        }

    }
    
    seatView.initSeatView(seatArray, rowCount, columnCount)
```
# Seat Drawer (Optional)

 You can create custom seat drawer otherwise default is CachedSeatDrawer
 
 ```kotlin
 class CustomSeatDrawer : SeatDrawer() {

    override fun draw(seatView: SeatView,
                      canvas: Canvas,
                      isInEditMode: Boolean,
                      seatBean: Seat,
                      seatRectF: RectF,
                      seatWidth: Float,
                      seatHeight: Float
    ) {

    }
}
 ```
Add to SeatView
 ```kotlin
seatView.seatDrawer = CustomSeatDrawer()
 ```
 
# Extensions (Optional)

You can create your own extensions like draw center lines or draw some debug points on SeatView
 ```kotlin
 class CustomExtension : SeatViewExtension() {

    override fun isActive(): Boolean {
        return true
    }

    override fun init(seatView: SeatView) {
        
    }

    override fun draw(seatView: SeatView, canvas: Canvas) {
    
    }

}
  ```
Add to SeatView
 ```kotlin
    seatView.extensions.add(DebugExtension())
    seatView.extensions.add(CenterLinesExtension())
 ```

# Support

If you like project please give a star.
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
    <com.murgupluoglu.seatview.SeatView xmlns:seatview="http://schemas.android.com/apk/res-auto"
        android:id="@+id/seatView"
        android:layout_width="0dp"
        android:layout_height="300dp"
        android:layout_margin="0dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        seatview:centerLineActive="true"
        seatview:centerLineColor="#e600ff"
        seatview:centerLineWidth="2.8"
        seatview:cinemaScreenViewActive="true"
        seatview:cinemaScreenViewBackgroundColor="#F44336"
        seatview:cinemaScreenViewSide="top"
        seatview:cinemaScreenViewText="Cinema Screen"
        seatview:cinemaScreenViewTextColor="#ffffff"
        seatview:seatNamesBarActive="true"
        seatview:seatViewBackgroundColor="#F4F4F4"
        seatview:thumbSeatViewActive="false"
        seatview:thumbSeatViewBackgroundColor="#bcb295"
        seatview:thumbSeatViewPointerColor="#ffee00"
        seatview:thumbSeatViewPointerWidth="5"
        seatview:zoomActive="true"
        seatview:zoomAfterClickActive="false" />
```

```kotlin
    seatView.initSeatView(seatArray, rowCount, columnCount, rowNames)

    seatView.seatClickListener = object : SeatViewListener {

        override fun seatReleased(releasedSeat: Seat, selectedSeats: HashMap<String, Seat>) {
            Toast.makeText(this@MainActivity, "Released->" + releasedSeat.seatName, Toast.LENGTH_SHORT).show()
        }

        override fun seatSelected(selectedSeat: Seat, selectedSeats: HashMap<String, Seat>) {
            Toast.makeText(this@MainActivity, "Selected->" + selectedSeat.seatName, Toast.LENGTH_SHORT).show()
        }

        override fun canSelectSeat(clickedSeat: Seat, selectedSeats: HashMap<String, Seat>): Boolean {
            return clickedSeat.type != Seat.TYPE.UNSELECTABLE
        }
    }
```

# Support

<a href="https://www.buymeacoffee.com/murgupluoglu" target="_blank"><img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Coffee" style="height: auto !important;width: auto !important;" ></a>

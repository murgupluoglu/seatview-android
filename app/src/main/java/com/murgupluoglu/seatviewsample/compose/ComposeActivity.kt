package com.murgupluoglu.seatviewsample.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.blankj.utilcode.util.LogUtils
import com.murgupluoglu.seatview.Seat
import com.murgupluoglu.seatview.SeatViewListener
import com.murgupluoglu.seatview.compose.SeatView
import com.murgupluoglu.seatview.compose.SeatViewTheme
import com.murgupluoglu.seatviewsample.json.JsonSampleActivity
import com.murgupluoglu.seatviewsample.utils.loadJSONFromAsset
import org.json.JSONArray
import org.json.JSONObject
import java.util.HashMap

class ComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rowNames: HashMap<String, String> = HashMap()

        val sample = JSONObject(loadJSONFromAsset("sample.json"))
        val rowCount = sample.getJSONObject("screen").getInt("totalRow")
        val columnCount = sample.getJSONObject("screen").getInt("totalColumn")
        val seatArray = Array(rowCount) { Array(columnCount) { Seat() } }
        val rowArray = sample.getJSONObject("screen").getJSONArray("rows")

        val seats = loadSample(seatArray, rowNames, rowArray, rowCount, columnCount)

        setContent {
            SeatViewTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black) {
                    SeatView(
                        modifier = Modifier.fillMaxSize(),
                        seatArray = seats,
                        rowCount = rowCount,
                        columnCount = columnCount,
                        clickListener = object : SeatViewListener{
                        override fun seatReleased(
                            releasedSeat: Seat,
                            selectedSeats: HashMap<String, Seat>
                        ) {
                            LogUtils.d("seatReleased", releasedSeat)
                        }

                        override fun seatSelected(
                            selectedSeat: Seat,
                            selectedSeats: HashMap<String, Seat>
                        ) {
                            LogUtils.d("seatSelected", selectedSeat)
                        }

                        override fun canSelectSeat(
                            clickedSeat: Seat,
                            selectedSeats: HashMap<String, Seat>
                        ): Boolean {
                            LogUtils.d("canSelectSeat", clickedSeat)

                            return true
                        }

                    })
                }
            }
        }
    }

    private fun loadSample(
        seatArray: Array<Array<Seat>>,
        rowNames: HashMap<String, String>,
        rowArray: JSONArray,
        rowCount: Int,
        columnCount: Int
    ): Array<Array<Seat>> {

        val reverseSeats = true

        for (index in 0 until rowArray.length()) {

            val oneRow = rowArray.getJSONObject(index)

            val rowName = oneRow.getString("rowName")
            var rowIndex = oneRow.getInt("rowIndex")
            val seats = oneRow.getJSONArray("seats")

            if (reverseSeats) {
                rowIndex = (rowCount - 1) - rowIndex
            }
            rowNames[rowIndex.toString()] = rowName

            for (columnIndex in 0 until seats.length()) {
                val seatObject = seats.getJSONObject(columnIndex)

                var rowIndexObject = seatObject.getInt("rowIndex")
                val columnIndexObject = seatObject.getInt("columnIndex")
                val seatNameObject = seatObject.getString("name")
                val seatType = seatObject.getString("type")
                val seatIsSelected = seatObject.getBoolean("isSelected")


                if (reverseSeats) {
                    rowIndexObject = (rowCount - 1) - rowIndexObject
                }

                val seat = Seat()
                seat.id = seatNameObject
                seat.seatName = seatNameObject
                seat.rowIndex = rowIndexObject
                seat.columnIndex = columnIndexObject

                seat.rowName = rowName
                //seat.drawableColor = "#4fc3f7"
                //seat.selectedDrawableColor = "#c700ff"
                seat.isSelected = seatIsSelected


                if (seatObject.has("multiple")) { //check multiple seats exist
                    val multipleSeatsArray = seatObject.getJSONArray("multiple")
                    for (multipleSeatsIndex in 0 until multipleSeatsArray.length()) {
                        val oneSeatIdMultiple = multipleSeatsArray.getString(multipleSeatsIndex)

                        if (oneSeatIdMultiple == seat.seatName) {
                            if (multipleSeatsIndex == 0) {
                                seat.multipleType = Seat.MULTIPLETYPE.LEFT
                                seat.drawableResourceName =
                                    if (seatType == "available") "seat_available_multiple_left" else "seat_notavailable_multiple_left"
                                seat.selectedDrawableResourceName = "seat_selected_multiple_left"
                            } else if (multipleSeatsIndex == (multipleSeatsArray.length() - 1)) {
                                seat.multipleType = Seat.MULTIPLETYPE.RIGHT
                                seat.drawableResourceName =
                                    if (seatType == "available") "seat_available_multiple_right" else "seat_notavailable_multiple_right"
                                seat.selectedDrawableResourceName = "seat_selected_multiple_right"
                            } else {
                                seat.multipleType = Seat.MULTIPLETYPE.CENTER
                                seat.drawableResourceName =
                                    if (seatType == "available") "seat_available_multiple_center" else "seat_notavailable_multiple_center"
                                seat.selectedDrawableResourceName = "seat_selected_multiple_center"
                            }
                            when (seatType) {
                                "available" -> {
                                    seat.type = Seat.TYPE.SELECTABLE
                                }
                                "notavailable" -> {
                                    seat.type = Seat.TYPE.UNSELECTABLE
                                }
                            }
                        }
                        seat.multipleSeats.add(oneSeatIdMultiple)
                    }
                }

                if (seat.multipleType == Seat.MULTIPLETYPE.NOTMULTIPLE) {
                    seat.selectedDrawableResourceName = "seat_selected"
                    when (seatType) {
                        "available" -> {
                            seat.drawableResourceName = "seat_available"
                            seat.type = Seat.TYPE.SELECTABLE
                        }
                        "disabled" -> {
                            seat.drawableResourceName = "seat_disabledperson"
                            seat.type = JsonSampleActivity.MY_TYPES.DISABLED_PERSON
                            seat.selectedDrawableResourceName = "ic_android_24dp"
                        }
                        "notavailable" -> {
                            seat.drawableResourceName = "seat_notavailable"
                            seat.type = Seat.TYPE.UNSELECTABLE
                            seat.selectedDrawableResourceName = "ic_android_24dp"
                        }
                    }
                }

                seatArray[rowIndexObject][columnIndexObject] = seat
            }

        }


        return seatArray
    }
}
package com.murgupluoglu.seatviewsample.number

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.murgupluoglu.seatview.Seat
import com.murgupluoglu.seatview.SeatViewListener
import com.murgupluoglu.seatview.extensions.CenterLinesExtension
import com.murgupluoglu.seatview.extensions.DebugExtension
import com.murgupluoglu.seatviewsample.R
import kotlinx.android.synthetic.main.activity_base.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/*
*  Created by Mustafa Ürgüplüoğlu on 26.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class NumbersActivity : AppCompatActivity() {


    private val DISABLED_PERSON = 10


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        seatView.extensions.add(DebugExtension())
        seatView.extensions.add(CenterLinesExtension())

        seatView.seatDrawer = NumberSeatDrawer()

        seatView.seatViewListener = object : SeatViewListener {

            override fun seatSelected(selectedSeat: Seat, selectedSeats: HashMap<String, Seat>) {
                Toast.makeText(
                    this@NumbersActivity,
                    "Selected->" + selectedSeat.seatName,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun seatReleased(releasedSeat: Seat, selectedSeats: HashMap<String, Seat>) {
                Toast.makeText(
                    this@NumbersActivity,
                    "Released->" + releasedSeat.seatName,
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun canSelectSeat(
                clickedSeat: Seat,
                selectedSeats: HashMap<String, Seat>
            ): Boolean {
                return clickedSeat.type != Seat.TYPE.UNSELECTABLE
            }

        }

        defaultSample()
    }

    private fun defaultSample() {
        val rowNames: HashMap<String, String> = HashMap()

        val sample = JSONObject(loadJSONFromAsset())
        val rowCount = sample.getJSONObject("screen").getInt("totalRow")
        val columnCount = sample.getJSONObject("screen").getInt("totalColumn")
        val seatArray = Array(rowCount) { Array(columnCount) { Seat() } }
        val rowArray = sample.getJSONObject("screen").getJSONArray("rows")


        seatView.initSeatView(
            loadSample(seatArray, rowNames, rowArray, rowCount, columnCount),
            rowCount,
            columnCount
        )
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
                seat.isSelected = seatIsSelected


                if (seatObject.has("multiple")) { //check multiple seats exist
                    val multipleSeatsArray = seatObject.getJSONArray("multiple")
                    for (multipleSeatsIndex in 0 until multipleSeatsArray.length()) {
                        val oneSeatIdMultiple = multipleSeatsArray.getString(multipleSeatsIndex)

                        if (oneSeatIdMultiple == seat.seatName) {
                            when (multipleSeatsIndex) {
                                0 -> {
                                    seat.multipleType = Seat.MULTIPLETYPE.LEFT
                                    seat.drawableResourceName =
                                        if (seatType == "available") "seat_available_multiple_left" else "seat_notavailable_multiple_left"
                                    seat.selectedDrawableResourceName =
                                        "seat_selected_multiple_left"
                                }
                                (multipleSeatsArray.length() - 1) -> {
                                    seat.multipleType = Seat.MULTIPLETYPE.RIGHT
                                    seat.drawableResourceName =
                                        if (seatType == "available") "seat_available_multiple_right" else "seat_notavailable_multiple_right"
                                    seat.selectedDrawableResourceName =
                                        "seat_selected_multiple_right"
                                }
                                else -> {
                                    seat.multipleType = Seat.MULTIPLETYPE.CENTER
                                    seat.drawableResourceName =
                                        if (seatType == "available") "seat_available_multiple_center" else "seat_notavailable_multiple_center"
                                    seat.selectedDrawableResourceName =
                                        "seat_selected_multiple_center"
                                }
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
                            seat.type = DISABLED_PERSON
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

    private fun loadJSONFromAsset(): String {
        val fileName = "sample.json"
        val jsonString = assets.open(fileName).bufferedReader().use {
            it.readText()
        }
        return jsonString
    }

}
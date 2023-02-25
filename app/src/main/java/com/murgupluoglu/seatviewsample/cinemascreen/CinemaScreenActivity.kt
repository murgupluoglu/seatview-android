package com.murgupluoglu.seatviewsample.cinemascreen

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.murgupluoglu.seatview.SeatView
import com.murgupluoglu.seatview.SeatViewListener
import com.murgupluoglu.seatview.extensions.CenterLinesExtension
import com.murgupluoglu.seatview.extensions.DebugExtension
import com.murgupluoglu.seatviewsample.R
import org.json.JSONArray
import org.json.JSONObject

/*
*  Created by Mustafa Ürgüplüoğlu on 26.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class CinemaScreenActivity : AppCompatActivity() {


    private lateinit var seatView: SeatView<MultipleSeat>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        seatView = findViewById(R.id.seatView)

        seatView.extensions.apply {
            add(DebugExtension())
            add(CenterLinesExtension())
            add(CinemaScreenExtension())
        }

        seatView.seatDrawer = CachedMultipleSeatDrawer()

        seatView.seatViewListener = object : SeatViewListener<MultipleSeat> {
            override fun seatReleased(releasedSeat: MultipleSeat, selectedSeats: HashSet<String>) {
                Toast.makeText(
                    this@CinemaScreenActivity,
                    "Released->" + releasedSeat.id(),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun seatSelected(selectedSeat: MultipleSeat, selectedSeats: HashSet<String>) {
                Toast.makeText(
                    this@CinemaScreenActivity,
                    "Selected->" + selectedSeat.id(),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun canSelectSeat(
                clickedSeat: MultipleSeat,
                selectedSeats: HashSet<String>
            ): Boolean {
                return clickedSeat.canSelect()
            }

        }

        defaultSample()
    }

    private fun defaultSample() {
        val rowNames: HashMap<String, String> = HashMap()

        val sample = JSONObject(loadJSONFromAsset())
        val rowCount = sample.getJSONObject("screen").getInt("totalRow")
        val columnCount = sample.getJSONObject("screen").getInt("totalColumn")
        val seatArray = Array(rowCount) { Array(columnCount) { MultipleSeat() } }
        val rowArray = sample.getJSONObject("screen").getJSONArray("rows")


        seatView.initSeatView(
            loadSample(seatArray, rowNames, rowArray, rowCount)
        )
    }

    private fun loadSample(
        seatArray: Array<Array<MultipleSeat>>,
        rowNames: HashMap<String, String>,
        rowArray: JSONArray,
        rowCount: Int
    ): Array<Array<MultipleSeat>> {

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

                val seat = MultipleSeat()
                seat.seatId = seatNameObject
                seat.seatName = seatNameObject
                seat.isPreSelectedSeat = seatIsSelected

                if (seatObject.has("multiple")) { //check multiple seats exist
                    val multipleSeatsArray = seatObject.getJSONArray("multiple")
                    for (multipleSeatsIndex in 0 until multipleSeatsArray.length()) {
                        val oneSeatIdMultiple = multipleSeatsArray.getString(multipleSeatsIndex)

                        if (oneSeatIdMultiple == seat.seatName) {
                            when (multipleSeatsIndex) {
                                0 -> {
                                    seat.multipleType = MultipleSeat.MULTIPLETYPE.LEFT
                                }

                                (multipleSeatsArray.length() - 1) -> {
                                    seat.multipleType = MultipleSeat.MULTIPLETYPE.RIGHT
                                }

                                else -> {
                                    seat.multipleType = MultipleSeat.MULTIPLETYPE.CENTER
                                }
                            }
                            when (seatType) {
                                "available" -> {
                                    seat.type = MultipleSeat.TYPE.SELECTABLE
                                }

                                "notavailable" -> {
                                    seat.type = MultipleSeat.TYPE.UNSELECTABLE
                                }
                            }
                        }
                        seat.multipleSeats.add(oneSeatIdMultiple)
                    }
                }

                if (seat.multipleType == MultipleSeat.MULTIPLETYPE.NOTMULTIPLE) {
                    when (seatType) {
                        "available" -> {
                            seat.type = MultipleSeat.TYPE.SELECTABLE
                        }

                        "disabled" -> {
                            seat.type = MultipleSeat.TYPE.DISABLED_PERSON
                        }

                        "notavailable" -> {
                            seat.type = MultipleSeat.TYPE.UNSELECTABLE
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
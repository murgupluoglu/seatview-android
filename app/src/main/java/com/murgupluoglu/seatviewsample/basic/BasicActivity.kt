package com.murgupluoglu.seatviewsample.basic

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.murgupluoglu.seatview.SeatView
import com.murgupluoglu.seatview.SeatViewListener
import com.murgupluoglu.seatviewsample.R

class BasicActivity : AppCompatActivity() {

    private lateinit var seatView: SeatView<BasicSeat>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        seatView = findViewById(R.id.seatView)

        seatView.seatViewListener = object : SeatViewListener<BasicSeat> {
            override fun seatReleased(releasedSeat: BasicSeat, selectedSeats: HashSet<String>) {
                Toast.makeText(
                    this@BasicActivity,
                    "Released->" + releasedSeat.id(),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun seatSelected(selectedSeat: BasicSeat, selectedSeats: HashSet<String>) {
                Toast.makeText(
                    this@BasicActivity,
                    "Selected->" + selectedSeat.id(),
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun canSelectSeat(
                clickedSeat: BasicSeat,
                selectedSeats: HashSet<String>
            ): Boolean {
                return clickedSeat.canSelect()
            }

        }

        val list = Array(10) { y ->
            Array(10) { x ->
                BasicSeat("$x-$y")
            }
        }

        seatView.initSeatView(list)
    }
}
package com.murgupluoglu.seatview

import java.util.*

interface SeatViewListener {

    fun seatReleased(releasedSeat: Seat, selectedSeats: HashMap<String, Seat>)

    fun seatSelected(selectedSeat: Seat, selectedSeats: HashMap<String, Seat>)

    fun canSelectSeat(clickedSeat: Seat, selectedSeats: HashMap<String, Seat>): Boolean

}
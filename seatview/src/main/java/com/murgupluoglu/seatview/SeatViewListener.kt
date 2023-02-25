package com.murgupluoglu.seatview

interface SeatViewListener<SEAT> {

    fun seatReleased(releasedSeat: SEAT, selectedSeats: HashSet<String>)

    fun seatSelected(selectedSeat: SEAT, selectedSeats: HashSet<String>)

    fun canSelectSeat(clickedSeat: SEAT, selectedSeats: HashSet<String>): Boolean

}
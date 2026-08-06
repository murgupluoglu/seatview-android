package com.murgupluoglu.seatviewsample.basic

import com.murgupluoglu.seatview.Seat

/**
 * A plain seat: only [id] and [name] need implementing — visibility, selectability,
 * connected seats and rect additions all use the [Seat] interface defaults.
 */
class BasicSeat(
    var id: String
) : Seat {
    override fun id(): String = id

    override fun name(): String = id
}
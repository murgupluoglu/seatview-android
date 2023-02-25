package com.murgupluoglu.seatviewsample.basic

import com.murgupluoglu.seatview.Seat

class BasicSeat(
    var id: String
) : Seat {
    override fun id(): String {
        return id
    }

    override fun name(): String {
        return id
    }

    override fun isVisible(): Boolean {
        return true
    }

    override fun allConnectedSeatIds(): Array<String> {
        return emptyArray()
    }

    override fun rightRectAddition(seatInlineGap: Float): Float {
        return 0f
    }

    override fun leftRectAddition(seatInlineGap: Float): Float {
        return 0f
    }

    override fun canSelect(): Boolean {
        return true
    }

    override fun isPreSelected(): Boolean {
        return false
    }
}
package com.murgupluoglu.seatview


interface Seat {

    /**
     * must be unique
     */
    fun id(): String

    fun name(): String

    fun isVisible(): Boolean

    fun allConnectedSeatIds(): Array<String>

    fun rightRectAddition(seatInlineGap: Float): Float

    fun leftRectAddition(seatInlineGap: Float): Float

    fun canSelect(): Boolean

    fun isPreSelected(): Boolean
}


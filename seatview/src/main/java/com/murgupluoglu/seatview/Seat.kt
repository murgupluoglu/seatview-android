package com.murgupluoglu.seatview


interface Seat {

    /**
     * Must be unique; it is the key used in the selection set.
     */
    fun id(): String

    /** Display label rendered by the [com.murgupluoglu.seatview.seatdrawer.SeatDrawer]. */
    fun name(): String

    /** Invisible seats model gaps in the grid: skipped in drawing and hit-testing. */
    fun isVisible(): Boolean = true

    /**
     * Ids that select/release atomically together with this seat (e.g. a couch sold as a unit).
     * The value may or may not include this seat's own id — [SeatView] tolerates both.
     * Defaults to no connected seats.
     */
    fun allConnectedSeatIds(): Array<String> = emptyArray()

    /** Horizontal adjustment applied to a seat's right edge to visually merge multi-seats. */
    fun rightRectAddition(seatInlineGap: Float): Float = 0f

    /** Horizontal adjustment applied to a seat's left edge to visually merge multi-seats. */
    fun leftRectAddition(seatInlineGap: Float): Float = 0f

    /**
     * Static, model-level eligibility: walls/aisles/sold seats return false.
     * Enforced by [SeatView.selectSeat]. For dynamic, interaction-time policy
     * (e.g. "max 6 seats") use [SeatViewListener.canSelectSeat] instead.
     */
    fun canSelect(): Boolean = true

    /** Whether this seat starts selected when the grid is initialized. */
    fun isPreSelected(): Boolean = false
}


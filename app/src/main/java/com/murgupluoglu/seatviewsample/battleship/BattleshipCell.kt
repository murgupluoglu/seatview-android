package com.murgupluoglu.seatviewsample.battleship

import com.murgupluoglu.seatview.Seat

/**
 * One board square, exposed to [com.murgupluoglu.seatview.SeatView] as a [Seat].
 *
 * Note how little the [Seat] contract asks for: only [id] and [name] are mandatory, and
 * [canSelect] is the single hook that stops an already-fired square from being fired again —
 * SeatView enforces it, so the game never has to guard the tap itself.
 */
class BattleshipCell(
    val row: Int,
    val column: Int,
) : Seat {

    enum class State { UNKNOWN, MISS, HIT, SUNK, REVEALED }

    var state: State = State.UNKNOWN
    var shipIndex: Int = BattleshipGame.NO_SHIP

    /** Grid reference such as "A1" — also what TalkBack announces for this square. */
    private val label: String = "${'A' + row}${column + 1}"

    override fun id(): String = "$column-$row"

    override fun name(): String = label

    /** Only an untouched square can be fired at. */
    override fun canSelect(): Boolean = state == State.UNKNOWN
}

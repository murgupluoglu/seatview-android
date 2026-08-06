package com.murgupluoglu.seatview

import android.view.View
import org.robolectric.RuntimeEnvironment

/**
 * Shared fakes and helpers for the Robolectric unit tests.
 */

/** A minimal, fully configurable [Seat] used across the unit tests. */
class TestSeat(
    private val seatId: String,
    private val visible: Boolean = true,
    private val selectable: Boolean = true,
    private val preSelected: Boolean = false,
    /** Extra ids that select/release atomically together with this seat (multi-seat grouping). */
    private val connectedIds: Array<String> = emptyArray(),
) : Seat {
    override fun id(): String = seatId
    override fun name(): String = seatId
    override fun isVisible(): Boolean = visible
    override fun allConnectedSeatIds(): Array<String> = connectedIds
    /**
     * Absolute px added to this seat's right edge, so a test can model a merged seat that spans
     * several cell pitches. Settable after `initSeatView`, once the real seat width is known.
     */
    var rightAdditionPx: Float = 0f

    override fun rightRectAddition(seatInlineGap: Float): Float = rightAdditionPx
    override fun leftRectAddition(seatInlineGap: Float): Float = 0f
    override fun canSelect(): Boolean = selectable
    override fun isPreSelected(): Boolean = preSelected
}

/** Records every callback so tests can assert on what the view emitted. */
class RecordingListener : SeatViewListener<TestSeat> {
    val selected = mutableListOf<TestSeat>()
    val released = mutableListOf<TestSeat>()
    var canSelectResult = true
    var canSelectQueries = 0

    override fun seatSelected(selectedSeat: TestSeat, selectedSeats: Set<String>) {
        selected.add(selectedSeat)
    }

    override fun seatReleased(releasedSeat: TestSeat, selectedSeats: Set<String>) {
        released.add(releasedSeat)
    }

    override fun canSelectSeat(clickedSeat: TestSeat, selectedSeats: Set<String>): Boolean {
        canSelectQueries++
        return canSelectResult
    }

    /** Every restore delivery, so tests can assert it fires exactly once with the full set. */
    val restored = mutableListOf<Set<String>>()

    override fun selectionRestored(selectedSeats: Set<String>) {
        restored.add(selectedSeats)
    }
}

/** Fresh [SeatView] backed by the Robolectric application context. */
fun newSeatView(): SeatView<TestSeat> = SeatView(RuntimeEnvironment.getApplication())

/**
 * Build a `[yIndex][xIndex]` grid: outer array = rows (ySize), inner array = columns (xSize).
 * [factory] receives (x, y).
 */
fun buildGrid(
    cols: Int,
    rows: Int,
    factory: (x: Int, y: Int) -> TestSeat = { x, y -> TestSeat("$x-$y") },
): Array<Array<TestSeat>> = Array(rows) { y -> Array(cols) { x -> factory(x, y) } }

/** Give the view real bounds so onSizeChanged() / initParameters() run. */
fun View.layoutForTest(w: Int = 1080, h: Int = 1920) {
    measure(
        View.MeasureSpec.makeMeasureSpec(w, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(h, View.MeasureSpec.EXACTLY),
    )
    layout(0, 0, w, h)
}

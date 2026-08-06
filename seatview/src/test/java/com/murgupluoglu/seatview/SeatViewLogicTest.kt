package com.murgupluoglu.seatview

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SeatViewLogicTest {

    private fun viewWith(grid: Array<Array<TestSeat>>): Pair<SeatView<TestSeat>, RecordingListener> {
        val view = newSeatView()
        val listener = RecordingListener()
        view.seatViewListener = listener
        view.initSeatView(grid)
        return view to listener
    }

    // grid ids are "column-row" (buildGrid default factory is "$x-$y")
    private fun at(row: Int, column: Int) = SeatPosition(row = row, column = column)

    // region init & validation

    @Test
    fun initSeatView_setsGridDimensions() {
        val (view, _) = viewWith(buildGrid(cols = 5, rows = 4))

        assertThat(view.columnCount).isEqualTo(5)
        assertThat(view.rowCount).isEqualTo(4)
    }

    @Test(expected = IllegalArgumentException::class)
    fun initSeatView_emptyList_throws() {
        newSeatView().initSeatView(arrayOf())
    }

    @Test(expected = IllegalArgumentException::class)
    fun initSeatView_emptyFirstRow_throws() {
        newSeatView().initSeatView(arrayOf(arrayOf<TestSeat>()))
    }

    @Test(expected = IllegalArgumentException::class)
    fun initSeatView_raggedRows_throws() {
        val ragged = arrayOf(
            arrayOf(TestSeat("0-0"), TestSeat("1-0")),
            arrayOf(TestSeat("0-1")), // shorter row
        )
        newSeatView().initSeatView(ragged)
    }

    @Test
    fun initSeatView_addsPreSelectedSeatsIncludingConnected() {
        val grid = buildGrid(cols = 3, rows = 2) { x, y ->
            if (x == 0 && y == 0) TestSeat("0-0", preSelected = true, connectedIds = arrayOf("0-0", "1-0"))
            else TestSeat("$x-$y")
        }
        val (view, _) = viewWith(grid)

        assertThat(view.selectedSeatIds).containsExactly("0-0", "1-0")
    }

    @Test
    fun initSeatView_clearsPreviousSelectionOnReInit() {
        val view = newSeatView()

        view.initSeatView(buildGrid(cols = 2, rows = 2))
        view.selectSeat(at(0, 0))
        assertThat(view.selectedSeatIds).hasSize(1)

        view.initSeatView(buildGrid(cols = 2, rows = 2))
        assertThat(view.selectedSeatIds).isEmpty()
    }

    // endregion

    // region selection by position

    @Test
    fun selectSeat_byPosition_selectsAndNotifies() {
        val (view, listener) = viewWith(buildGrid(cols = 3, rows = 3))

        val result = view.selectSeat(at(row = 2, column = 1))

        assertThat(result).isNotNull()
        assertThat(result!!.id()).isEqualTo("1-2")
        assertThat(view.isSelected("1-2")).isTrue()
        assertThat(listener.selected.map { it.id() }).containsExactly("1-2")
    }

    @Test
    fun selectSeat_addsAllConnectedIds() {
        val grid = buildGrid(cols = 4, rows = 1) { x, _ ->
            if (x == 0) TestSeat("0-0", connectedIds = arrayOf("1-0")) else TestSeat("$x-0")
        }
        val (view, _) = viewWith(grid)

        view.selectSeat(at(0, 0))

        assertThat(view.selectedSeatIds).containsExactly("0-0", "1-0")
    }

    @Test
    fun selectSeat_alreadySelected_isNoOp() {
        val (view, listener) = viewWith(buildGrid(cols = 2, rows = 2))

        assertThat(view.selectSeat(at(0, 0))).isNotNull()
        assertThat(view.selectSeat(at(0, 0))).isNull()

        assertThat(view.selectedSeatIds).hasSize(1)
        assertThat(listener.selected).hasSize(1)
    }

    @Test
    fun selectSeat_nonSelectableSeat_returnsNullAndDoesNotNotify() {
        val grid = buildGrid(cols = 3, rows = 3) { x, y ->
            TestSeat("$x-$y", selectable = !(x == 0 && y == 0))
        }
        val (view, listener) = viewWith(grid)

        assertThat(view.selectSeat(at(0, 0))).isNull()

        assertThat(view.selectedSeatIds).isEmpty()
        assertThat(listener.selected).isEmpty()
    }

    @Test
    fun selectSeat_outOfBounds_returnsNullAndDoesNotNotify() {
        val (view, listener) = viewWith(buildGrid(cols = 3, rows = 3))

        assertThat(view.selectSeat(at(row = 0, column = 3))).isNull()   // column == columnCount
        assertThat(view.selectSeat(at(row = 3, column = 0))).isNull()   // row == rowCount
        assertThat(view.selectSeat(at(row = 0, column = -1))).isNull()
        assertThat(view.selectSeat(at(row = 99, column = 99))).isNull()

        assertThat(view.selectedSeatIds).isEmpty()
        assertThat(listener.selected).isEmpty()
    }

    // endregion

    // region selection by id + helpers

    @Test
    fun selectSeat_byId_selectsCorrectSeat() {
        val (view, _) = viewWith(buildGrid(cols = 4, rows = 3))

        val seat = view.selectSeat("2-1")

        assertThat(seat?.id()).isEqualTo("2-1")
        assertThat(view.isSelected("2-1")).isTrue()
    }

    @Test
    fun selectSeat_byUnknownId_returnsNull() {
        val (view, _) = viewWith(buildGrid(cols = 2, rows = 2))
        assertThat(view.selectSeat("nope")).isNull()
    }

    @Test
    fun releaseSeat_byId_releases() {
        val (view, _) = viewWith(buildGrid(cols = 3, rows = 3))
        view.selectSeat("1-1")
        assertThat(view.isSelected("1-1")).isTrue()

        assertThat(view.releaseSeat("1-1")).isTrue()
        assertThat(view.isSelected("1-1")).isFalse()
    }

    @Test
    fun clearSelection_removesEverything() {
        val (view, _) = viewWith(buildGrid(cols = 3, rows = 3))
        view.selectSeat("0-0")
        view.selectSeat("1-1")
        assertThat(view.selectedSeatIds).hasSize(2)

        view.clearSelection()

        assertThat(view.selectedSeatIds).isEmpty()
    }

    @Test
    fun selectedSeatIds_isReadOnlySnapshotType() {
        val (view, _) = viewWith(buildGrid(cols = 2, rows = 2))
        val ids: Set<String> = view.selectedSeatIds
        assertThat(ids).isEmpty()
    }

    // endregion

    // region release by position

    @Test
    fun releaseSeat_removesSeatAndConnectedIdsAndNotifies() {
        val grid = buildGrid(cols = 4, rows = 1) { x, _ ->
            if (x == 0) TestSeat("0-0", connectedIds = arrayOf("1-0")) else TestSeat("$x-0")
        }
        val (view, listener) = viewWith(grid)

        view.selectSeat(at(0, 0))
        assertThat(view.selectedSeatIds).hasSize(2)

        assertThat(view.releaseSeat(at(0, 0))).isTrue()

        assertThat(view.selectedSeatIds).isEmpty()
        assertThat(listener.released.map { it.id() }).containsExactly("0-0")
    }

    @Test
    fun releaseSeat_notSelected_isNoOp() {
        val (view, listener) = viewWith(buildGrid(cols = 2, rows = 2))

        assertThat(view.releaseSeat(at(0, 0))).isFalse()

        assertThat(view.selectedSeatIds).isEmpty()
        assertThat(listener.released).isEmpty()
    }

    @Test
    fun releaseSeat_outOfBounds_isNoOp() {
        val (view, _) = viewWith(buildGrid(cols = 2, rows = 2))
        view.selectSeat(at(0, 0))

        assertThat(view.releaseSeat(at(5, 5))).isFalse()

        assertThat(view.selectedSeatIds).hasSize(1)
    }

    // endregion

    // region lookup & bounds

    @Test
    fun positionOf_returnsRowColumnForKnownId() {
        val (view, _) = viewWith(buildGrid(cols = 5, rows = 4))

        assertThat(view.positionOf("2-3")).isEqualTo(SeatPosition(row = 3, column = 2))
        assertThat(view.positionOf("0-0")).isEqualTo(SeatPosition(row = 0, column = 0))
    }

    @Test
    fun positionOf_returnsNullForUnknownId() {
        val (view, _) = viewWith(buildGrid(cols = 3, rows = 3))
        assertThat(view.positionOf("nope")).isNull()
    }

    @Test
    fun seatAt_respectsGridBoundaries() {
        val (view, _) = viewWith(buildGrid(cols = 3, rows = 2)) // columns=3, rows=2

        assertThat(view.seatAt(at(row = 0, column = 0))).isNotNull()
        assertThat(view.seatAt(at(row = 1, column = 2))).isNotNull()  // last valid cell
        assertThat(view.seatAt(at(row = 1, column = 3))).isNull()     // column == columnCount
        assertThat(view.seatAt(at(row = 2, column = 2))).isNull()     // row == rowCount
        assertThat(view.seatAt(at(row = 0, column = -1))).isNull()
    }

    @Test
    fun seatAt_returnsSeatAtRowColumn() {
        val (view, _) = viewWith(buildGrid(cols = 4, rows = 3))

        assertThat(view.seatAt(at(row = 2, column = 3))?.id()).isEqualTo("3-2")
    }

    // endregion
}

package com.murgupluoglu.seatview

import android.graphics.Bitmap
import android.graphics.Canvas
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Regression coverage for the draw loop. The v4 rewrite briefly used inclusive
 * ranges (`0..xSize` / `0..ySize`) in drawSeats(), which walked one cell past the
 * grid and crashed with ArrayIndexOutOfBoundsException on the very first frame.
 * These tests fail loudly if that off-by-one ever comes back.
 */
@RunWith(RobolectricTestRunner::class)
class SeatViewDrawTest {

    private fun draw(view: SeatView<TestSeat>, w: Int = 1080, h: Int = 1920) {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
    }

    @Test
    fun draw_doesNotThrow_whenLaidOutThenInitialised() {
        val view = newSeatView()
        view.layoutForTest()
        view.initSeatView(buildGrid(cols = 12, rows = 20))

        draw(view) // would throw AIOOBE with inclusive draw ranges
    }

    @Test
    fun draw_doesNotThrow_whenInitialisedThenLaidOut() {
        val view = newSeatView()
        view.initSeatView(buildGrid(cols = 12, rows = 20))
        view.layoutForTest() // onSizeChanged builds the layout here

        draw(view)
    }

    @Test
    fun draw_doesNotThrow_forSingleCellGrid() {
        val view = newSeatView()
        view.layoutForTest()
        view.initSeatView(buildGrid(cols = 1, rows = 1))

        draw(view)
    }

    @Test
    fun draw_doesNotThrow_withInvisibleSeats() {
        val view = newSeatView()
        view.layoutForTest()
        view.initSeatView(buildGrid(cols = 6, rows = 6) { x, y ->
            TestSeat("$x-$y", visible = (x + y) % 2 == 0)
        })

        draw(view)
    }

    @Test
    fun draw_beforeInit_doesNotThrow() {
        // rowCount/columnCount default to 0, so nothing is drawn and lateinit `seats` is untouched.
        val view = newSeatView()
        view.layoutForTest()

        draw(view)

        assertThat(view.columnCount).isEqualTo(0)
        assertThat(view.rowCount).isEqualTo(0)
    }
}

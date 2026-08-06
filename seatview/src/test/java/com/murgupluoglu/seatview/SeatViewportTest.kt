package com.murgupluoglu.seatview

import android.graphics.RectF
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Direct unit coverage for the extracted pan/clamp model. Previously this math
 * lived inside SeatView and was only reachable through touch gestures on a device;
 * as a standalone class it can be asserted precisely.
 */
@RunWith(RobolectricTestRunner::class)
class SeatViewportTest {

    private fun params(window: RectF, virtual: RectF) =
        SeatViewParameters(windowRectF = window, virtualRectF = virtual)

    @Test
    fun clamp_centersGridSmallerThanWindow() {
        val p = params(RectF(0f, 0f, 1000f, 1000f), RectF(0f, 0f, 100f, 100f))

        SeatViewport(p).clamp()

        assertThat(p.virtualRectF.left).isWithin(0.01f).of(450f)
        assertThat(p.virtualRectF.top).isWithin(0.01f).of(450f)
        assertThat(p.virtualRectF.right).isWithin(0.01f).of(550f)
        assertThat(p.virtualRectF.bottom).isWithin(0.01f).of(550f)
    }

    @Test
    fun clamp_snapsBackWhenLeftEdgeExposesGap() {
        // Grid wider than window but dragged 50px to the right, leaving a gap on the left.
        val p = params(RectF(0f, 0f, 500f, 500f), RectF(50f, 0f, 1050f, 500f))

        SeatViewport(p).clamp()

        assertThat(p.virtualRectF.left).isWithin(0.01f).of(0f)
        assertThat(p.virtualRectF.right).isAtLeast(500f)
    }

    @Test
    fun pan_movesGridWhenItOverflowsTheWindow() {
        val p = params(RectF(0f, 0f, 500f, 500f), RectF(-250f, -250f, 750f, 750f))

        SeatViewport(p).pan(distanceX = 100f, distanceY = 0f)

        assertThat(p.virtualRectF.left).isWithin(0.01f).of(-350f)
        assertThat(p.virtualRectF.left).isAtMost(0f)
        assertThat(p.virtualRectF.right).isAtLeast(500f)
    }

    @Test
    fun pan_cannotDragGridPastTheEdge() {
        val p = params(RectF(0f, 0f, 500f, 500f), RectF(-10f, -10f, 510f, 510f))

        SeatViewport(p).pan(distanceX = 1000f, distanceY = 0f) // absurd over-drag

        // Invariant: a grid wider than the window may never expose empty space.
        assertThat(p.virtualRectF.left).isAtMost(0.01f)
        assertThat(p.virtualRectF.right).isAtLeast(499.99f)
    }

    @Test
    fun rebuildVirtualRect_centersGridInWindow() {
        val p = SeatViewParameters(
            windowRectF = RectF(0f, 0f, 1000f, 1000f),
            seatWidth = 100f,
            seatHeight = 100f,
            seatInlineGap = 0f,
            seatNewlineGap = 0f,
            xSize = 2,
            ySize = 2,
        )

        SeatViewport(p).rebuildVirtualRect()

        // 2x2 grid of 100px seats = 200x200, centered in a 1000x1000 window.
        assertThat(p.virtualRectF.left).isWithin(0.01f).of(400f)
        assertThat(p.virtualRectF.top).isWithin(0.01f).of(400f)
        assertThat(p.virtualRectF.right).isWithin(0.01f).of(600f)
        assertThat(p.virtualRectF.bottom).isWithin(0.01f).of(600f)
    }
}

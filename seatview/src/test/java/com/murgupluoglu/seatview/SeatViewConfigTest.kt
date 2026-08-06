package com.murgupluoglu.seatview

import android.graphics.Color
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SeatViewConfigTest {

    @Test
    fun config_hasExpectedDefaults() {
        val config = SeatViewConfig()

        assertThat(config.leftPadding).isEqualTo(50f)
        assertThat(config.rightPadding).isEqualTo(50f)
        assertThat(config.topPadding).isEqualTo(50f)
        assertThat(config.bottomPadding).isEqualTo(50f)
        assertThat(config.seatWidthHeightRatio).isEqualTo(1f)
        assertThat(config.backgroundColor).isEqualTo(Color.WHITE)
    }

    @Test
    fun config_seatWidthBoundsAreOrdered() {
        val config = SeatViewConfig()

        assertThat(config.seatMinWidthDp).isAtMost(config.seatDefaultWidthDp)
        assertThat(config.seatDefaultWidthDp).isAtMost(config.seatMaxWidthDp)
    }

    @Test
    fun initSeatView_convertsDpSizesToPixels() {
        val view = newSeatView()
        view.initSeatView(buildGrid(cols = 3, rows = 3))

        val density = view.resources.displayMetrics.density
        val expected = SeatViewConfig().seatDefaultWidthDp * density + 0.5f
        assertThat(view.parameters.seatWidth).isWithin(0.001f).of(expected)
        assertThat(view.parameters.seatWidth).isGreaterThan(0f)
    }

    @Test
    fun parameters_defaultToZero() {
        val params = SeatViewParameters()

        assertThat(params.xSize).isEqualTo(0)
        assertThat(params.ySize).isEqualTo(0)
        assertThat(params.seatWidth).isEqualTo(0f)
        assertThat(params.seatHeight).isEqualTo(0f)
    }

    @Test
    fun customConfigIsApplied() {
        val view = newSeatView()

        view.initSeatView(buildGrid(cols = 3, rows = 3), SeatViewConfig(backgroundColor = Color.BLACK))

        assertThat(view.config.backgroundColor).isEqualTo(Color.BLACK)
    }

    @Test
    fun isZoomEnabled_defaultsToTrue() {
        assertThat(newSeatView().isZoomEnabled).isTrue()
    }

    /** Zoom is a runtime property with a single source of truth; re-init must not resurrect it. */
    @Test
    fun isZoomEnabled_survivesReInitialisation() {
        val view = newSeatView()
        view.initSeatView(buildGrid(cols = 3, rows = 3))

        view.isZoomEnabled = false
        view.initSeatView(buildGrid(cols = 4, rows = 4)) // refresh, no config argument

        assertThat(view.isZoomEnabled).isFalse()
    }
}

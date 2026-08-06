package com.murgupluoglu.seatview

import android.graphics.Color
import androidx.annotation.ColorInt

/*
*  Created by Mustafa Ürgüplüoğlu on 13.03.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

/**
 * Immutable, consumer-tunable inputs for [SeatView].
 *
 * Seat sizes are expressed in **dp** and converted to pixels by the view against its own
 * display density in [SeatView.initSeatView] — the config no longer reaches into the
 * global `Resources.getSystem()` at construction time.
 */
data class SeatViewConfig(
    val leftPadding: Float = 50f,
    val rightPadding: Float = 50f,
    val topPadding: Float = 50f,
    val bottomPadding: Float = 50f,

    val seatMinWidthDp: Float = 12f,
    val seatMaxWidthDp: Float = 30f,
    val seatDefaultWidthDp: Float = 13f,
    val seatWidthHeightRatio: Float = 1f,
    val seatInlineGapWidthRatio: Float = 0.265f,
    val seatNewlineGapWidthRatio: Float = 0.304f,

    @param:ColorInt
    val backgroundColor: Int = Color.WHITE,
)

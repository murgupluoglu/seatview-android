package com.murgupluoglu.seatview

import android.graphics.RectF

/**
 * Runtime-computed layout state derived from [SeatViewConfig]. Seat sizes here are in
 * **pixels** (converted from the config's dp values); the two rectangles drive pan/zoom.
 */
data class SeatViewParameters(
    var windowRectF: RectF = RectF(),
    var virtualRectF: RectF = RectF(),
    var seatWidth: Float = 0f,
    var seatHeight: Float = 0f,
    var seatInlineGap: Float = 0f,
    var seatNewlineGap: Float = 0f,
    var seatMinWidth: Float = 0f,
    var seatMaxWidth: Float = 0f,
    var xSize: Int = 0,
    var ySize: Int = 0,
)

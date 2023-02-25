package com.murgupluoglu.seatview

import android.graphics.RectF

data class SeatViewParameters(
    var windowRectF: RectF = RectF(),
    var virtualRectF: RectF = RectF(),
    var seatDefaultHeight: Float = 0f,
    var seatWidth: Float = 0f,
    var seatHeight: Float = 0f,
    var seatInlineGap: Float = 0f,
    var seatNewlineGap: Float = 0f,
    var xSize: Int = 0,
    var ySize: Int = 0,
    var scaleFactorStart: Float = 0f,
    var scaleFactor: Float = 0f,
)
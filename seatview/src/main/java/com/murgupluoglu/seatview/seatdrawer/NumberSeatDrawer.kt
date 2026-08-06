package com.murgupluoglu.seatview.seatdrawer

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect


/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class NumberSeatDrawer : SeatDrawer {

    private val commonPaint = Paint().apply {
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private val textBounds = Rect()

    override fun draw(scope: SeatDrawScope) {
        val seat = scope.seat
        val color: Int = when {
            scope.isSelected -> Color.GREEN
            !seat.canSelect() -> Color.RED
            else -> Color.GRAY
        }
        commonPaint.color = color

        //Draw rectangle
        commonPaint.style = Paint.Style.FILL_AND_STROKE
        scope.canvas.drawRect(scope.seatRect, commonPaint)

        //Draw text
        textPaint.getTextBounds(seat.name(), 0, seat.name().length, textBounds)
        val yOffset = textBounds.height() / 2
        textPaint.textSize = scope.parameters.seatWidth * 0.5f
        scope.canvas.drawText(
            seat.name(),
            scope.seatRect.centerX(),
            scope.seatRect.centerY() + yOffset,
            textPaint
        )
    }
}

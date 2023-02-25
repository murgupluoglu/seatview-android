package com.murgupluoglu.seatview.seatdrawer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import com.murgupluoglu.seatview.Seat
import com.murgupluoglu.seatview.SeatViewConfig
import com.murgupluoglu.seatview.SeatViewParameters


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

    override fun <SEAT : Seat> draw(
        context: Context,
        params: SeatViewParameters,
        config: SeatViewConfig,
        canvas: Canvas,
        seat: SEAT,
        seatRectF: RectF,
        isSelected: Boolean
    ) {
        val color: Int = when {
            isSelected -> {
                Color.GREEN
            }

            !seat.canSelect() -> {
                Color.RED
            }

            else -> {
                Color.GRAY
            }
        }
        commonPaint.color = color

        //Draw circle
        //val calculatedSize = seatWidth * 0.5f
        //canvas.drawCircle(seatRectF.centerX(), seatRectF.centerY(), calculatedSize, commonPaint)

        //Draw rectangle
        commonPaint.style = Paint.Style.FILL_AND_STROKE
        canvas.drawRect(seatRectF, commonPaint)

        //Draw text
        val result = Rect()
        textPaint.getTextBounds(seat.name(), 0, seat.name().length, result)
        val yOffset = result.height() / 2
        textPaint.textSize = params.seatWidth * 0.5f
        canvas.drawText(
            seat.name(),
            seatRectF.centerX(),
            seatRectF.centerY() + yOffset,
            textPaint
        )
    }
}
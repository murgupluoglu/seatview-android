package com.murgupluoglu.seatview.extensions

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.murgupluoglu.seatview.SeatView

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class DebugExtension : SeatViewExtension() {

    private val debugPaint = Paint()

    override fun isActive(): Boolean {
        return true
    }

    override fun init(seatView: SeatView) {

    }

    override fun draw(seatView: SeatView, canvas: Canvas) {

        debugPaint.apply {
            color = Color.BLACK
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(seatView.windowRectF, debugPaint)

        debugPaint.apply {
            color = Color.RED
        }
        canvas.drawRect(seatView.virtualRectF, debugPaint)

        debugPaint.apply {
            color = Color.BLUE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(seatView.virtualRectF.left, seatView.virtualRectF.top, 20f, debugPaint)
        canvas.drawCircle(
            seatView.virtualRectF.right,
            seatView.virtualRectF.bottom,
            20f,
            debugPaint
        )

        debugPaint.apply {
            color = Color.BLACK
            style = Paint.Style.FILL
        }
        canvas.drawLine(
            seatView.windowRectF.centerX(),
            seatView.windowRectF.centerY(),
            seatView.windowRectF.centerX(),
            seatView.windowRectF.bottom,
            debugPaint
        )

    }

}
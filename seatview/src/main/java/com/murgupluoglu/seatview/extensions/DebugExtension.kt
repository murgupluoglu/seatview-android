package com.murgupluoglu.seatview.extensions

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.murgupluoglu.seatview.SeatViewConfig
import com.murgupluoglu.seatview.SeatViewParameters

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class DebugExtension : SeatViewExtension() {

    private val debugPaint = Paint()

    override fun isActive(): Boolean {
        return true
    }

    override fun init(params: SeatViewParameters, config: SeatViewConfig) {

    }

    override fun draw(
        canvas: Canvas,
        params: SeatViewParameters,
        config: SeatViewConfig
    ) {
        params.apply {
            debugPaint.apply {
                color = Color.BLACK
                strokeWidth = 3f
                style = Paint.Style.STROKE
            }
            canvas.drawRect(windowRectF, debugPaint)

            debugPaint.apply {
                color = Color.RED
            }
            canvas.drawRect(virtualRectF, debugPaint)

            debugPaint.apply {
                color = Color.BLUE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(
                virtualRectF.left,
                virtualRectF.top,
                20f,
                debugPaint
            )
            canvas.drawCircle(
                virtualRectF.right,
                virtualRectF.bottom,
                20f,
                debugPaint
            )

            debugPaint.apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }
            canvas.drawLine(
                windowRectF.centerX(),
                windowRectF.centerY(),
                windowRectF.centerX(),
                windowRectF.bottom,
                debugPaint
            )
        }


    }

}
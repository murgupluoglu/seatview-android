package com.murgupluoglu.seatview.extensions

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.ColorInt
import com.murgupluoglu.seatview.SeatViewConfig
import com.murgupluoglu.seatview.SeatViewParameters

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class CenterLinesExtension(
    private val drawVertical: Boolean = true,
    private val drawHorizontal: Boolean = true,
    private var paintStyle: Paint.Style = Paint.Style.STROKE,
    private var strokeWidthSize: Float = 3f,
    private var lineEffect: DashPathEffect? = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f),
    private var antiAlias: Boolean = true,
    @ColorInt
    private var lineColor: Int = Color.BLUE
) : SeatViewExtension() {

    override fun isActive(): Boolean {
        return true
    }

    private lateinit var paint: Paint

    override fun init(params: SeatViewParameters, config: SeatViewConfig) {

        paint = Paint().apply {
            style = paintStyle
            strokeWidth = strokeWidthSize
            isAntiAlias = antiAlias
            color = lineColor
            if (lineEffect != null) {
                pathEffect = lineEffect
            }
        }
    }

    override fun draw(canvas: Canvas, params: SeatViewParameters, config: SeatViewConfig) {

        params.apply {
            if (drawVertical) {
                val centerLinePathVertical = Path()

                centerLinePathVertical.moveTo(
                    virtualRectF.centerX(),
                    windowRectF.top
                )
                centerLinePathVertical.lineTo(
                    virtualRectF.centerX(),
                    windowRectF.bottom
                )

                canvas.drawPath(centerLinePathVertical, paint)
            }

            if (drawHorizontal) {
                val centerLinePathHorizontal = Path()

                centerLinePathHorizontal.moveTo(
                    windowRectF.left,
                    virtualRectF.centerY()
                )
                centerLinePathHorizontal.lineTo(
                    windowRectF.right,
                    virtualRectF.centerY()
                )

                canvas.drawPath(centerLinePathHorizontal, paint)
            }
        }
    }
}
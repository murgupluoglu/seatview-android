package com.murgupluoglu.seatview.extensions

import android.graphics.*
import androidx.annotation.ColorInt
import com.murgupluoglu.seatview.SeatView

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class CenterLinesExtension(
    val drawVertical: Boolean = true,
    val drawHorizontal: Boolean = true,
    var _paintStyle: Paint.Style = Paint.Style.STROKE,
    var _strokeWidth: Float = 3f,
    var _pathEffect: DashPathEffect? = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f),
    var _isAntiAlias: Boolean = true,
    @ColorInt
    var _color: Int = Color.BLUE
) : SeatViewExtension() {

    override fun isActive(): Boolean {
        return true
    }

    private lateinit var paint: Paint

    override fun init(seatView: SeatView) {

        paint = Paint().apply {
            style = _paintStyle
            strokeWidth = _strokeWidth
            isAntiAlias = _isAntiAlias
            color = _color
            if (_pathEffect != null) {
                pathEffect = _pathEffect
            }
        }

    }

    override fun draw(seatView: SeatView, canvas: Canvas) {


        if (drawVertical) {
            val centerLinePathVertical = Path()

            centerLinePathVertical.moveTo(seatView.virtualRectF.centerX(), seatView.windowRectF.top)
            centerLinePathVertical.lineTo(
                seatView.virtualRectF.centerX(),
                seatView.windowRectF.bottom
            )

            canvas.drawPath(centerLinePathVertical, paint)
        }

        if (drawHorizontal) {
            val centerLinePathHorizontal = Path()

            centerLinePathHorizontal.moveTo(
                seatView.windowRectF.left,
                seatView.virtualRectF.centerY()
            )
            centerLinePathHorizontal.lineTo(
                seatView.windowRectF.right,
                seatView.virtualRectF.centerY()
            )

            canvas.drawPath(centerLinePathHorizontal, paint)
        }

    }
}
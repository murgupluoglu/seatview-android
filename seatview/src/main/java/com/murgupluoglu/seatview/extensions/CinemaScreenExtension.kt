package com.murgupluoglu.seatview.extensions

import android.graphics.*
import androidx.annotation.ColorInt
import com.murgupluoglu.seatview.SeatView


/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class CinemaScreenExtension(
    @ColorInt val screenBackgroundColor: Int = Color.RED,
    val screenPaintStyle: Paint.Style = Paint.Style.FILL,
    val screenCornerPathEffect: CornerPathEffect = CornerPathEffect(12f),
    val text: String = "Cinema Screen",
    @ColorInt val textColor: Int = Color.WHITE,
    val textAlign: Paint.Align = Paint.Align.CENTER
) : SeatViewExtension() {

    private val cinemaScreenPaint = Paint()
    private val cinemaScreenTextPaint = Paint()

    private var cinemaScreenViewHalfWidth: Float = 0f
    private var cinemaScreenViewHeight: Float = 0f

    override fun isActive(): Boolean {
        return true
    }

    override fun init(seatView: SeatView) {

        cinemaScreenPaint.color = screenBackgroundColor
        cinemaScreenPaint.style = screenPaintStyle
        cinemaScreenPaint.isAntiAlias = true
        cinemaScreenPaint.pathEffect = screenCornerPathEffect

        cinemaScreenTextPaint.color = textColor
        cinemaScreenTextPaint.textAlign = textAlign
        cinemaScreenTextPaint.isAntiAlias = true

    }

    override fun draw(seatView: SeatView, canvas: Canvas) {

        val cinemaScreenViewPath = Path()

        cinemaScreenViewHalfWidth = (seatView.virtualRectF.width() * 0.55f) / 2
        cinemaScreenViewHeight = (cinemaScreenViewHalfWidth * 2) / 8

        val centerX = seatView.virtualRectF.centerX()
        val top = seatView.virtualRectF.top - 10

        cinemaScreenViewPath.moveTo(centerX, top) //center
        cinemaScreenViewPath.lineTo(centerX - cinemaScreenViewHalfWidth + 10, top) //go left
        cinemaScreenViewPath.lineTo(
            centerX - cinemaScreenViewHalfWidth,
            top - cinemaScreenViewHeight
        ) //go up
        cinemaScreenViewPath.lineTo(
            centerX + cinemaScreenViewHalfWidth,
            top - cinemaScreenViewHeight
        ) // go right
        cinemaScreenViewPath.lineTo(centerX + cinemaScreenViewHalfWidth - 10, top) //go down
        cinemaScreenViewPath.close()

        cinemaScreenTextPaint.textSize = cinemaScreenViewHalfWidth / 7f

        canvas.drawPath(cinemaScreenViewPath, cinemaScreenPaint)
        canvas.drawText(
            text,
            centerX,
            top - (cinemaScreenTextPaint.textSize / 2),
            cinemaScreenTextPaint
        )

    }

}
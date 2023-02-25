package com.murgupluoglu.seatviewsample.cinemascreen

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Path
import androidx.annotation.ColorInt
import com.murgupluoglu.seatview.SeatViewConfig
import com.murgupluoglu.seatview.SeatViewParameters
import com.murgupluoglu.seatview.extensions.SeatViewExtension


/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class CinemaScreenExtension(
    @ColorInt val screenBackgroundColor: Int = Color.RED,
    private val screenPaintStyle: Paint.Style = Paint.Style.FILL,
    private val screenCornerPathEffect: CornerPathEffect = CornerPathEffect(12f),
    private val text: String = "Cinema Screen",
    @ColorInt val textColor: Int = Color.WHITE,
    private val cineTextAlign: Paint.Align = Paint.Align.CENTER
) : SeatViewExtension() {

    private val cinemaScreenPaint = Paint()
    private val cinemaScreenTextPaint = Paint()

    private var cinemaScreenViewHalfWidth: Float = 0f
    private var cinemaScreenViewHeight: Float = 0f

    override fun isActive(): Boolean {
        return true
    }

    override fun init(params: SeatViewParameters, config: SeatViewConfig) {

        cinemaScreenPaint.apply {
            color = screenBackgroundColor
            style = screenPaintStyle
            isAntiAlias = true
            pathEffect = screenCornerPathEffect
        }
        cinemaScreenTextPaint.apply {
            color = textColor
            textAlign = cineTextAlign
            isAntiAlias = true
        }
    }

    override fun draw(canvas: Canvas, params: SeatViewParameters, config: SeatViewConfig) {

        val cinemaScreenViewPath = Path()

        cinemaScreenViewHalfWidth = (params.virtualRectF.width() * 0.55f) / 2
        cinemaScreenViewHeight = (cinemaScreenViewHalfWidth * 2) / 8

        val centerX = params.virtualRectF.centerX()
        val top = params.virtualRectF.top - 10

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
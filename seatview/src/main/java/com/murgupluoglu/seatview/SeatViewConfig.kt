package com.murgupluoglu.seatview

import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import androidx.annotation.ColorInt

/*
*  Created by Mustafa Ürgüplüoğlu on 13.03.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

data class SeatViewConfig(
        var isDebug: Boolean = true,

        var leftPadding: Float = 50f,
        var rightPadding: Float = 50f,
        var topPadding: Float = 50f,
        var bottomPadding: Float = 50f,

        var seatMinWidth: Float = 12f.dp2px(),
        var seatMaxWidth: Float = 30f.dp2px(),
        var seatDefaultWidth:Float = 13f.dp2px(),
        var seatWidthHeightRatio: Float = 1f,
        var seatInlineGapWidthRatio: Float = 0.265f,
        var seatNewlineGapWidthRatio: Float = 0.304f,

        @ColorInt
        var backgroundColor: Int = Color.GRAY,
        var isZoomActive: Boolean = true,

        var centerLineConfig: CenterLineConfig = CenterLineConfig()
)

data class CenterLineConfig(
        var isActive : Boolean = false,
        var isVertical : Boolean = true,
        var paintStyle: Paint.Style = Paint.Style.STROKE,
        var strokeWidth : Float = 5f,
        var pathEffect : DashPathEffect? =  DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f),
        var isAntiAlias : Boolean = true,
        @ColorInt
        var color: Int = Color.BLUE

)
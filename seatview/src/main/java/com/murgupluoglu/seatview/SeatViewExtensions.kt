package com.murgupluoglu.seatview

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path

/*
*  Created by Mustafa Ürgüplüoğlu on 01.04.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/


fun drawCenterLine(seatView: SeatView, canvas : Canvas) {

    val centerLinePath = Path()

    val rectF = seatView.virtualRectF
    if(seatView.config.centerLineConfig.isVertical){
        centerLinePath.moveTo(rectF.centerX() , rectF.top)
        centerLinePath.lineTo(rectF.centerX(), rectF.bottom)
    }else{
        centerLinePath.moveTo(rectF.left , rectF.centerY())
        centerLinePath.lineTo(rectF.right, rectF.centerY())
    }

    seatView.config.centerLineConfig.let {
        Paint().apply {
            style = it.paintStyle
            strokeWidth = it.strokeWidth
            isAntiAlias = it.isAntiAlias
            color = it.color
            if (it.pathEffect != null) {
                pathEffect = it.pathEffect
            }
            canvas.drawPath(centerLinePath, this)
        }
    }
}


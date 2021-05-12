package com.murgupluoglu.seatviewsample.number

import android.graphics.*
import com.amulyakhare.textdrawable.TextDrawable
import com.murgupluoglu.seatview.Seat
import com.murgupluoglu.seatview.SeatView
import com.murgupluoglu.seatview.seatdrawer.SeatDrawer

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class NumberSeatDrawer : SeatDrawer() {

    private val commonPaint = Paint().apply {
        isAntiAlias = true
    }

    override fun draw(
        seatView: SeatView,
        canvas: Canvas,
        isInEditMode: Boolean,
        seatBean: Seat,
        seatRectF: RectF,
        seatWidth: Float,
        seatHeight: Float
    ) {

        val color: Int = when {
            seatBean.type == Seat.TYPE.UNSELECTABLE -> {
                Color.BLACK
            }
            seatBean.isSelected -> {
                Color.GREEN
            }
            else -> {
                Color.GRAY
            }
        }

        val drawable = TextDrawable.builder().buildRound(
            "${seatBean.columnIndex + 1 + (seatBean.rowIndex * seatView.columnCount)}",
            color
        )
        val drawBitmap =
            Bitmap.createBitmap(seatWidth.toInt(), seatWidth.toInt(), Bitmap.Config.ARGB_8888)
        val c = Canvas(drawBitmap!!)
        drawable.setBounds(0, 0, seatWidth.toInt(), seatWidth.toInt())
        drawable.draw(c)
        canvas.drawBitmap(drawBitmap, null, seatRectF, commonPaint)
    }
}
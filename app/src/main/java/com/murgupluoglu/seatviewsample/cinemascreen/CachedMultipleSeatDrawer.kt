package com.murgupluoglu.seatviewsample.cinemascreen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.murgupluoglu.seatview.Seat
import com.murgupluoglu.seatview.SeatViewConfig
import com.murgupluoglu.seatview.SeatViewParameters
import com.murgupluoglu.seatview.seatdrawer.SeatDrawer

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class CachedMultipleSeatDrawer : SeatDrawer {

    private var bitmaps = HashMap<String, Bitmap>()
    private val commonPaint = Paint().apply {
        isAntiAlias = true
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
        seat as MultipleSeat

        val drawableName = seat.getResourceName(isSelected)
        val drawBitmap = if (bitmaps[drawableName] != null) {
            bitmaps[drawableName]!!
        } else {
            val newBitmap =
                getBitmap(
                    context,
                    drawableName,
                    params.seatWidth.toInt(),
                    params.seatHeight.toInt()
                )
            bitmaps[drawableName] = newBitmap
            newBitmap
        }

        canvas.drawBitmap(drawBitmap, null, seatRectF, commonPaint)
    }

    private fun getBitmap(
        context: Context,
        drawableResourceName: String,
        width: Int,
        height: Int
    ): Bitmap {
        val resId =
            context.resources.getIdentifier(
                drawableResourceName,
                "drawable",
                context.packageName
            )
        val drawable = ContextCompat.getDrawable(context, resId)!!
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap!!)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }
}
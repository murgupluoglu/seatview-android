package com.murgupluoglu.seatviewsample.cinemascreen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.core.content.ContextCompat
import com.murgupluoglu.seatview.seatdrawer.SeatDrawScope
import com.murgupluoglu.seatview.seatdrawer.SeatDrawer
import androidx.core.graphics.createBitmap

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class CachedMultipleSeatDrawer : SeatDrawer {

    private var bitmaps = HashMap<String, Bitmap>()
    private val commonPaint = Paint().apply {
        isAntiAlias = true
    }

    override fun draw(scope: SeatDrawScope) {
        val seat = scope.seat as MultipleSeat

        val drawableName = seat.getResourceName(scope.isSelected)
        val drawBitmap = bitmaps[drawableName] ?: getBitmap(
            scope.context,
            drawableName,
            scope.parameters.seatWidth.toInt(),
            scope.parameters.seatHeight.toInt()
        ).also { bitmaps[drawableName] = it }

        scope.canvas.drawBitmap(drawBitmap, null, scope.seatRect, commonPaint)
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
        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        return bitmap
    }
}

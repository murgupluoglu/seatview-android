package com.murgupluoglu.seatview.seatdrawer

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.murgupluoglu.seatview.R
import com.murgupluoglu.seatview.Seat
import com.murgupluoglu.seatview.SeatView
import java.util.*

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

data class CachedSeatDrawer(val context: Context) : SeatDrawer() {

    private var bitmaps = HashMap<String, Bitmap>()
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

        val resourceName: String
        val resourceColor: String

        if (seatBean.isSelected) {
            resourceName = seatBean.selectedDrawableResourceName
            resourceColor = seatBean.selectedDrawableColor
        } else {
            resourceName = seatBean.drawableResourceName
            resourceColor = seatBean.drawableColor
        }

        val seatTypeId =
            seatBean.type.toString() + "_" + seatBean.multipleType + "_" + seatBean.drawableColor + "_" + seatBean.isSelected
        val drawBitmap = drawableToBitmap(
            isInEditMode,
            seatTypeId,
            seatWidth,
            seatHeight,
            resourceName,
            resourceColor
        )


        if (drawBitmap != null) canvas.drawBitmap(drawBitmap, null, seatRectF, commonPaint)

    }

    private fun drawableToBitmap(
        isInEditMode: Boolean,
        seatType: String,
        width: Float,
        height: Float,
        resourceName: String,
        color: String
    ): Bitmap? {

        var bitmap: Bitmap? = null
        if (bitmaps[seatType] != null) {
            bitmap = bitmaps[seatType]!!
        } else {
            var drawable = ContextCompat.getDrawable(context, R.drawable.square_seat)
            if (!isInEditMode && resourceName != "null") {
                val resId =
                    context.resources.getIdentifier(resourceName, "drawable", context.packageName)
                drawable = ContextCompat.getDrawable(context, resId)
            }
            if (drawable != null) {
                if (color != "null") {
                    DrawableCompat.setTint(drawable, Color.parseColor(color))
                }
                bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap!!)
                drawable.setBounds(0, 0, width.toInt(), height.toInt())
                drawable.draw(canvas)
                bitmaps[seatType] = bitmap
            }
        }

        return bitmap
    }
}
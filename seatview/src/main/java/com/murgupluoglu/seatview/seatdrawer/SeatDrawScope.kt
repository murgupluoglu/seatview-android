package com.murgupluoglu.seatview.seatdrawer

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import com.murgupluoglu.seatview.Seat
import com.murgupluoglu.seatview.SeatViewConfig
import com.murgupluoglu.seatview.SeatViewParameters

/**
 * Everything a [SeatDrawer] needs to paint a single seat, bundled into one object so the
 * drawing contract can gain fields over time without breaking every custom drawer's
 * method signature.
 */
class SeatDrawScope(
    val context: Context,
    val parameters: SeatViewParameters,
    val config: SeatViewConfig,
    val canvas: Canvas,
    val seat: Seat,
    val seatRect: RectF,
    val isSelected: Boolean,
)

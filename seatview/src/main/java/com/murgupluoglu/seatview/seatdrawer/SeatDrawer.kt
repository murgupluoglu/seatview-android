package com.murgupluoglu.seatview.seatdrawer

import android.graphics.Canvas
import android.graphics.RectF
import com.murgupluoglu.seatview.Seat
import com.murgupluoglu.seatview.SeatView

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

abstract class SeatDrawer {
    abstract fun draw(
        seatView: SeatView,
        canvas: Canvas,
        isInEditMode: Boolean,
        seatBean: Seat,
        seatRectF: RectF,
        seatWidth: Float,
        seatHeight: Float
    )
}
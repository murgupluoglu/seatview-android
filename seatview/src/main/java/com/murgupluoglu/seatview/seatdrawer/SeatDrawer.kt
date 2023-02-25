package com.murgupluoglu.seatview.seatdrawer

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import com.murgupluoglu.seatview.Seat
import com.murgupluoglu.seatview.SeatViewConfig
import com.murgupluoglu.seatview.SeatViewParameters

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

interface SeatDrawer {
    fun <SEAT : Seat> draw(
        context: Context,
        params: SeatViewParameters,
        config: SeatViewConfig,
        canvas: Canvas,
        seat: SEAT,
        seatRectF: RectF,
        isSelected: Boolean
    )
}
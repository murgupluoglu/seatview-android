package com.murgupluoglu.seatview.extensions

import android.graphics.Canvas
import com.murgupluoglu.seatview.SeatView

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

abstract class SeatViewExtension {
    abstract fun isActive() : Boolean
    abstract fun  init(seatView : SeatView)
    abstract fun draw(seatView : SeatView, canvas: Canvas)
}
package com.murgupluoglu.seatview.extensions

import android.graphics.Canvas
import com.murgupluoglu.seatview.SeatView

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

abstract class SeatViewExtension {
    /**
     * if its true view will be drawn on SeatView
     */
    abstract fun isActive(): Boolean

    /**
     * Its will be called before draw and one time
     */
    abstract fun init(seatView: SeatView)

    /**
     * Its called with every SeatView.draw()
     */
    abstract fun draw(seatView: SeatView, canvas: Canvas)
}
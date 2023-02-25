package com.murgupluoglu.seatview.extensions

import android.graphics.Canvas
import com.murgupluoglu.seatview.SeatViewConfig
import com.murgupluoglu.seatview.SeatViewParameters

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

abstract class SeatViewExtension {
    /**
     * if it's true, the view will be drawn on SeatView
     */
    abstract fun isActive(): Boolean

    /**
     * It will be called before the draw and one time
     */
    abstract fun init(params: SeatViewParameters, config: SeatViewConfig)

    /**
     * It's called with every SeatView#draw
     */
    abstract fun draw(canvas: Canvas, params: SeatViewParameters, config: SeatViewConfig)
}
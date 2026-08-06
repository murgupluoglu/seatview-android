package com.murgupluoglu.seatview.seatdrawer

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

/**
 * Strategy for painting a single seat. Implementations receive a [SeatDrawScope]
 * carrying the canvas, the seat, its rect and selection state.
 */
interface SeatDrawer {
    fun draw(scope: SeatDrawScope)
}

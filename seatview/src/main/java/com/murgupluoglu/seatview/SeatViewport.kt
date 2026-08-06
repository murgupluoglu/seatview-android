package com.murgupluoglu.seatview

import android.graphics.RectF

/**
 * Owns the pannable/zoomable viewport: it positions and clamps
 * [SeatViewParameters.virtualRectF] against [SeatViewParameters.windowRectF].
 * Extracted from [SeatView] (Single Responsibility) so the pan/clamp math is a
 * single, directly testable model instead of two ad-hoc ones.
 */
internal class SeatViewport(private val parameters: SeatViewParameters) {

    /** Centers a freshly-sized grid inside the window. */
    fun rebuildVirtualRect() {
        parameters.apply {
            val virtualWidth = (xSize * (seatWidth + seatInlineGap)) - seatInlineGap
            val virtualHeight = (ySize * (seatHeight + seatNewlineGap)) - seatNewlineGap
            val left = windowRectF.centerX() - (virtualWidth / 2)
            val top = windowRectF.centerY() - (virtualHeight / 2)
            virtualRectF = RectF(left, top, left + virtualWidth, top + virtualHeight)
        }
    }

    /** Pans by a scroll delta (as reported by GestureDetector), then re-clamps. */
    fun pan(distanceX: Float, distanceY: Float) {
        parameters.virtualRectF.offset(-distanceX, -distanceY)
        clamp()
    }

    /**
     * Centers the grid on an axis when it is smaller than the window, otherwise
     * clamps it so its edges cannot drift past the window edges (no empty gaps).
     */
    fun clamp() {
        parameters.apply {
            var dx = 0f
            var dy = 0f

            if (virtualRectF.width() <= windowRectF.width()) {
                dx = windowRectF.centerX() - virtualRectF.centerX()
            } else if (virtualRectF.left > windowRectF.left) {
                dx = windowRectF.left - virtualRectF.left
            } else if (virtualRectF.right < windowRectF.right) {
                dx = windowRectF.right - virtualRectF.right
            }

            if (virtualRectF.height() <= windowRectF.height()) {
                dy = windowRectF.centerY() - virtualRectF.centerY()
            } else if (virtualRectF.top > windowRectF.top) {
                dy = windowRectF.top - virtualRectF.top
            } else if (virtualRectF.bottom < windowRectF.bottom) {
                dy = windowRectF.bottom - virtualRectF.bottom
            }

            virtualRectF.offset(dx, dy)
        }
    }
}

package com.murgupluoglu.seatviewsample.battleship

import android.graphics.Color
import android.graphics.Paint
import com.murgupluoglu.seatview.seatdrawer.SeatDrawScope
import com.murgupluoglu.seatview.seatdrawer.SeatDrawer

/**
 * Paints a battleship square from the game's own state.
 *
 * Deliberately ignores [SeatDrawScope.isSelected]: the board's truth lives in
 * [BattleshipCell.state], not in SeatView's selection set, which the activity clears after every
 * shot. This is the pattern to copy when the view is a projection of external state.
 */
class BattleshipDrawer : SeatDrawer {

    private val fill = Paint().apply { isAntiAlias = true; style = Paint.Style.FILL }
    private val stroke = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        color = Color.WHITE
    }

    override fun draw(scope: SeatDrawScope) {
        val cell = scope.seat as BattleshipCell
        val rect = scope.seatRect
        val radius = scope.parameters.seatWidth * 0.18f

        fill.color = when (cell.state) {
            BattleshipCell.State.UNKNOWN -> WATER
            BattleshipCell.State.MISS -> MISS
            BattleshipCell.State.HIT -> HIT
            BattleshipCell.State.SUNK -> SUNK
            BattleshipCell.State.REVEALED -> REVEALED
        }
        scope.canvas.drawRoundRect(rect, radius, radius, fill)

        stroke.strokeWidth = scope.parameters.seatWidth * 0.09f
        val inset = scope.parameters.seatWidth * 0.3f
        when (cell.state) {
            // A small dot for splashes in the water.
            BattleshipCell.State.MISS ->
                scope.canvas.drawCircle(
                    rect.centerX(), rect.centerY(), scope.parameters.seatWidth * 0.12f, stroke
                )
            // A cross for a hit or a destroyed ship.
            BattleshipCell.State.HIT, BattleshipCell.State.SUNK -> {
                scope.canvas.drawLine(
                    rect.left + inset, rect.top + inset, rect.right - inset, rect.bottom - inset, stroke
                )
                scope.canvas.drawLine(
                    rect.right - inset, rect.top + inset, rect.left + inset, rect.bottom - inset, stroke
                )
            }
            else -> Unit
        }
    }

    private companion object {
        const val WATER = 0xFF1E6FA8.toInt()
        const val MISS = 0xFF4A8DB8.toInt()
        const val HIT = 0xFFE07B39.toInt()
        const val SUNK = 0xFF8E2420.toInt()
        const val REVEALED = 0xFF5A6672.toInt()
    }
}

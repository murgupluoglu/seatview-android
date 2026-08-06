package com.murgupluoglu.seatviewsample.battleship

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.murgupluoglu.seatview.SeatView
import com.murgupluoglu.seatview.SeatViewConfig
import com.murgupluoglu.seatview.SeatViewListener
import com.murgupluoglu.seatviewsample.R
import timber.log.Timber

/**
 * Battleship on top of [SeatView] — a sample that uses the grid for something other than seats.
 *
 * What it demonstrates:
 * - The grid is a projection of *external* state: the game owns the board, the drawer reads
 *   [BattleshipCell.state], and the selection set is cleared after every shot.
 * - [com.murgupluoglu.seatview.Seat.canSelect] is the only guard needed to stop a square being
 *   fired at twice — SeatView refuses the tap for us.
 * - [SeatViewListener.canSelectSeat] vetoes every tap once the game is over.
 * - [SeatView.isZoomEnabled] is a runtime property: set once here, it survives the
 *   `initSeatView` that starts each new game.
 * - Accessibility is free: each square announces its grid reference ("A1") to TalkBack.
 */
class BattleshipActivity : AppCompatActivity() {

    private lateinit var seatView: SeatView<BattleshipCell>
    private lateinit var statusText: TextView
    private var game = BattleshipGame()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_battleship)

        seatView = findViewById(R.id.seatView)
        statusText = findViewById(R.id.statusText)
        findViewById<Button>(R.id.newGameButton).setOnClickListener { startNewGame() }

        seatView.seatDrawer = BattleshipDrawer()
        // Pinch-zoom would fight with a fixed board; panning still works if it overflows.
        seatView.isZoomEnabled = false

        seatView.seatViewListener = object : SeatViewListener<BattleshipCell> {
            override fun canSelectSeat(
                clickedSeat: BattleshipCell,
                selectedSeats: Set<String>
            ): Boolean = !game.isOver

            override fun seatSelected(selectedSeat: BattleshipCell, selectedSeats: Set<String>) {
                fireAt(selectedSeat)
            }
        }

        startNewGame()
    }

    private fun startNewGame() {
        game = BattleshipGame()
        // A fresh grid; isZoomEnabled is deliberately NOT re-seeded by this call.
        seatView.initSeatView(game.cells, boardConfig())
        updateStatus()
    }

    private fun fireAt(cell: BattleshipCell) {
        val shot = game.fire(cell)
        Timber.d("fire %s -> %s", cell.name(), shot)

        // The board's truth is the game, not SeatView's selection, so drop the selection
        // immediately; the drawer renders from BattleshipCell.state.
        seatView.clearSelection()
        updateStatus()

        when (shot) {
            is BattleshipGame.Shot.Sunk -> toast("Sunk the ${shot.ship.name}!")
            BattleshipGame.Shot.Hit -> toast("Hit!")
            else -> Unit
        }
        if (game.isOver) {
            toast("All ships down in ${game.shots} shots 🎉")
        }
    }

    private fun updateStatus() {
        statusText.text = if (game.isOver) {
            "You win — ${game.shots} shots, ${game.hits} hits"
        } else {
            "Shots ${game.shots}   Hits ${game.hits}/${BattleshipGame.fleetCells}" +
                "   Sunk ${game.sunkShips}/${game.totalShips}"
        }
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun boardConfig() = SeatViewConfig(
        leftPadding = 24f,
        rightPadding = 24f,
        topPadding = 24f,
        bottomPadding = 24f,
        seatMinWidthDp = 16f,
        seatDefaultWidthDp = 30f,
        seatMaxWidthDp = 44f,
        seatInlineGapWidthRatio = 0.12f,
        seatNewlineGapWidthRatio = 0.12f,
        backgroundColor = 0xFF102A3A.toInt(),
    )
}

package com.murgupluoglu.seatviewsample.battleship

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import kotlin.random.Random

/**
 * The board rules are plain Kotlin, so they are tested on the JVM with a seeded [Random]
 * to make ship placement deterministic.
 */
class BattleshipGameTest {

    private fun game(seed: Int = 7) = BattleshipGame(random = Random(seed))

    private fun BattleshipGame.shipCells() =
        cells.flatten().filter { it.shipIndex != BattleshipGame.NO_SHIP }

    @Test
    fun boardStartsUntouched() {
        val game = game()

        assertThat(game.shots).isEqualTo(0)
        assertThat(game.hits).isEqualTo(0)
        assertThat(game.sunkShips).isEqualTo(0)
        assertThat(game.isOver).isFalse()
        assertThat(game.cells.flatten().map { it.state })
            .containsNoneOf(BattleshipCell.State.HIT, BattleshipCell.State.MISS)
    }

    @Test
    fun fleetOccupiesExactlyTheExpectedNumberOfCells() {
        // No overlap: total occupied cells must equal the sum of the ship lengths.
        repeat(25) { seed ->
            val game = game(seed)
            assertThat(game.shipCells()).hasSize(BattleshipGame.fleetCells)
        }
    }

    @Test
    fun everyShipIsPlacedContiguouslyInOneLineForManySeeds() {
        repeat(25) { seed ->
            val game = game(seed)
            BattleshipGame.FLEET.forEachIndexed { index, ship ->
                val cells = game.shipCells().filter { it.shipIndex == index }
                assertThat(cells).hasSize(ship.length)

                val rows = cells.map { it.row }.distinct()
                val columns = cells.map { it.column }.distinct()
                val horizontal = rows.size == 1
                // Exactly one axis is fixed...
                assertThat(horizontal || columns.size == 1).isTrue()
                // ...and the other is a run of consecutive indices.
                val moving = (if (horizontal) cells.map { it.column } else cells.map { it.row }).sorted()
                assertThat(moving.last() - moving.first()).isEqualTo(ship.length - 1)
                assertThat(moving.distinct()).hasSize(ship.length)
            }
        }
    }

    @Test
    fun firingAtWaterIsAMiss() {
        val game = game()
        val water = game.cells.flatten().first { it.shipIndex == BattleshipGame.NO_SHIP }

        assertThat(game.fire(water)).isEqualTo(BattleshipGame.Shot.Miss)
        assertThat(water.state).isEqualTo(BattleshipCell.State.MISS)
        assertThat(game.shots).isEqualTo(1)
        assertThat(game.hits).isEqualTo(0)
    }

    @Test
    fun firingAtAShipIsAHitAndBlocksTheSquare() {
        val game = game()
        val target = game.shipCells().first { cell ->
            BattleshipGame.FLEET[cell.shipIndex].length > 1
        }

        assertThat(game.fire(target)).isEqualTo(BattleshipGame.Shot.Hit)
        assertThat(target.state).isEqualTo(BattleshipCell.State.HIT)
        assertThat(game.hits).isEqualTo(1)
        // SeatView refuses a second tap because of this:
        assertThat(target.canSelect()).isFalse()
    }

    @Test
    fun repeatedShotDoesNotCount() {
        val game = game()
        val water = game.cells.flatten().first { it.shipIndex == BattleshipGame.NO_SHIP }
        game.fire(water)

        assertThat(game.fire(water)).isEqualTo(BattleshipGame.Shot.Repeated)
        assertThat(game.shots).isEqualTo(1) // unchanged
    }

    @Test
    fun sinkingAShipReportsItAndMarksAllOfItsCells() {
        val game = game()
        val shortest = BattleshipGame.FLEET.withIndex().minBy { it.value.length }
        val cells = game.shipCells().filter { it.shipIndex == shortest.index }

        val outcomes = cells.map { game.fire(it) }

        assertThat(outcomes.last()).isEqualTo(BattleshipGame.Shot.Sunk(shortest.value))
        assertThat(cells.map { it.state })
            .containsExactlyElementsIn(List(cells.size) { BattleshipCell.State.SUNK })
        assertThat(game.sunkShips).isEqualTo(1)
        assertThat(game.isOver).isFalse()
    }

    @Test
    fun gameIsOverOnlyWhenEveryShipIsSunk() {
        val game = game()
        val shipCells = game.shipCells()

        shipCells.dropLast(1).forEach { game.fire(it) }
        assertThat(game.isOver).isFalse()

        game.fire(shipCells.last())

        assertThat(game.isOver).isTrue()
        assertThat(game.sunkShips).isEqualTo(game.totalShips)
        assertThat(game.hits).isEqualTo(BattleshipGame.fleetCells)
        assertThat(game.shots).isEqualTo(BattleshipGame.fleetCells)
    }

    @Test
    fun revealAllExposesUnhitShipsOnly() {
        val game = game()
        val target = game.shipCells().first()
        game.fire(target)

        game.revealAll()

        assertThat(target.state).isEqualTo(BattleshipCell.State.HIT)
        assertThat(game.shipCells().filter { it !== target }.map { it.state })
            .doesNotContain(BattleshipCell.State.UNKNOWN)
        // Water is untouched by a reveal.
        assertThat(game.cells.flatten().filter { it.shipIndex == BattleshipGame.NO_SHIP }
            .all { it.state == BattleshipCell.State.UNKNOWN }).isTrue()
    }

    @Test
    fun cellLabelsMatchGridReferences() {
        val game = game()

        assertThat(game.cellAt(0, 0)!!.name()).isEqualTo("A1")
        assertThat(game.cellAt(2, 4)!!.name()).isEqualTo("C5")
        assertThat(game.cellAt(9, 9)!!.name()).isEqualTo("J10")
        assertThat(game.cellAt(10, 0)).isNull()
    }
}

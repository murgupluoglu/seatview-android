package com.murgupluoglu.seatviewsample.battleship

import kotlin.random.Random

/**
 * Battleship board state. Deliberately free of Android and SeatView types so the rules can be
 * unit-tested on their own; [Random] is injectable to make ship placement deterministic.
 */
class BattleshipGame(
    val size: Int = 10,
    random: Random = Random.Default,
) {

    data class Ship(val name: String, val length: Int)

    /** Outcome of a single shot. */
    sealed interface Shot {
        data object Miss : Shot
        data object Hit : Shot
        data class Sunk(val ship: Ship) : Shot

        /** The cell was already fired at, so nothing happened. */
        data object Repeated : Shot
    }

    /** `[row][column]`, matching SeatView's grid layout. */
    val cells: Array<Array<BattleshipCell>> =
        Array(size) { row -> Array(size) { column -> BattleshipCell(row, column) } }

    var shots: Int = 0
        private set
    var hits: Int = 0
        private set

    private val hitsPerShip = IntArray(FLEET.size)

    val sunkShips: Int get() = FLEET.indices.count { hitsPerShip[it] == FLEET[it].length }
    val totalShips: Int get() = FLEET.size
    val isOver: Boolean get() = sunkShips == FLEET.size

    init {
        placeFleet(random)
    }

    fun cellAt(row: Int, column: Int): BattleshipCell? =
        if (row in 0 until size && column in 0 until size) cells[row][column] else null

    fun fire(cell: BattleshipCell): Shot {
        if (cell.state != BattleshipCell.State.UNKNOWN) return Shot.Repeated

        shots++
        val shipIndex = cell.shipIndex
        if (shipIndex == NO_SHIP) {
            cell.state = BattleshipCell.State.MISS
            return Shot.Miss
        }

        hits++
        hitsPerShip[shipIndex]++
        val ship = FLEET[shipIndex]
        return if (hitsPerShip[shipIndex] == ship.length) {
            // Mark the whole ship so the drawer can render it as destroyed.
            forEachCellOfShip(shipIndex) { it.state = BattleshipCell.State.SUNK }
            Shot.Sunk(ship)
        } else {
            cell.state = BattleshipCell.State.HIT
            Shot.Hit
        }
    }

    /** Reveals every remaining ship — used when the player gives up. */
    fun revealAll() {
        cells.forEach { row ->
            row.forEach { cell ->
                if (cell.shipIndex != NO_SHIP && cell.state == BattleshipCell.State.UNKNOWN) {
                    cell.state = BattleshipCell.State.REVEALED
                }
            }
        }
    }

    private inline fun forEachCellOfShip(shipIndex: Int, action: (BattleshipCell) -> Unit) {
        cells.forEach { row -> row.forEach { if (it.shipIndex == shipIndex) action(it) } }
    }

    private fun placeFleet(random: Random) {
        FLEET.forEachIndexed { index, ship ->
            var placed = false
            var attempts = 0
            while (!placed && attempts < MAX_PLACEMENT_ATTEMPTS) {
                attempts++
                val horizontal = random.nextBoolean()
                val row = random.nextInt(if (horizontal) size else size - ship.length + 1)
                val column = random.nextInt(if (horizontal) size - ship.length + 1 else size)
                if (fits(row, column, ship.length, horizontal)) {
                    for (offset in 0 until ship.length) {
                        val target = if (horizontal) cells[row][column + offset]
                        else cells[row + offset][column]
                        target.shipIndex = index
                    }
                    placed = true
                }
            }
            check(placed) { "Could not place ${ship.name} on a ${size}x$size board" }
        }
    }

    private fun fits(row: Int, column: Int, length: Int, horizontal: Boolean): Boolean =
        (0 until length).all { offset ->
            val cell = if (horizontal) cellAt(row, column + offset) else cellAt(row + offset, column)
            cell != null && cell.shipIndex == NO_SHIP
        }

    companion object {
        const val NO_SHIP = -1
        private const val MAX_PLACEMENT_ATTEMPTS = 500

        val FLEET = listOf(
            Ship("Carrier", 5),
            Ship("Battleship", 4),
            Ship("Cruiser", 3),
            Ship("Submarine", 3),
            Ship("Destroyer", 2),
        )

        /** Total cells occupied by the fleet. */
        val fleetCells: Int get() = FLEET.sumOf { it.length }
    }
}

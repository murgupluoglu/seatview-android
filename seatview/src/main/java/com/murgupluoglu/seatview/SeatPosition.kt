package com.murgupluoglu.seatview

/**
 * A seat's position in the grid, addressed by [row] and [column] (both zero-based).
 *
 * Replaces the old raw `(xIndex, yIndex)` / `Pair<Int, Int>` coordinates, which were
 * ambiguous (the grid is stored `[row][column]` but the API took `(x, y)`). Using a
 * named type removes that confusion at the call site.
 */
data class SeatPosition(
    val row: Int,
    val column: Int,
)

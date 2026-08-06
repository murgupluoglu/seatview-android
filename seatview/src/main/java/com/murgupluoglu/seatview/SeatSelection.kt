package com.murgupluoglu.seatview

/**
 * Pure state holder for the selected seat ids, including multi-seat grouping via
 * [Seat.allConnectedSeatIds]. It owns no `View` concerns: [SeatView] fires listener
 * callbacks and calls `invalidate()` based on the booleans returned here.
 */
internal class SeatSelection<SEAT : Seat> {

    private val ids = HashSet<String>()

    /** Read-only, live view of the selected ids. */
    val selectedIds: Set<String> get() = ids

    fun isSelected(id: String): Boolean = ids.contains(id)

    /**
     * Rebuilds the selection from the grid's pre-selected seats, discarding previous state.
     * Invisible seats (grid gaps) are skipped even if they claim to be pre-selected, so the
     * selection can never contain a seat that is neither drawn nor tappable.
     */
    fun initFrom(seats: Array<Array<SEAT>>) {
        ids.clear()
        seats.forEach { row ->
            row.forEach { seat ->
                if (seat.isVisible() && seat.isPreSelected()) addConnected(seat)
            }
        }
    }

    /** @return true if the seat became newly selected. */
    fun select(seat: SEAT): Boolean {
        if (ids.contains(seat.id())) return false
        addConnected(seat)
        return true
    }

    /** @return true if the seat was selected and is now released. */
    fun release(seat: SEAT): Boolean {
        if (!ids.contains(seat.id())) return false
        ids.remove(seat.id())
        seat.allConnectedSeatIds().forEach { ids.remove(it) }
        return true
    }

    /** @return true if anything was cleared. */
    fun clear(): Boolean {
        if (ids.isEmpty()) return false
        ids.clear()
        return true
    }

    /** Replaces the selection wholesale (used by instance-state restore). */
    fun restore(restored: Collection<String>) {
        ids.clear()
        ids.addAll(restored)
    }

    private fun addConnected(seat: SEAT) {
        ids.add(seat.id())
        seat.allConnectedSeatIds().forEach { ids.add(it) }
    }
}

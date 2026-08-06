package com.murgupluoglu.seatview

/**
 * Callbacks for seat interaction. Every method has a default (no-op / allow), so an
 * implementer overrides only what it needs, and [SeatView] uses a no-op instance by
 * default — the view never crashes just because a listener was not assigned.
 *
 * Collections are exposed as read-only [Set]s so callers cannot mutate selection state.
 */
interface SeatViewListener<SEAT> {

    /** Fired after a seat (and any connected seats) are released. */
    fun seatReleased(releasedSeat: SEAT, selectedSeats: Set<String>) {}

    /** Fired after a seat (and any connected seats) become selected. */
    fun seatSelected(selectedSeat: SEAT, selectedSeats: Set<String>) {}

    /**
     * Dynamic, interaction-time veto for a tap selection: return false to block it
     * (e.g. a maximum-seats rule). Distinct from [Seat.canSelect], which is the static
     * model-level eligibility enforced by [SeatView.selectSeat].
     */
    fun canSelectSeat(clickedSeat: SEAT, selectedSeats: Set<String>): Boolean = true

    /**
     * Fired **once** after a selection is restored from saved instance state (e.g. a rotation),
     * with the full set of restored ids. Rebuild derived state (seat count, price, order list)
     * from [selectedSeats] here.
     *
     * This is deliberately *not* [seatSelected]: a restore is not a user action, and a connected
     * multi-seat group would otherwise produce one callback per member and double-count. It is
     * also delivered after the listener is assigned, so assigning the listener *after*
     * `initSeatView` still receives it.
     */
    fun selectionRestored(selectedSeats: Set<String>) {}
}

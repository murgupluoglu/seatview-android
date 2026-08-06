package com.murgupluoglu.seatview

import android.graphics.Rect
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.customview.widget.ExploreByTouchHelper

/**
 * Exposes each visible seat as a virtual accessibility node so TalkBack (and other
 * a11y services) can explore and toggle seats that are otherwise just pixels on a
 * `Canvas`. Virtual view ids are the seats' packed `row * columnCount + column`.
 */
internal class SeatViewAccessibilityHelper<SEAT : Seat>(
    private val seatView: SeatView<SEAT>,
) : ExploreByTouchHelper(seatView) {

    override fun getVirtualViewAt(x: Float, y: Float): Int {
        val position = seatView.positionAt(x, y) ?: return INVALID_ID
        return seatView.virtualIdOf(position)
    }

    override fun getVisibleVirtualViews(virtualViewIds: MutableList<Int>) {
        for (row in 0 until seatView.rowCount) {
            for (column in 0 until seatView.columnCount) {
                val position = SeatPosition(row, column)
                val seat = seatView.seatAt(position) ?: continue
                if (seat.isVisible()) virtualViewIds.add(seatView.virtualIdOf(position))
            }
        }
    }

    override fun onPopulateEventForVirtualView(virtualViewId: Int, event: AccessibilityEvent) {
        val position = seatView.positionForVirtualId(virtualViewId)
        val seat = position?.let { seatView.seatAt(it) }
        event.contentDescription = seat?.name() ?: ""
    }

    /**
     * [setBoundsInParent] is deprecated on [AccessibilityNodeInfoCompat], but
     * [ExploreByTouchHelper] still requires virtual-node bounds to be reported in the host's
     * coordinate space, so there is no non-deprecated alternative here.
     */
    @Suppress("DEPRECATION")
    override fun onPopulateNodeForVirtualView(virtualViewId: Int, node: AccessibilityNodeInfoCompat) {
        val position = seatView.positionForVirtualId(virtualViewId)
        val seat = position?.let { seatView.seatAt(it) }
        if (position == null || seat == null) {
            node.contentDescription = ""
            node.setBoundsInParent(EMPTY_BOUNDS)
            return
        }

        val selected = seatView.isSelected(seat.id())
        node.contentDescription = seat.name()
        node.className = "android.widget.Button"
        node.isEnabled = seat.canSelect()
        node.isClickable = true
        node.isCheckable = true
        node.isChecked = selected
        node.isSelected = selected
        node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK)

        val bounds = Rect()
        val rectF = seatView.seatRect(position)
        if (rectF != null) rectF.round(bounds) else bounds.set(EMPTY_BOUNDS)
        node.setBoundsInParent(bounds)
    }

    override fun onPerformActionForVirtualView(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean {
        if (action == AccessibilityNodeInfoCompat.ACTION_CLICK) {
            val position = seatView.positionForVirtualId(virtualViewId) ?: return false
            seatView.toggleForAccessibility(position)
            return true
        }
        return false
    }

    private companion object {
        val EMPTY_BOUNDS = Rect(0, 0, 1, 1)
    }
}

package com.murgupluoglu.seatviewsample

import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.murgupluoglu.seatview.SeatPosition
import com.murgupluoglu.seatview.SeatView
import com.murgupluoglu.seatviewsample.cinemascreen.CinemaScreenActivity
import com.murgupluoglu.seatviewsample.cinemascreen.MultipleSeat
import com.google.common.truth.Truth.assertThat
import com.google.common.truth.Truth.assertWithMessage
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Gesture-level instrumented tests: they exercise the full touch pipeline
 * (positionAt / viewport pan / the zoom gate in onTouchEvent) by feeding synthetic
 * [MotionEvent]s through [SeatView.onTouchEvent] on the UI thread, and assert only via
 * the public API ([SeatView.seatRect], [SeatView.selectedSeatIds], ...).
 *
 * NOTE: authored without an on-device run. Validate with:
 *   ./gradlew :app:connectedAndroidTest
 * Synthetic multitouch (pinch) and touch-slop timing vary by device/API; tune the span
 * deltas / step timing in the helpers if the zoom or scroll cases misbehave. The exact
 * pan/clamp math is covered deterministically by SeatViewportTest on the JVM.
 */
@LargeTest
@RunWith(AndroidJUnit4::class)
class SeatViewGestureTest {

    @get:Rule
    var rule: ActivityScenarioRule<CinemaScreenActivity> =
        ActivityScenarioRule(CinemaScreenActivity::class.java)

    private fun seatView(activity: CinemaScreenActivity): SeatView<MultipleSeat> =
        activity.findViewById(R.id.seatView)

    // region touch helpers

    private fun tapAt(view: View, x: Float, y: Float, downTime: Long) {
        val down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0)
        val up = MotionEvent.obtain(downTime, downTime + 50, MotionEvent.ACTION_UP, x, y, 0)
        view.onTouchEvent(down)
        view.onTouchEvent(up)
        down.recycle()
        up.recycle()
    }

    private fun dragBy(view: View, startX: Float, startY: Float, dx: Float, dy: Float, downTime: Long) {
        val down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, startX, startY, 0)
        view.onTouchEvent(down)
        down.recycle()

        val steps = 5
        for (i in 1..steps) {
            val t = downTime + i * 16L
            val move = MotionEvent.obtain(
                downTime, t, MotionEvent.ACTION_MOVE,
                startX + dx * i / steps, startY + dy * i / steps, 0
            )
            view.onTouchEvent(move)
            move.recycle()
        }

        val up = MotionEvent.obtain(
            downTime, downTime + (steps + 1) * 16L, MotionEvent.ACTION_UP,
            startX + dx, startY + dy, 0
        )
        view.onTouchEvent(up)
        up.recycle()
    }

    private fun pinch(view: View, focusX: Float, focusY: Float, startSpan: Float, endSpan: Float, downTime: Long) {
        val props = arrayOf(
            MotionEvent.PointerProperties().apply { id = 0; toolType = MotionEvent.TOOL_TYPE_FINGER },
            MotionEvent.PointerProperties().apply { id = 1; toolType = MotionEvent.TOOL_TYPE_FINGER },
        )
        fun coords(span: Float): Array<MotionEvent.PointerCoords> = arrayOf(
            MotionEvent.PointerCoords().apply { x = focusX - span / 2f; y = focusY },
            MotionEvent.PointerCoords().apply { x = focusX + span / 2f; y = focusY },
        )
        fun obtain(t: Long, action: Int, count: Int, span: Float): MotionEvent =
            MotionEvent.obtain(downTime, t, action, count, props, coords(span), 0, 0, 1f, 1f, 0, 0, 0, 0)

        val pointerDown = MotionEvent.ACTION_POINTER_DOWN or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)
        val pointerUp = MotionEvent.ACTION_POINTER_UP or (1 shl MotionEvent.ACTION_POINTER_INDEX_SHIFT)

        obtain(downTime, MotionEvent.ACTION_DOWN, 1, startSpan).also { view.onTouchEvent(it); it.recycle() }
        obtain(downTime + 10, pointerDown, 2, startSpan).also { view.onTouchEvent(it); it.recycle() }
        val steps = 6
        for (i in 1..steps) {
            val span = startSpan + (endSpan - startSpan) * i / steps
            obtain(downTime + 10 + i * 16L, MotionEvent.ACTION_MOVE, 2, span)
                .also { view.onTouchEvent(it); it.recycle() }
        }
        obtain(downTime + 200, pointerUp, 2, endSpan).also { view.onTouchEvent(it); it.recycle() }
        obtain(downTime + 210, MotionEvent.ACTION_UP, 1, endSpan).also { view.onTouchEvent(it); it.recycle() }
    }

    // endregion

    // region geometry helpers (public API only)

    private fun findSeat(
        view: SeatView<MultipleSeat>,
        predicate: (MultipleSeat) -> Boolean,
    ): SeatPosition? {
        for (row in 0 until view.rowCount) {
            for (column in 0 until view.columnCount) {
                val position = SeatPosition(row, column)
                val seat = view.seatAt(position) ?: continue
                if (seat.isVisible() && predicate(seat)) return position
            }
        }
        return null
    }

    private fun centerOf(view: SeatView<MultipleSeat>, position: SeatPosition): Pair<Float, Float> {
        val rect = view.seatRect(position)!!
        return rect.centerX() to rect.centerY()
    }

    private fun plainSelectable(view: SeatView<MultipleSeat>) = findSeat(view) {
        it.type == MultipleSeat.TYPE.SELECTABLE && it.multipleType == MultipleSeat.MULTIPLETYPE.NOTMULTIPLE
    }

    // endregion

    @Test
    fun tapSelectsSelectableSeat() {
        rule.scenario.onActivity { activity ->
            val view = seatView(activity)
            val position = plainSelectable(view)
            assumeTrue("no plain selectable seat in sample data", position != null)

            val before = view.selectedSeatIds.size
            val id = view.seatAt(position!!)!!.id()
            val (cx, cy) = centerOf(view, position)

            tapAt(view, cx, cy, SystemClock.uptimeMillis())

            assertWithMessage("seat should be selected").that(view.isSelected(id)).isTrue()
            assertThat(view.selectedSeatIds).hasSize(before + 1)
        }
    }

    @Test
    fun tapTwiceSelectsThenReleasesSeat() {
        rule.scenario.onActivity { activity ->
            val view = seatView(activity)
            val position = plainSelectable(view)
            assumeTrue("no plain selectable seat in sample data", position != null)

            val before = view.selectedSeatIds.size
            val id = view.seatAt(position!!)!!.id()
            val (cx, cy) = centerOf(view, position)
            val t0 = SystemClock.uptimeMillis()

            tapAt(view, cx, cy, t0)               // select
            tapAt(view, cx, cy, t0 + 1000)        // +1s so it is not a double tap → release

            assertWithMessage("seat should be released").that(view.isSelected(id)).isFalse()
            assertThat(view.selectedSeatIds).hasSize(before)
        }
    }

    @Test
    fun tapOnUnselectableSeatIsVetoed() {
        rule.scenario.onActivity { activity ->
            val view = seatView(activity)
            val position = findSeat(view) {
                it.type == MultipleSeat.TYPE.UNSELECTABLE &&
                    it.multipleType == MultipleSeat.MULTIPLETYPE.NOTMULTIPLE
            }
            assumeTrue("no plain unselectable seat in sample data", position != null)

            val before = view.selectedSeatIds.size
            val id = view.seatAt(position!!)!!.id()
            val (cx, cy) = centerOf(view, position)

            tapAt(view, cx, cy, SystemClock.uptimeMillis())

            assertThat(view.isSelected(id)).isFalse()
            assertThat(view.selectedSeatIds).hasSize(before)
        }
    }

    @Test
    fun tapOnBlankAreaSelectsNothing() {
        rule.scenario.onActivity { activity ->
            val view = seatView(activity)
            val before = view.selectedSeatIds.size

            tapAt(view, 2f, 2f, SystemClock.uptimeMillis()) // top-left: outside the centered grid

            assertThat(view.selectedSeatIds).hasSize(before)
        }
    }

    @Test
    fun scrollKeepsGridCoveringTheWindow() {
        rule.scenario.onActivity { activity ->
            val view = seatView(activity)
            if (view.rowCount == 0 || view.columnCount == 0) return@onActivity

            // Install a grid that is guaranteed wider than any screen, so the pan path
            // actually runs instead of being skipped on large displays.
            val wide = Array(8) { row ->
                Array(80) { column ->
                    MultipleSeat(
                        seatId = "w$column-$row",
                        seatName = "$column",
                        type = MultipleSeat.TYPE.SELECTABLE,
                    )
                }
            }
            view.initSeatView(wide, com.murgupluoglu.seatview.SeatViewConfig(seatDefaultWidthDp = 30f))

            val topLeft = view.seatRect(SeatPosition(0, 0))!!
            val topRight = view.seatRect(SeatPosition(0, view.columnCount - 1))!!
            val gridWidth = topRight.right - topLeft.left
            assertWithMessage("test grid must overflow the viewport")
                .that(gridWidth).isGreaterThan(view.width.toFloat())

            val beforeLeft = topLeft.left
            dragBy(view, view.width / 2f, view.height / 2f, -300f, 0f, SystemClock.uptimeMillis())

            val after = view.seatRect(SeatPosition(0, 0))!!
            val afterRight = view.seatRect(SeatPosition(0, view.columnCount - 1))!!.right
            // Dragging content left must move the grid left (or hold it at the clamp edge).
            assertWithMessage("grid must not move right when dragged left")
                .that(after.left).isAtMost(beforeLeft + 1f)
            // Strict clamp invariant: an overflowing grid may never expose empty space inside
            // the window (view bounds minus the config's default 50px paddings).
            val windowLeft = 50f
            val windowRight = view.width - 50f
            assertWithMessage("left edge must still cover the window")
                .that(after.left).isAtMost(windowLeft + 1f)
            assertWithMessage("right edge must still cover the window")
                .that(afterRight).isAtLeast(windowRight - 1f)
        }
    }

    /**
     * Pinching a laid-out but not-yet-populated view (async seat load) used to compute
     * factor = 0f/0f = NaN and permanently corrupt virtualRectF, so the grid never appeared
     * once the data arrived.
     */
    @Test
    fun pinchBeforeGridArrives_doesNotCorruptTheViewport() {
        rule.scenario.onActivity { activity ->
            val view = SeatView<MultipleSeat>(activity)
            view.measure(
                View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.EXACTLY),
            )
            view.layout(0, 0, 1080, 1920)

            // No initSeatView yet: every seat width is 0f.
            pinch(view, 540f, 960f, 200f, 600f, SystemClock.uptimeMillis())

            val grid = Array(4) { row ->
                Array(4) { col -> MultipleSeat(seatId = "$col-$row", seatName = "$col", type = MultipleSeat.TYPE.SELECTABLE) }
            }
            view.initSeatView(grid)

            val rect = view.seatRect(SeatPosition(0, 0))
            assertWithMessage("seat rect must exist after the grid arrives").that(rect).isNotNull()
            assertWithMessage("viewport must not be NaN").that(rect!!.left.isNaN()).isFalse()
            assertThat(rect.width()).isGreaterThan(0f)
        }
    }

    /**
     * A spread gesture must *actually* enlarge the seats. Asserting only "did not shrink"
     * would stay green if the zoom pipeline stopped working altogether.
     */
    @Test
    fun pinchSpreadEnlargesSeats() {
        rule.scenario.onActivity { activity ->
            val view = seatView(activity)
            val probe = SeatPosition(0, 0)
            val before = view.seatRect(probe)!!.width()

            pinch(view, view.width / 2f, view.height / 2f, 200f, 600f, SystemClock.uptimeMillis())

            val after = view.seatRect(probe)!!.width()
            assertWithMessage("spread must enlarge seats (before=$before after=$after)")
                .that(after).isGreaterThan(before)
        }
    }

    /** Repeated spreads must saturate at the configured max width rather than grow forever. */
    @Test
    fun pinchZoomSaturatesAtMaxWidth() {
        rule.scenario.onActivity { activity ->
            val view = seatView(activity)
            val probe = SeatPosition(0, 0)
            var t = SystemClock.uptimeMillis()

            repeat(8) {
                pinch(view, view.width / 2f, view.height / 2f, 200f, 600f, t)
                t += 500
            }
            val saturated = view.seatRect(probe)!!.width()

            pinch(view, view.width / 2f, view.height / 2f, 200f, 600f, t)

            assertWithMessage("width must stop growing once clamped at the max")
                .that(view.seatRect(probe)!!.width()).isWithin(0.5f).of(saturated)
        }
    }

    /**
     * Differential test: the same gesture that provably zooms when enabled must be a no-op
     * when disabled — so this cannot pass just because the gesture does nothing.
     */
    @Test
    fun pinchZoomIgnoredWhenZoomDisabled() {
        rule.scenario.onActivity { activity ->
            val view = seatView(activity)
            val probe = SeatPosition(0, 0)
            var t = SystemClock.uptimeMillis()

            view.isZoomEnabled = false
            val before = view.seatRect(probe)!!.width()
            pinch(view, view.width / 2f, view.height / 2f, 200f, 600f, t)
            val whileDisabled = view.seatRect(probe)!!.width()
            assertWithMessage("zoom disabled must ignore the pinch")
                .that(whileDisabled).isWithin(0.001f).of(before)

            // Same gesture, zoom enabled: proves the gesture itself is capable of zooming.
            t += 500
            view.isZoomEnabled = true
            pinch(view, view.width / 2f, view.height / 2f, 200f, 600f, t)
            assertWithMessage("the same gesture must zoom once enabled")
                .that(view.seatRect(probe)!!.width()).isGreaterThan(before)
        }
    }
}

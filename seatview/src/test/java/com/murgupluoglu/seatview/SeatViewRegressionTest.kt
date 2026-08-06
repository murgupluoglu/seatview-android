package com.murgupluoglu.seatview

import android.graphics.Bitmap
import android.graphics.Canvas
import com.google.common.truth.Truth.assertThat
import com.murgupluoglu.seatview.extensions.SeatViewExtension
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * One test per defect found in the v4 code review, so none of them can silently return.
 */
@RunWith(RobolectricTestRunner::class)
class SeatViewRegressionTest {

    private fun draw(view: SeatView<TestSeat>, w: Int = 1080, h: Int = 1920) {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
    }

    /** Records init/draw ordering so the documented extension contract can be asserted. */
    private class RecordingExtension : SeatViewExtension() {
        var initCount = 0
        var drawsBeforeInit = 0
        var drawCount = 0
        override fun isActive(): Boolean = true
        override fun init(params: SeatViewParameters, config: SeatViewConfig) {
            initCount++
        }

        override fun draw(canvas: Canvas, params: SeatViewParameters, config: SeatViewConfig) {
            if (initCount == 0) drawsBeforeInit++
            drawCount++
        }
    }

    // #2 — an inverted width range used to crash later, inside the pinch handler.
    @Test
    fun initSeatView_invertedWidthBounds_throwsImmediately() {
        val error = runCatching {
            newSeatView().initSeatView(
                buildGrid(cols = 3, rows = 3),
                SeatViewConfig(seatMinWidthDp = 40f, seatMaxWidthDp = 30f, seatDefaultWidthDp = 45f),
            )
        }.exceptionOrNull()

        assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(error!!).hasMessageThat().contains("seatMinWidthDp")
    }

    @Test
    fun initSeatView_defaultWidthAboveMax_throws() {
        val error = runCatching {
            newSeatView().initSeatView(
                buildGrid(cols = 3, rows = 3),
                SeatViewConfig(seatMinWidthDp = 10f, seatMaxWidthDp = 20f, seatDefaultWidthDp = 30f),
            )
        }.exceptionOrNull()

        assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
    }

    // #3 — the gap between seats must stay part of the owning seat's touch target.
    @Test
    fun positionAt_tapInRowGapResolvesToOwningSeat() {
        val view = newSeatView()
        view.layoutForTest()
        view.initSeatView(buildGrid(cols = 5, rows = 5))

        val seatRect = view.seatRect(SeatPosition(row = 0, column = 0))!!
        // A point below the drawn seat, inside the newline gap that belongs to row 0.
        val inGapY = seatRect.bottom + view.parameters.seatNewlineGap / 2f

        assertThat(view.positionAt(seatRect.centerX(), inGapY))
            .isEqualTo(SeatPosition(row = 0, column = 0))
    }

    @Test
    fun positionAt_tapInColumnGapResolvesToOwningSeat() {
        val view = newSeatView()
        view.layoutForTest()
        view.initSeatView(buildGrid(cols = 5, rows = 5))

        val seatRect = view.seatRect(SeatPosition(row = 1, column = 1))!!
        val inGapX = seatRect.right + view.parameters.seatInlineGap / 2f

        assertThat(view.positionAt(inGapX, seatRect.centerY()))
            .isEqualTo(SeatPosition(row = 1, column = 1))
    }

    @Test
    fun positionAt_outsideGridStillReturnsNull() {
        val view = newSeatView()
        view.layoutForTest()
        view.initSeatView(buildGrid(cols = 4, rows = 4))

        assertThat(view.positionAt(1f, 1f)).isNull()
    }

    // #4 — a config supplied after layout must drive the window rect.
    @Test
    fun initSeatView_afterLayout_recomputesWindowRectFromNewConfig() {
        val view = newSeatView()
        view.layoutForTest(w = 1000, h = 2000) // first layout uses the default 50px paddings
        assertThat(view.parameters.windowRectF.top).isWithin(0.01f).of(50f)

        view.initSeatView(
            buildGrid(cols = 4, rows = 4),
            SeatViewConfig(topPadding = 300f, leftPadding = 0f),
        )

        assertThat(view.parameters.windowRectF.top).isWithin(0.01f).of(300f)
        assertThat(view.parameters.windowRectF.left).isWithin(0.01f).of(0f)
        // ...and the grid is centered inside that new window, not the stale one.
        assertThat(view.parameters.virtualRectF.centerY())
            .isWithin(0.5f).of(view.parameters.windowRectF.centerY())
    }

    // #5 — extension init() must precede its first draw(), even when seats arrive late.
    @Test
    fun extension_initRunsBeforeFirstDraw_whenGridArrivesAfterLayout() {
        val view = newSeatView()
        val extension = RecordingExtension()
        view.extensions.add(extension)

        view.layoutForTest()          // laid out with no grid yet
        draw(view)                    // used to draw with init() never having run

        assertThat(extension.drawsBeforeInit).isEqualTo(0)
        assertThat(extension.initCount).isEqualTo(1)
        assertThat(extension.drawCount).isAtLeast(1)
    }

    @Test
    fun extension_initRunsBeforeFirstDraw_whenInitialisedBeforeLayout() {
        val view = newSeatView()
        val extension = RecordingExtension()
        view.extensions.add(extension)

        view.initSeatView(buildGrid(cols = 3, rows = 3))
        view.layoutForTest()
        draw(view)

        assertThat(extension.drawsBeforeInit).isEqualTo(0)
        assertThat(extension.initCount).isEqualTo(1)
    }

    @Test
    fun extension_isReinitialisedWhenConfigChanges() {
        val view = newSeatView()
        val extension = RecordingExtension()
        view.extensions.add(extension)
        view.layoutForTest()
        view.initSeatView(buildGrid(cols = 3, rows = 3))
        draw(view)
        // Assert the contract (init happened, and never after a draw), not an exact count:
        // the pre-grid layout and the populated grid are legitimately two different inits.
        val afterFirst = extension.initCount
        assertThat(afterFirst).isAtLeast(1)
        assertThat(extension.drawsBeforeInit).isEqualTo(0)

        view.initSeatView(buildGrid(cols = 3, rows = 3), SeatViewConfig(topPadding = 120f))
        draw(view)

        assertThat(extension.initCount).isGreaterThan(afterFirst)
        assertThat(extension.drawsBeforeInit).isEqualTo(0)
    }

    // xhigh #2 — an extension added after the first init must still get init() before draw().
    @Test
    fun extension_addedAfterFirstDraw_isStillInitialised() {
        val view = newSeatView()
        view.layoutForTest()
        view.initSeatView(buildGrid(cols = 3, rows = 3))
        draw(view)

        val late = RecordingExtension()
        view.extensions.add(late) // consumer toggles an overlay on
        draw(view)

        assertThat(late.drawsBeforeInit).isEqualTo(0)
        assertThat(late.initCount).isEqualTo(1)
    }

    // xhigh #3 — a view smaller than its paddings has an empty window rect; init must still run.
    @Test
    fun extension_isInitialised_evenWhenWindowRectIsEmpty() {
        val view = newSeatView()
        val extension = RecordingExtension()
        view.extensions.add(extension)

        view.layoutForTest(w = 60, h = 60) // smaller than 50+50 padding -> empty window rect
        view.initSeatView(buildGrid(cols = 2, rows = 2))
        draw(view, w = 60, h = 60)

        assertThat(extension.drawsBeforeInit).isEqualTo(0)
        assertThat(extension.initCount).isAtLeast(1)
    }

    // xhigh #14 — a different-sized grid with an equal config must re-run init().
    @Test
    fun extension_isReinitialisedWhenGridSizeChanges() {
        val view = newSeatView()
        val extension = RecordingExtension()
        view.extensions.add(extension)
        view.layoutForTest()

        val sameConfig = SeatViewConfig(topPadding = 100f)
        view.initSeatView(buildGrid(cols = 3, rows = 3), sameConfig)
        draw(view)
        val afterFirst = extension.initCount

        // Equal-by-value config, different grid: a size-dependent cache must be rebuilt.
        view.initSeatView(buildGrid(cols = 8, rows = 9), SeatViewConfig(topPadding = 100f))
        draw(view)

        assertThat(extension.initCount).isGreaterThan(afterFirst)
        assertThat(extension.drawsBeforeInit).isEqualTo(0)
    }

    // xhigh #5 — a merged seat wider than one cell pitch must be tappable across its whole rect.
    @Test
    fun positionAt_resolvesWideMergedSeatAcrossItsFullWidth() {
        val view = newSeatView()
        view.layoutForTest()
        // Column 0 spans 3 pitches; columns 1 and 2 are invisible filler cells.
        val wide = TestSeat("wide")
        view.initSeatView(
            arrayOf(arrayOf(wide, TestSeat("f1", visible = false), TestSeat("f2", visible = false)))
        )
        val pitch = view.parameters.seatWidth + view.parameters.seatInlineGap
        wide.rightAdditionPx = 2 * pitch // now that the real seat width is known

        val rect = view.seatRect(SeatPosition(row = 0, column = 0))!!
        // A point inside the drawn right-hand third — used to resolve to null.
        val x = rect.right - view.parameters.seatWidth / 2f

        assertThat(view.positionAt(x, rect.centerY()))
            .isEqualTo(SeatPosition(row = 0, column = 0))
    }

    // xhigh #12 — taps outside the painted window must not resolve to culled seats.
    @Test
    fun positionAt_rejectsTouchesOutsideThePaintedWindow() {
        val view = newSeatView()
        view.layoutForTest(w = 1000, h = 1000)
        view.initSeatView(buildGrid(cols = 3, rows = 3))

        val window = view.parameters.windowRectF
        // Inside the view but inside the config padding strip, i.e. never painted.
        assertThat(view.positionAt(window.left - 10f, window.centerY())).isNull()
        assertThat(view.positionAt(window.centerX(), window.top - 10f)).isNull()
    }

    // xhigh #4 — the a11y framework may query ids before the grid exists.
    @Test
    fun positionForVirtualId_returnsNullBeforeInit() {
        val view = newSeatView()

        assertThat(view.positionForVirtualId(0)).isNull()
        assertThat(view.positionForVirtualId(42)).isNull()
    }

    // xhigh #11 — a zero width:height ratio is a divisor and must be rejected.
    @Test
    fun initSeatView_zeroWidthHeightRatio_throws() {
        val error = runCatching {
            newSeatView().initSeatView(
                buildGrid(cols = 3, rows = 3),
                SeatViewConfig(seatWidthHeightRatio = 0f),
            )
        }.exceptionOrNull()

        assertThat(error).isInstanceOf(IllegalArgumentException::class.java)
        assertThat(error!!).hasMessageThat().contains("seatWidthHeightRatio")
    }

    // #6 — restore must drop foreign ids and announce the result once.
    @Test
    fun restore_dropsIdsAbsentFromGridAndNotifiesListener() {
        val source = newSeatView()
        source.initSeatView(buildGrid(cols = 3, rows = 1)) // ids 0-0, 1-0, 2-0
        source.selectSeat("0-0")
        source.selectSeat("2-0")
        val saved = source.onSaveInstanceState()

        // Target grid only contains 0-0 and 1-0, so 2-0 must not survive the restore.
        val target = newSeatView()
        val listener = RecordingListener()
        target.seatViewListener = listener
        target.initSeatView(arrayOf(arrayOf(TestSeat("0-0"), TestSeat("1-0"))))
        target.onRestoreInstanceState(saved)

        assertThat(target.selectedSeatIds).containsExactly("0-0")
        assertThat(listener.restored).containsExactly(setOf("0-0"))
        // seatSelected models user intent and must NOT be synthesised by a restore.
        assertThat(listener.selected).isEmpty()
    }

    @Test
    fun restore_announcesSelectionSoConsumerStateStaysInSync() {
        val source = newSeatView()
        source.initSeatView(buildGrid(cols = 3, rows = 2))
        source.selectSeat("1-1")
        source.selectSeat("2-0")
        val saved = source.onSaveInstanceState()

        val target = newSeatView()
        val listener = RecordingListener()
        target.seatViewListener = listener
        target.onRestoreInstanceState(saved)      // state first...
        target.initSeatView(buildGrid(cols = 3, rows = 2)) // ...grid second

        assertThat(target.selectedSeatIds).containsExactly("1-1", "2-0")
        assertThat(listener.restored).containsExactly(setOf("1-1", "2-0"))
    }

    // xhigh #1 — a connected group must produce ONE restore callback, not one per member.
    @Test
    fun restore_firesOnceForAConnectedMultiSeatGroup() {
        val couch = arrayOf("0-0", "1-0")
        fun grid() = arrayOf(
            arrayOf(TestSeat("0-0", connectedIds = couch), TestSeat("1-0", connectedIds = couch))
        )
        val source = newSeatView()
        source.seatViewListener = RecordingListener()
        source.initSeatView(grid())
        source.selectSeat("0-0")
        assertThat(source.selectedSeatIds).containsExactly("0-0", "1-0")
        val saved = source.onSaveInstanceState()

        val target = newSeatView()
        val listener = RecordingListener()
        target.seatViewListener = listener
        target.initSeatView(grid())
        target.onRestoreInstanceState(saved)

        assertThat(target.selectedSeatIds).containsExactly("0-0", "1-0")
        assertThat(listener.restored).hasSize(1) // was 2 callbacks -> double-counted price
        assertThat(listener.restored.single()).containsExactly("0-0", "1-0")
    }

    // xhigh #15 — a listener assigned AFTER initSeatView must still receive the restore.
    @Test
    fun restore_isDeliveredToAListenerAssignedAfterInit() {
        val source = newSeatView()
        source.initSeatView(buildGrid(cols = 3, rows = 2))
        source.selectSeat("1-1")
        val saved = source.onSaveInstanceState()

        val target = newSeatView()
        target.onRestoreInstanceState(saved)
        target.initSeatView(buildGrid(cols = 3, rows = 2)) // restore applies here, no listener yet

        val listener = RecordingListener()
        target.seatViewListener = listener                 // ...assigned only now

        assertThat(listener.restored).containsExactly(setOf("1-1"))
    }

    // xhigh #6 — an id that is now an invisible gap must not re-enter the selection.
    @Test
    fun restore_dropsIdsThatAreNowInvisible() {
        val source = newSeatView()
        source.initSeatView(buildGrid(cols = 3, rows = 1))
        source.selectSeat("1-0")
        val saved = source.onSaveInstanceState()

        val target = newSeatView()
        target.seatViewListener = RecordingListener()
        target.initSeatView(buildGrid(cols = 3, rows = 1) { x, _ ->
            TestSeat("$x-0", visible = x != 1) // 1-0 became a gap
        })
        target.onRestoreInstanceState(saved)

        assertThat(target.selectedSeatIds).isEmpty()
    }

    // xhigh #7 — an id that is no longer selectable (e.g. sold meanwhile) must be dropped.
    @Test
    fun restore_dropsIdsThatAreNoLongerSelectable() {
        val source = newSeatView()
        source.initSeatView(buildGrid(cols = 3, rows = 1))
        source.selectSeat("2-0")
        val saved = source.onSaveInstanceState()

        val target = newSeatView()
        target.seatViewListener = RecordingListener()
        target.initSeatView(buildGrid(cols = 3, rows = 1) { x, _ ->
            TestSeat("$x-0", selectable = x != 2) // 2-0 got sold while backgrounded
        })
        target.onRestoreInstanceState(saved)

        assertThat(target.selectedSeatIds).isEmpty()
    }

    // xhigh #8 — a second save before the grid arrives must carry the pending selection.
    @Test
    fun save_carriesForwardAnUnappliedPendingSelection() {
        val source = newSeatView()
        source.initSeatView(buildGrid(cols = 3, rows = 1))
        source.selectSeat("0-0")
        val firstSave = source.onSaveInstanceState()

        // Grid has not arrived yet, so the restore stays pending...
        val middle = newSeatView()
        middle.onRestoreInstanceState(firstSave)
        val secondSave = middle.onSaveInstanceState() // ...and must not be lost here

        val target = newSeatView()
        val listener = RecordingListener()
        target.seatViewListener = listener
        target.onRestoreInstanceState(secondSave)
        target.initSeatView(buildGrid(cols = 3, rows = 1))

        assertThat(target.selectedSeatIds).containsExactly("0-0")
    }

    // xhigh #9 — clearing before the grid arrives must not be undone by the grid arriving.
    @Test
    fun clearSelection_discardsAnUnappliedPendingSelection() {
        val source = newSeatView()
        source.initSeatView(buildGrid(cols = 3, rows = 1))
        source.selectSeat("0-0")
        val saved = source.onSaveInstanceState()

        val target = newSeatView()
        val listener = RecordingListener()
        target.seatViewListener = listener
        target.onRestoreInstanceState(saved) // pending, grid not loaded
        target.clearSelection()              // user taps "clear all" while loading
        target.initSeatView(buildGrid(cols = 3, rows = 1))

        assertThat(target.selectedSeatIds).isEmpty()
        assertThat(listener.restored).isEmpty()
    }

    // #8 — selectedSeatIds must be a snapshot, not the live internal set.
    @Test
    fun selectedSeatIds_isSnapshot_soReleasingWhileIteratingWorks() {
        val view = newSeatView()
        view.seatViewListener = RecordingListener()
        view.initSeatView(buildGrid(cols = 3, rows = 3))
        view.selectSeat("0-0")
        view.selectSeat("1-1")
        view.selectSeat("2-2")

        // The obvious consumer idiom: used to throw ConcurrentModificationException.
        view.selectedSeatIds.forEach { view.releaseSeat(it) }

        assertThat(view.selectedSeatIds).isEmpty()
    }

    @Test
    fun selectedSeatIds_snapshotDoesNotChangeAfterFurtherSelection() {
        val view = newSeatView()
        view.initSeatView(buildGrid(cols = 3, rows = 3))
        view.selectSeat("0-0")

        val snapshot = view.selectedSeatIds
        view.selectSeat("1-1")

        assertThat(snapshot).containsExactly("0-0") // snapshot is frozen
        assertThat(view.selectedSeatIds).containsExactly("0-0", "1-1")
    }

    // #9 — invisible grid gaps must never enter the selection.
    @Test
    fun selectSeat_invisibleSeat_isRejected() {
        val grid = buildGrid(cols = 3, rows = 3) { x, y ->
            TestSeat("$x-$y", visible = !(x == 1 && y == 1))
        }
        val view = newSeatView()
        val listener = RecordingListener()
        view.seatViewListener = listener
        view.initSeatView(grid)

        assertThat(view.selectSeat(SeatPosition(row = 1, column = 1))).isNull()
        assertThat(view.selectSeat("1-1")).isNull()

        assertThat(view.selectedSeatIds).isEmpty()
        assertThat(listener.selected).isEmpty()
    }

    @Test
    fun preSelection_skipsInvisibleSeats() {
        val grid = buildGrid(cols = 3, rows = 1) { x, _ ->
            TestSeat("$x-0", visible = x != 1, preSelected = true)
        }
        val view = newSeatView()
        view.initSeatView(grid)

        assertThat(view.selectedSeatIds).containsExactly("0-0", "2-0")
    }
}

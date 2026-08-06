package com.murgupluoglu.seatview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.ViewCompat
import com.murgupluoglu.seatview.extensions.SeatViewExtension
import com.murgupluoglu.seatview.seatdrawer.NumberSeatDrawer
import com.murgupluoglu.seatview.seatdrawer.SeatDrawScope
import com.murgupluoglu.seatview.seatdrawer.SeatDrawer
import java.util.Collections
import java.util.IdentityHashMap
import kotlin.math.floor

class SeatView<SEAT : Seat> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // region public API
    var seatDrawer: SeatDrawer = NumberSeatDrawer()

    val extensions = arrayListOf<SeatViewExtension>()

    /** Sentinel used to detect "no listener assigned yet"; must be declared before the property. */
    private val noOp: SeatViewListener<SEAT> = object : SeatViewListener<SEAT> {}

    /**
     * Interaction listener. Optional — defaults to a no-op, so the view never crashes if unset.
     * Assigning it flushes any pending [SeatViewListener.selectionRestored] notification, so a
     * listener assigned *after* [initSeatView] still learns about a restored selection.
     */
    var seatViewListener: SeatViewListener<SEAT> = noOp
        set(value) {
            field = value
            flushRestoreNotification()
        }

    /**
     * Whether pinch-to-zoom is enabled. Defaults to true and is **never** reset by
     * [initSeatView] — this is the single source of truth for zoom.
     */
    var isZoomEnabled: Boolean = true

    /** Rows / columns in the current grid (0 before [initSeatView]). */
    val rowCount: Int get() = parameters.ySize
    val columnCount: Int get() = parameters.xSize

    /**
     * Snapshot of the currently selected seat ids (including connected ids). Each read returns
     * an independent copy, so callers can safely select/release while iterating it.
     */
    val selectedSeatIds: Set<String> get() = selection.selectedIds.toSet()
    // endregion

    // region internal state (exposed to drawers/extensions, not to consumers)
    internal var config = SeatViewConfig()
        private set
    internal val parameters = SeatViewParameters()
    private lateinit var seats: Array<Array<SEAT>>

    private val selection = SeatSelection<SEAT>()
    private val viewport = SeatViewport(parameters)
    private val accessibilityHelper = SeatViewAccessibilityHelper(this)

    /** Selection restored from instance state, applied once the grid is set. */
    private var pendingSelection: List<String>? = null

    /** Restored ids awaiting a [SeatViewListener.selectionRestored] delivery. */
    private var pendingRestoreNotification: Set<String>? = null

    /**
     * Extensions are init()-ed before their first draw(). Initialization is tracked
     * **per extension instance** (so one added later is still initialized) and reset whenever
     * the config, window rect or grid size changes (so cached, size-dependent state is rebuilt).
     */
    private data class ExtensionInitKey(
        val config: SeatViewConfig,
        val window: RectF,
        val columns: Int,
        val rows: Int,
    )

    private var extensionInitKey: ExtensionInitKey? = null
    private val initializedExtensions =
        Collections.newSetFromMap(IdentityHashMap<SeatViewExtension, Boolean>())

    /** Set when a pan/zoom moved the viewport, so accessibility bounds are refreshed once. */
    private var viewportMoved = false
    // endregion

    init {
        ViewCompat.setAccessibilityDelegate(this, accessibilityHelper)
    }

    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                viewport.pan(distanceX, distanceY)
                viewportMoved = true
                invalidate()
                return true
            }

            override fun onDown(e: MotionEvent): Boolean = true

            override fun onSingleTapUp(event: MotionEvent): Boolean {
                val position = positionAt(event.x, event.y)
                if (position != null) {
                    val seat = getSeat(position.column, position.row)
                    if (seat.isVisible()) {
                        if (selection.isSelected(seat.id())) {
                            releaseSeat(position)
                        } else if (seatViewListener.canSelectSeat(seat, selectedSeatIds)) {
                            selectSeat(position)
                        }
                    }
                }
                return super.onSingleTapUp(event)
            }
        })

    fun initSeatView(
        list: Array<Array<SEAT>>,
        optionalConfig: SeatViewConfig = SeatViewConfig()
    ) {
        require(list.isNotEmpty() && list.first().isNotEmpty()) {
            "Seat list must not be empty"
        }
        val columnCount = list.first().size
        require(list.all { it.size == columnCount }) {
            "All rows must have the same number of columns (rectangular grid required)"
        }
        // Validate the size bounds up front: an inverted range would otherwise blow up
        // later inside the pinch handler, far from the offending call.
        require(optionalConfig.seatMinWidthDp <= optionalConfig.seatMaxWidthDp) {
            "seatMinWidthDp (${optionalConfig.seatMinWidthDp}) cannot be greater than " +
                "seatMaxWidthDp (${optionalConfig.seatMaxWidthDp})"
        }
        require(optionalConfig.seatDefaultWidthDp >= optionalConfig.seatMinWidthDp) {
            "seatDefaultWidthDp (${optionalConfig.seatDefaultWidthDp}) cannot be smaller than " +
                "seatMinWidthDp (${optionalConfig.seatMinWidthDp})"
        }
        require(optionalConfig.seatDefaultWidthDp <= optionalConfig.seatMaxWidthDp) {
            "seatDefaultWidthDp (${optionalConfig.seatDefaultWidthDp}) cannot be greater than " +
                "seatMaxWidthDp (${optionalConfig.seatMaxWidthDp})"
        }
        // Used as a divisor for seatHeight: 0f would silently produce Infinity/NaN geometry
        // that renders nothing yet still resolves taps.
        require(optionalConfig.seatWidthHeightRatio > 0f) {
            "seatWidthHeightRatio must be > 0 (was ${optionalConfig.seatWidthHeightRatio})"
        }
        require(optionalConfig.seatInlineGapWidthRatio >= 0f && optionalConfig.seatNewlineGapWidthRatio >= 0f) {
            "gap ratios cannot be negative"
        }

        config = optionalConfig
        seats = list
        parameters.xSize = columnCount
        parameters.ySize = list.size

        // Rebuild the selection from the grid's pre-selected seats.
        selection.initFrom(seats)

        val density = resources.displayMetrics.density
        parameters.apply {
            seatMinWidth = config.seatMinWidthDp.dpToPx(density)
            seatMaxWidth = config.seatMaxWidthDp.dpToPx(density)
            seatWidth = config.seatDefaultWidthDp.dpToPx(density)
            seatHeight = seatWidth / config.seatWidthHeightRatio
            seatInlineGap = seatWidth * config.seatInlineGapWidthRatio
            seatNewlineGap = seatWidth * config.seatNewlineGapWidthRatio
        }

        // A selection restored from instance state wins over the grid's defaults.
        applyPendingSelection()

        // If the view is already laid out, rebuild the layout immediately; otherwise
        // onSizeChanged() will build it on the first layout pass. The window rect must be
        // recomputed here too, because it is derived from the (possibly new) config paddings.
        if (width > 0 && height > 0) {
            updateWindowRect(width, height)
            initParameters()
        }
        accessibilityHelper.invalidateRoot()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updateWindowRect(w, h)
        if (::seats.isInitialized) {
            initParameters()
        } else {
            // No grid yet, but the window is known: honor the documented contract that an
            // extension's init() runs before its first draw().
            ensureExtensionsInitialized()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(config.backgroundColor)
        drawSeats(canvas)

        // Guarantees init() ran even when the grid arrives after the first draw pass.
        ensureExtensionsInitialized()
        extensions.forEach {
            if (it.isActive()) {
                it.draw(canvas, parameters, config)
            }
        }
    }

    private fun drawSeats(canvas: Canvas) {
        if (!::seats.isInitialized) return
        parameters.apply {
            for (row in 0 until ySize) {
                for (column in 0 until xSize) {
                    val seat = getSeat(column, row)
                    val seatRectF = getSeatRect(column, row, seat)

                    if (seatRectF.right < windowRectF.left || seatRectF.left > windowRectF.right
                        || seatRectF.top > windowRectF.bottom || seatRectF.bottom < windowRectF.top
                    ) {
                        continue
                    }

                    if (seat.isVisible()) {
                        seatDrawer.draw(
                            SeatDrawScope(
                                context = context,
                                parameters = parameters,
                                config = config,
                                canvas = canvas,
                                seat = seat,
                                seatRect = seatRectF,
                                isSelected = selection.isSelected(seat.id()),
                            )
                        )
                    }
                }
            }
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            parameters.apply {
                // No grid yet (async load): every width is 0f, so scaling would compute
                // 0f/0f = NaN and permanently corrupt virtualRectF.
                if (!::seats.isInitialized) return true
                if (seatWidth <= 0f || seatMinWidth <= 0f || seatMaxWidth <= 0f) return true
                // Defensive: an inverted range is rejected by initSeatView, but never let a
                // gesture throw if one somehow reaches here.
                if (seatMinWidth > seatMaxWidth) return true

                // Clamp the incremental scale so the seat width stays within
                // [seatMinWidth, seatMaxWidth]; at a boundary the factor becomes 1f.
                val clampedWidth = (seatWidth * detector.scaleFactor)
                    .coerceIn(seatMinWidth, seatMaxWidth)
                val factor = clampedWidth / seatWidth
                if (factor == 1f) return true

                seatWidth = clampedWidth
                seatHeight = seatWidth / config.seatWidthHeightRatio
                seatInlineGap = seatWidth * config.seatInlineGapWidthRatio
                seatNewlineGap = seatWidth * config.seatNewlineGapWidthRatio

                // Zoom around the pinch focal point, then keep the grid on-screen.
                val matrix = Matrix()
                matrix.postScale(factor, factor, detector.focusX, detector.focusY)
                matrix.mapRect(virtualRectF)
                viewport.clamp()

                viewportMoved = true
                this@SeatView.postInvalidateOnAnimation()
            }
            return true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount == 1) {
            gestureDetector.onTouchEvent(event)
        } else if (isZoomEnabled) {
            scaleDetector.onTouchEvent(event)
        }

        // Seat bounds changed during a pan/zoom: refresh the accessibility node tree once,
        // when the gesture ends, rather than on every frame.
        if (event.actionMasked == MotionEvent.ACTION_UP ||
            event.actionMasked == MotionEvent.ACTION_CANCEL
        ) {
            if (viewportMoved) {
                viewportMoved = false
                accessibilityHelper.invalidateRoot()
            }
        }
        return true
    }

    override fun dispatchHoverEvent(event: MotionEvent): Boolean {
        return accessibilityHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event)
    }

    private fun updateWindowRect(w: Int, h: Int) {
        parameters.windowRectF = RectF(
            config.leftPadding,
            config.topPadding,
            w.toFloat() - config.rightPadding,
            h.toFloat() - config.bottomPadding
        )
    }

    private fun initParameters() {
        viewport.rebuildVirtualRect()
        ensureExtensionsInitialized()
    }

    /**
     * Guarantees `init()` runs before an extension's first `draw()`. Deliberately does **not**
     * skip on a degenerate (empty) window rect: `onDraw` draws extensions unconditionally, so
     * skipping here would break the documented contract for views smaller than their paddings.
     */
    private fun ensureExtensionsInitialized() {
        if (extensions.isEmpty()) return

        val key = ExtensionInitKey(
            config = config,
            window = RectF(parameters.windowRectF),
            columns = parameters.xSize,
            rows = parameters.ySize,
        )
        if (extensionInitKey != key) {
            // Config, viewport or grid size changed: everything must be re-initialized.
            initializedExtensions.clear()
            extensionInitKey = key
        }

        // Per-instance tracking, so an extension added after the first init is still covered.
        extensions.forEach { extension ->
            if (initializedExtensions.add(extension)) {
                extension.init(parameters, config)
            }
        }
    }

    /**
     * Get the draw rect for a cell, honoring the seat's per-seat rect additions
     * (used to visually merge multi-seats). Coordinates are in view/screen space.
     */
    private fun getSeatRect(column: Int, row: Int, seatBean: Seat): RectF {
        parameters.apply {
            val left = virtualRectF.left + (column * (seatWidth + seatInlineGap)) +
                seatBean.leftRectAddition(seatInlineGap)
            val right = left + seatWidth + seatBean.rightRectAddition(seatInlineGap)
            val top = virtualRectF.top + (row * (seatHeight + seatNewlineGap))
            val bottom = top + seatHeight
            return RectF(left, top, right, bottom)
        }
    }

    // region public grid & selection API

    /** The seat at [position], or null if the position is outside the grid. */
    fun seatAt(position: SeatPosition): SEAT? =
        if (isValid(position.column, position.row)) getSeat(position.column, position.row) else null

    /** The position of the seat whose [Seat.id] equals [id], or null if none/uninitialized. */
    fun positionOf(id: String): SeatPosition? {
        if (!::seats.isInitialized) return null
        seats.forEachIndexed { row, columns ->
            columns.forEachIndexed { column, seat ->
                if (seat.id() == id) return SeatPosition(row, column)
            }
        }
        return null
    }

    /** The on-screen rect of the seat at [position] (e.g. to anchor a tooltip), or null. */
    fun seatRect(position: SeatPosition): RectF? {
        val seat = seatAt(position) ?: return null
        return RectF(getSeatRect(position.column, position.row, seat))
    }

    fun isSelected(id: String): Boolean = selection.isSelected(id)

    /**
     * Selects the seat at [position].
     * @return the seat, or null if the position is invalid, the seat is invisible or not
     * selectable, or it was already selected.
     */
    fun selectSeat(position: SeatPosition): SEAT? {
        val seat = seatAt(position) ?: return null
        if (!seat.isVisible()) return null
        if (!seat.canSelect()) return null
        if (!selection.select(seat)) return null
        seatViewListener.seatSelected(seat, selectedSeatIds)
        invalidateAccessibilityFor(seat, position)
        invalidate()
        return seat
    }

    /** Selects the seat with [id]. @return the seat, or null if it could not be selected. */
    fun selectSeat(id: String): SEAT? = positionOf(id)?.let { selectSeat(it) }

    /** Releases the seat at [position]. @return true if it was selected and is now released. */
    fun releaseSeat(position: SeatPosition): Boolean {
        val seat = seatAt(position) ?: return false
        if (!selection.release(seat)) return false
        seatViewListener.seatReleased(seat, selectedSeatIds)
        invalidateAccessibilityFor(seat, position)
        invalidate()
        return true
    }

    /** Releases the seat with [id]. @return true if it was selected and is now released. */
    fun releaseSeat(id: String): Boolean = positionOf(id)?.let { releaseSeat(it) } ?: false

    /**
     * Clears the entire selection (no per-seat callbacks are fired). Also discards a selection
     * restored from instance state that has not been applied yet, so clearing while the grid is
     * still loading cannot be undone by the grid arriving later.
     */
    fun clearSelection() {
        val hadPending = pendingSelection != null || pendingRestoreNotification != null
        pendingSelection = null
        pendingRestoreNotification = null
        if (selection.clear() || hadPending) {
            accessibilityHelper.invalidateRoot()
            invalidate()
        }
    }

    // endregion

    // region internals shared with the accessibility helper

    internal fun getSeat(column: Int, row: Int): SEAT = seats[row][column]

    internal fun isValid(column: Int, row: Int): Boolean =
        column in 0 until parameters.xSize && row in 0 until parameters.ySize

    internal fun virtualIdOf(position: SeatPosition): Int =
        position.row * columnCount + position.column

    /**
     * Null when the grid is not set yet (or the id predates a smaller grid) — the accessibility
     * framework may query arbitrary/stale virtual ids, and `columnCount` is 0 before init.
     */
    internal fun positionForVirtualId(id: Int): SeatPosition? {
        val columns = columnCount
        if (columns <= 0 || id < 0) return null
        return SeatPosition(row = id / columns, column = id % columns)
    }

    /**
     * Maps a touch pixel to the seat under it. The seat's own draw rect wins (so per-seat
     * rect additions on merged multi-seats resolve exactly); a touch landing in the gap
     * between seats falls back to the cell that owns that gap, which keeps the effective
     * touch target one full pitch wide instead of shrinking it to the drawn rect.
     */
    internal fun positionAt(touchX: Float, touchY: Float): SeatPosition? {
        if (!::seats.isInitialized) return null
        parameters.apply {
            if (seatWidth <= 0f || seatHeight <= 0f) return null

            // Only the window is painted (drawSeats culls against it), so a touch outside it
            // must not resolve to a seat the user never saw.
            if (!windowRectF.contains(touchX, touchY)) return null

            val virtualX = touchX - virtualRectF.left
            val virtualY = touchY - virtualRectF.top
            if (virtualX < 0 || virtualX > virtualRectF.width() ||
                virtualY < 0 || virtualY > virtualRectF.height()
            ) {
                return null
            }

            val approxColumn = floor((virtualX / (seatWidth + seatInlineGap)).toDouble()).toInt()
            val approxRow = floor((virtualY / (seatHeight + seatNewlineGap)).toDouble()).toInt()
            if (!isValid(approxColumn, approxRow)) return null

            // Scan the whole row rather than a fixed +/-1 window: a merged seat's rect additions
            // can span several cell pitches, so its drawn area may start many columns away.
            // This runs once per tap, so the linear scan is not on a hot path.
            for (column in 0 until xSize) {
                val seat = getSeat(column, approxRow)
                if (!seat.isVisible()) continue
                if (getSeatRect(column, approxRow, seat).contains(touchX, touchY)) {
                    return SeatPosition(approxRow, column)
                }
            }

            // Gap fallback: the pitch-based cell owns the surrounding gap.
            val approx = SeatPosition(approxRow, approxColumn)
            return if (getSeat(approxColumn, approxRow).isVisible()) approx else null
        }
    }

    /** Tap-equivalent toggle used by the accessibility CLICK action. */
    internal fun toggleForAccessibility(position: SeatPosition) {
        val seat = seatAt(position) ?: return
        if (!seat.isVisible()) return
        if (selection.isSelected(seat.id())) {
            releaseSeat(position)
        } else if (seatViewListener.canSelectSeat(seat, selectedSeatIds)) {
            selectSeat(position)
        }
    }

    /**
     * Invalidates the accessibility node of [seat] plus every connected seat, whose
     * checked/selected state flips together with it.
     */
    private fun invalidateAccessibilityFor(seat: SEAT, position: SeatPosition) {
        accessibilityHelper.invalidateVirtualView(virtualIdOf(position))
        seat.allConnectedSeatIds().forEach { id ->
            if (id != seat.id()) {
                positionOf(id)?.let { accessibilityHelper.invalidateVirtualView(virtualIdOf(it)) }
            }
        }
    }

    // endregion

    // region instance state (survives rotation)

    public override fun onSaveInstanceState(): Parcelable =
        SavedState(super.onSaveInstanceState()).apply {
            // A restore that has not been applied yet (grid still loading) must be carried
            // forward, otherwise a second save/restore round trip loses the user's selection.
            selectedIds = pendingSelection?.let { ArrayList(it) }
                ?: ArrayList(selection.selectedIds)
        }

    public override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            pendingSelection = state.selectedIds
            if (::seats.isInitialized) {
                applyPendingSelection()
                invalidate()
            }
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    /**
     * Applies a selection restored from instance state. An id only survives if it belongs to a
     * seat that is currently **visible and selectable**, or is a connected id of such a seat —
     * the same bar [selectSeat] enforces, so a restore can never produce a selection the user
     * could not have made (or cannot release).
     *
     * The result is announced once via [SeatViewListener.selectionRestored], queued until a
     * listener is actually assigned.
     */
    private fun applyPendingSelection() {
        val pending = pendingSelection ?: return
        pendingSelection = null

        val restorable = HashSet<String>()
        seats.forEach { row ->
            row.forEach { seat ->
                if (seat.isVisible() && seat.canSelect()) {
                    restorable.add(seat.id())
                    seat.allConnectedSeatIds().forEach { restorable.add(it) }
                }
            }
        }
        selection.restore(pending.filter { restorable.contains(it) })
        accessibilityHelper.invalidateRoot()

        pendingRestoreNotification = selectedSeatIds
        flushRestoreNotification()
    }

    /** Delivers a queued restore notification once a real listener exists. */
    private fun flushRestoreNotification() {
        val restored = pendingRestoreNotification ?: return
        if (seatViewListener === noOp) return // wait for a real listener
        pendingRestoreNotification = null
        seatViewListener.selectionRestored(restored)
    }

    /**
     * Must not be `private`: a Kotlin `private` nested class compiles to a package-private
     * JVM class, and [Parcel] resolves `CREATOR` reflectively from `android.os`, which can
     * fail with `BadParcelableException` on stricter runtimes.
     */
    internal class SavedState : View.BaseSavedState {
        var selectedIds: List<String> = emptyList()

        constructor(superState: Parcelable?) : super(superState)

        private constructor(source: Parcel) : super(source) {
            selectedIds = source.createStringArrayList() ?: emptyList()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeStringList(selectedIds)
        }

        companion object {
            @JvmField
            val CREATOR = object : Parcelable.Creator<SavedState> {
                override fun createFromParcel(source: Parcel): SavedState = SavedState(source)
                override fun newArray(size: Int): Array<SavedState?> = arrayOfNulls(size)
            }
        }
    }

    // endregion
}

private fun Float.dpToPx(density: Float): Float = this * density + 0.5f

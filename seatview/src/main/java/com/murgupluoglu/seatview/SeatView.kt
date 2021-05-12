package com.murgupluoglu.seatview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.ViewCompat
import com.murgupluoglu.seatview.extensions.SeatViewExtension
import com.murgupluoglu.seatview.seatdrawer.CachedSeatDrawer
import com.murgupluoglu.seatview.seatdrawer.SeatDrawer
import java.util.*
import kotlin.math.abs
import kotlin.math.floor


class SeatView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //region public
    lateinit var windowRectF: RectF
        private set
    lateinit var virtualRectF: RectF
        private set

    var config = SeatViewConfig()
    var seatArray: Array<Array<Seat>> = arrayOf()
    val selectedSeats = HashMap<String, Seat>()
    var rowCount: Int = 0
    var columnCount: Int = 0
    var scaleFactorStart = 0f
    var scaleFactor = 0f
    lateinit var seatViewListener: SeatViewListener
    val extensions = arrayListOf<SeatViewExtension>()
    var seatDrawer: SeatDrawer = CachedSeatDrawer(context)
    //endregion

    //region private
    private var seatDefaultHeight: Float = config.seatDefaultWidth / config.seatWidthHeightRatio

    private var seatWidth = config.seatDefaultWidth
    private var seatHeight = seatDefaultHeight

    private var seatInlineGap: Float = seatWidth * config.seatInlineGapWidthRatio
    private var seatNewlineGap: Float = seatWidth * config.seatNewlineGapWidthRatio

    private var scaleDetector = ScaleGestureDetector(context, ScaleListener())

    private val gestureDetector =
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent,
                e2: MotionEvent,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                moveSeatView(distanceX, distanceY)
                invalidate()
                return true
            }

            override fun onDown(e: MotionEvent): Boolean {
                return true
            }

            override fun onSingleTapUp(event: MotionEvent): Boolean {

                val clickedSeat = getClickedSeat(event.x, event.y)
                if (clickedSeat != null) {
                    if (clickedSeat.type != Seat.TYPE.NOT_EXIST) {
                        if (selectedSeats[clickedSeat.id] != null) {
                            releaseSeat(clickedSeat.rowIndex, clickedSeat.columnIndex)
                        } else {
                            if (seatViewListener.canSelectSeat(clickedSeat, selectedSeats)) {
                                selectSeat(clickedSeat.rowIndex, clickedSeat.columnIndex)
                            }
                        }
                    }
                } else {
                    // Clicked blank area
                }
                return super.onSingleTapUp(event)
            }
        })
    //endregion private

    fun addSeatsInsideSelected(clickedSeat: Seat) {
        if (clickedSeat.multipleType == Seat.MULTIPLETYPE.NOTMULTIPLE) {
            clickedSeat.isSelected = true
            selectedSeats[clickedSeat.id!!] = clickedSeat
        } else {
            clickedSeat.multipleSeats.forEach {
                val oneSeatInsideMultipe = findSeatWithName(it)
                oneSeatInsideMultipe!!.isSelected = true
                selectedSeats[oneSeatInsideMultipe.id!!] = oneSeatInsideMultipe
            }
        }
    }

    fun removeSeatsInsideSelected(clickedSeat: Seat) {
        if (clickedSeat.multipleType == Seat.MULTIPLETYPE.NOTMULTIPLE) {
            clickedSeat.isSelected = false
            selectedSeats.remove(clickedSeat.id)
        } else {
            clickedSeat.multipleSeats.forEach {
                val oneSeatInsideMultipe = findSeatWithName(it)
                oneSeatInsideMultipe!!.isSelected = false
                selectedSeats.remove(oneSeatInsideMultipe.id)
            }
        }
    }

    fun initSeatView(_seatArray: Array<Array<Seat>>, _rowCount: Int, _columnCount: Int) {

        //add all pre-selected seat inside selectedSeats
        seatArray.forEachIndexed { rowIndex, arrayOfSeats ->
            arrayOfSeats.forEachIndexed { columnIndex, seat ->
                if (seat.isSelected) {
                    addSeatsInsideSelected(seat)
                }
            }
        }

        seatArray = _seatArray
        rowCount = _rowCount
        columnCount = _columnCount
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.e("onSizeChanged", w.toString())
        windowRectF = RectF(
            config.leftPadding,
            config.topPadding,
            w.toFloat() - config.rightPadding,
            h.toFloat() - config.bottomPadding
        )
        initParameters()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(config.backgroundColor)
        drawSeat(canvas)

        extensions.forEach {
            if (it.isActive()) {
                it.draw(this@SeatView, canvas)
            }
        }
    }

    private fun drawSeat(canvas: Canvas) {
        for (rowIndex in 0 until rowCount) {
            for (columnIndex in 0 until columnCount) {
                val seatBean = seatArray[rowIndex][columnIndex]

                val seatRectF = getSeatRect(rowIndex, columnIndex, seatBean)

                if (seatRectF.right < windowRectF.left || seatRectF.left > windowRectF.right
                    || seatRectF.top > windowRectF.bottom || seatRectF.bottom < windowRectF.top
                ) {
                    continue
                }

                if (seatBean.type != Seat.TYPE.NOT_EXIST) {

                    var calculatedSeatWidth = seatWidth
                    if (seatBean.multipleType == Seat.MULTIPLETYPE.LEFT || seatBean.multipleType == Seat.MULTIPLETYPE.RIGHT) {
                        calculatedSeatWidth = seatWidth + seatInlineGap / 2
                    } else if (seatBean.multipleType == Seat.MULTIPLETYPE.CENTER) {
                        calculatedSeatWidth = seatWidth + seatInlineGap
                    }

                    seatDrawer.draw(
                        seatView = this@SeatView,
                        canvas = canvas,
                        isInEditMode = isInEditMode,
                        seatBean = seatBean,
                        seatRectF = seatRectF,
                        seatWidth = calculatedSeatWidth,
                        seatHeight = seatHeight
                    )
                }
            }
        }
    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            scaleFactorStart = detector.scaleFactor
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {

            //TODO must be improved
            scaleFactor = detector.scaleFactor
            val newSeatWidth = seatWidth * scaleFactor

            if (newSeatWidth >= config.seatMinWidth && newSeatWidth <= config.seatMaxWidth) {
                seatWidth = newSeatWidth
                seatHeight = seatWidth / config.seatWidthHeightRatio
                seatInlineGap = seatWidth * config.seatInlineGapWidthRatio
                seatNewlineGap = seatWidth * config.seatNewlineGapWidthRatio


                val rawDifX = (windowRectF.centerX() - virtualRectF.centerX())
                val rawDifY = (windowRectF.centerY() - virtualRectF.centerY())
                var stepX = (windowRectF.centerX() - virtualRectF.centerX()) * 0.03f
                var stepY = (windowRectF.centerY() - virtualRectF.centerY()) * 0.03f

                if (abs(stepX) <= 4) {
                    stepX = rawDifX
                }

                if (abs(stepY) <= 4) {
                    stepY = rawDifY
                }

                var focusX = scaleDetector.focusX
                var focusY = scaleDetector.focusY

                if (!virtualRectF.contains(windowRectF)) { //Small items
                    focusX = windowRectF.centerX()
                    focusY = windowRectF.centerY()
                }

                //Log.e("stepX", stepX.toString())
                //Log.e("cal", if((scaleFactor - factor) > 0) "zoom-in" else "zoom-out")

                val matrix = Matrix()
                matrix.preScale(scaleFactor, scaleFactor, focusX, focusY)
                if ((scaleFactor - scaleFactorStart) < 0) { //zoom out
                    matrix.postTranslate(stepX, stepY)
                }
                //matrix.postTranslate(stepX, stepY)
                //matrix.setRectToRect(virtualRectF, windowRectF, Matrix.ScaleToFit.CENTER)
                matrix.mapRect(virtualRectF)

                //scale(virtualRectF, scaleFactor)
                //matrix.setRectToRect(virtualRectF, windowRectF, Matrix.ScaleToFit.START )


                ViewCompat.postInvalidateOnAnimation(this@SeatView)
            }

            return true
        }
    }

    private fun RectF.scale(factor: Float): RectF {
        val oldWidth = width()
        val oldHeight = height()
        val rectCenterX = left + oldWidth / 2F
        val rectCenterY = top + oldHeight / 2F
        val newWidth = oldWidth * factor
        val newHeight = oldHeight * factor
        val left = rectCenterX - newWidth / 2F
        val right = rectCenterX + newWidth / 2F
        val top = rectCenterY - newHeight / 2F
        val bottom = rectCenterY + newHeight / 2F
        return RectF(left, top, right, bottom)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.pointerCount == 1) {
            gestureDetector.onTouchEvent(event)
        } else if (config.isZoomActive) {
            scaleDetector.onTouchEvent(event)
        }
        return true
    }

    fun checkColumnIsEmpty(rowIndex: Int): Boolean {
        var isEmpty = true

        for (columnIndex in 0 until columnCount) {
            val seatBean = seatArray[rowIndex][columnIndex]
            if (seatBean.type != Seat.TYPE.NOT_EXIST) {
                isEmpty = false
            }
        }

        return isEmpty
    }

    fun isSeatSelected(rowIndex: Int, columnIndex: Int): Boolean {
        return if (isSafeSelect(rowIndex, columnIndex)) {
            seatArray[rowIndex][columnIndex].isSelected
        } else false
    }

    fun findSeatWithName(seatName: String): Seat? {
        seatArray.forEach { arrayOfRows ->
            arrayOfRows.forEach { seat ->
                if (seatName == seat.seatName) {
                    return seat
                }
            }
        }
        return null
    }

    fun selectSeat(rowIndex: Int, columnIndex: Int): Seat? {
        if (isSafeSelect(rowIndex, columnIndex)) {
            val seatBean = seatArray[rowIndex][columnIndex]
            if (seatBean.type != Seat.TYPE.NOT_EXIST && !seatBean.isSelected) {
                seatBean.isSelected = true
                addSeatsInsideSelected(seatBean)
                seatViewListener.seatSelected(seatBean, selectedSeats)
                invalidate()
                return seatBean
            }
        }
        return null
    }

    fun releaseSeat(rowIndex: Int, columnIndex: Int) {
        if (isSafeSelect(rowIndex, columnIndex)) {
            val seatBean = seatArray[rowIndex][columnIndex]
            if (seatBean.isSelected) {
                seatBean.isSelected = false
                removeSeatsInsideSelected(seatBean)
                seatViewListener.seatReleased(seatBean, selectedSeats)
                invalidate()
            }
        }
    }


    fun initParameters() {

        if (config.seatDefaultWidth < config.seatMinWidth) {
            throw Exception("seatDefaultWidth cannot be smaller than seatMinWidth")
        }

        val virtualWidth = columnCount * (seatWidth + seatInlineGap) - seatInlineGap
        val virtualHeight = rowCount * (seatHeight + seatNewlineGap) - seatNewlineGap

        val left = (windowRectF.centerX() - virtualWidth / 2)
        val top = (windowRectF.centerY() - virtualHeight / 2)

        virtualRectF = RectF(
            left,
            top,
            left + virtualWidth,
            top + virtualHeight
        )

        extensions.forEach {
            it.init(this@SeatView)
        }
    }

    /**
     * Get seat rect for given rowIndex and columnIndex
     * Seat rect is using for draw seat at correct place
     */
    private fun getSeatRect(rowIndex: Int, columnIndex: Int, seatBean: Seat): RectF {

        var left = virtualRectF.left + (columnIndex * (seatInlineGap + seatWidth))
        var right = left + seatWidth

        when (seatBean.multipleType) {
            Seat.MULTIPLETYPE.LEFT -> {
                right += seatInlineGap / 2
            }
            Seat.MULTIPLETYPE.RIGHT -> {
                left -= seatInlineGap / 2
            }
            Seat.MULTIPLETYPE.CENTER -> {
                left -= seatInlineGap / 2
                right += seatInlineGap / 2
            }
        }

        val top = virtualRectF.top + (rowIndex * (seatNewlineGap + seatHeight))
        val bottom = top + seatHeight

        return RectF(left, top, right, bottom)
    }

    private fun getClickedSeat(touchX: Float, touchY: Float): Seat? {

        val virtualX = touchX - virtualRectF.left
        val virtualY = touchY - virtualRectF.top
        if (virtualX < 0 || virtualX > virtualRectF.width()
            || virtualY < 0 || virtualY > virtualRectF.height()
        ) {
            //Touch outside of the seats
            return null
        } else {
            val columnIndex = floor((virtualX / (seatWidth + seatInlineGap)).toDouble()).toInt()
            val rowIndex = floor((virtualY / (seatHeight + seatNewlineGap)).toDouble()).toInt()
            return if (isSafeSelect(rowIndex, columnIndex)) getSeat(rowIndex, columnIndex) else null
        }
    }

    private fun moveSeatView(moveX: Float, moveY: Float) {

        val possibleRectF =
            RectF(virtualRectF.left, virtualRectF.top, virtualRectF.right, virtualRectF.bottom)
        possibleRectF.offset(-moveX, 0f)
        if (possibleRectF.left <= windowRectF.left && possibleRectF.right >= windowRectF.right) {
            virtualRectF.offset(-moveX, 0f)
        }
        possibleRectF.offset(0f, -moveY)
        if (possibleRectF.top <= windowRectF.top && possibleRectF.bottom >= windowRectF.bottom) {
            virtualRectF.offset(0f, -moveY)
        }
    }

    /**
     * Make sure seatArray contains given rowIndex and columnIndex
     */
    private fun isSafeSelect(rowIndex: Int, columnIndex: Int): Boolean {
        return rowIndex >= 0 && columnIndex >= 0 && rowIndex < rowCount && columnIndex < columnCount
    }

    /**
     * Get seat from given rowIndex and columnIndex
     */
    fun getSeat(rowIndex: Int, columnIndex: Int): Seat? {
        return if (isSafeSelect(rowIndex, columnIndex)) {
            seatArray[rowIndex][columnIndex]
        } else null
    }

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SeatView, 0, 0)

        if (a.hasValue(R.styleable.SeatView_seatViewBackgroundColor)) config.backgroundColor =
            Color.parseColor(a.getString(R.styleable.SeatView_seatViewBackgroundColor))

        a.recycle()

        if (isInEditMode) { //this is just for layout editor preview

            val sRandom = Random()

            val sRowCount = 10
            val sColumnCount = 10
            val sSeatArray = Array(sRowCount) { Array(sColumnCount) { Seat() } }

            sSeatArray.forEachIndexed { rowIndex, arrayOfSeats ->
                arrayOfSeats.forEachIndexed { columnIndex, seat ->
                    val rInteger = sRandom.nextInt(3)
                    if (rowIndex != 3) {
                        if (rInteger == Seat.TYPE.UNSELECTABLE) {
                            seat.type = Seat.TYPE.UNSELECTABLE
                            seat.drawableColor = "#e57373"
                        } else {
                            seat.type = Seat.TYPE.SELECTABLE
                            seat.drawableColor = "#64b5f6"
                        }
                    } else {
                        seat.type = Seat.TYPE.NOT_EXIST
                    }
                }
            }

            windowRectF = RectF(
                config.leftPadding,
                config.topPadding,
                width.toFloat() - config.rightPadding,
                height.toFloat() - config.bottomPadding
            )

            seatArray = sSeatArray
            rowCount = sRowCount
            columnCount = sColumnCount
            initParameters()
        }
    }

}
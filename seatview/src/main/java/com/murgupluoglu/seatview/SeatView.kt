package com.murgupluoglu.seatview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.core.view.ViewCompat
import com.murgupluoglu.seatview.extensions.SeatViewExtension
import com.murgupluoglu.seatview.seatdrawer.NumberSeatDrawer
import com.murgupluoglu.seatview.seatdrawer.SeatDrawer
import java.util.*
import kotlin.math.abs
import kotlin.math.floor


class SeatView<SEAT : Seat> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //region public
    var config = SeatViewConfig()
    val parameters = SeatViewParameters()
    lateinit var seats: Array<Array<SEAT>>
    lateinit var seatViewListener: SeatViewListener<SEAT>

    val extensions = arrayListOf<SeatViewExtension>()
    var seatDrawer: SeatDrawer = NumberSeatDrawer()
    val selectedSeats = HashSet<String>()
    //endregion

    //region private
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

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

                val pairXYAndSeat = getClickedSeatAndXY(event.x, event.y)
                val pairXY = pairXYAndSeat?.first
                val clickedSeat = pairXYAndSeat?.second
                if (clickedSeat != null) {
                    if (clickedSeat.isVisible()) {
                        if (selectedSeats.contains(clickedSeat.id())) {
                            releaseSeat(pairXY!!.first, pairXY.second)
                        } else {
                            if (seatViewListener.canSelectSeat(
                                    clickedSeat,
                                    selectedSeats
                                )
                            ) {
                                selectSeat(pairXY!!.first, pairXY.second)
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

    fun initSeatView(
        list: Array<Array<SEAT>>,
        optionalConfig: SeatViewConfig = SeatViewConfig()
    ) {

        config = optionalConfig
        seats = list
        parameters.apply {
            xSize = list.first().size
            ySize = list.size
        }

        //add all pre-selected seat inside selectedSeats
        seats.forEachIndexed { _, arrayOfSeats ->
            arrayOfSeats.forEachIndexed { _, seat ->
                if (seat.isPreSelected()) {
                    selectedSeats.add(seat.id())
                    seat.allConnectedSeatIds().forEach { seatId ->
                        selectedSeats.add(seatId)
                    }
                }
            }
        }

        parameters.apply {
            seatDefaultHeight = config.seatDefaultWidth / config.seatWidthHeightRatio

            seatWidth = config.seatDefaultWidth
            seatHeight = seatDefaultHeight

            seatInlineGap = seatWidth * config.seatInlineGapWidthRatio
            seatNewlineGap = seatWidth * config.seatNewlineGapWidthRatio
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        parameters.windowRectF = RectF(
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
        drawSeats(canvas)

        extensions.forEach {
            if (it.isActive()) {
                it.draw(canvas, parameters, config)
            }
        }
    }

    private fun drawSeats(canvas: Canvas) {
        parameters.apply {
            for (yIndex in 0 until ySize) {
                for (xIndex in 0 until xSize) {
                    val seatBean = getSeat(xIndex, yIndex)

                    val seatRectF = getSeatRect(xIndex, yIndex, seatBean)

                    if (seatRectF.right < windowRectF.left || seatRectF.left > windowRectF.right
                        || seatRectF.top > windowRectF.bottom || seatRectF.bottom < windowRectF.top
                    ) {
                        continue
                    }

                    if (seatBean.isVisible()) {
                        seatDrawer.draw(
                            context = context,
                            params = parameters,
                            config = config,
                            canvas = canvas,
                            seat = seatBean,
                            seatRectF = seatRectF,
                            isSelected = selectedSeats.contains(seatBean.id())
                        )
                    }
                }
            }
        }

    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            parameters.scaleFactorStart = detector.scaleFactor
            return true
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            parameters.apply {
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

                    val matrix = Matrix()
                    matrix.preScale(scaleFactor, scaleFactor, focusX, focusY)
                    if ((scaleFactor - scaleFactorStart) < 0) { //zoom out
                        matrix.postTranslate(stepX, stepY)
                    }
                    matrix.mapRect(virtualRectF)

                    ViewCompat.postInvalidateOnAnimation(this@SeatView)
                }
            }


            return true
        }
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

    private fun initParameters() {

        if (config.seatDefaultWidth < config.seatMinWidth) {
            throw RuntimeException("seatDefaultWidth cannot be smaller than seatMinWidth")
        }

        parameters.apply {

            val virtualWidth = (xSize * (seatWidth + seatInlineGap)) - seatInlineGap
            val virtualHeight = (ySize * (seatHeight + seatNewlineGap)) - seatNewlineGap

            val left = (windowRectF.centerX() - (virtualWidth / 2))
            val top = (windowRectF.centerY() - (virtualHeight / 2))

            virtualRectF = RectF(
                left,
                top,
                left + virtualWidth,
                top + virtualHeight
            )
        }

        extensions.forEach {
            it.init(parameters, config)
        }
    }

    /**
     * Get seat rect for given xIndex and yIndex
     * Seat rect is used for drawing seats at the correct place
     */
    private fun getSeatRect(xIndex: Int, yIndex: Int, seatBean: Seat): RectF {
        parameters.apply {
            val left =
                virtualRectF.left + (xIndex * (seatWidth + seatInlineGap)) + seatBean.leftRectAddition(
                    seatInlineGap
                )
            val right = left + seatWidth + seatBean.rightRectAddition(seatInlineGap)

            val top = virtualRectF.top + (yIndex * (seatHeight + seatNewlineGap))
            val bottom = top + seatHeight

            return RectF(left, top, right, bottom)
        }
    }

    private fun getClickedSeatAndXY(touchX: Float, touchY: Float): Pair<Pair<Int, Int>, SEAT?>? {
        val pairXY = getClickedSafeXY(touchX, touchY)
        pairXY?.let {
            return Pair(pairXY, getSeat(it.first, it.second))
        }
        return null
    }

    private fun getClickedSafeXY(touchX: Float, touchY: Float): Pair<Int, Int>? {
        parameters.apply {
            val virtualX = touchX - virtualRectF.left
            val virtualY = touchY - virtualRectF.top
            return if (virtualX < 0 || virtualX > virtualRectF.width()
                || virtualY < 0 || virtualY > virtualRectF.height()
            ) {
                //Touch outside of the seats
                null
            } else {
                val xIndex = floor((virtualX / (seatWidth + seatInlineGap)).toDouble()).toInt()
                val yIndex = floor((virtualY / (seatHeight + seatNewlineGap)).toDouble()).toInt()
                if (isSafeSelect(xIndex, yIndex)) Pair(xIndex, yIndex) else null
            }
        }
    }

    private fun moveSeatView(moveX: Float, moveY: Float) {
        parameters.apply {
            val possibleRectF = RectF(
                virtualRectF.left,
                virtualRectF.top,
                virtualRectF.right,
                virtualRectF.bottom
            )
            possibleRectF.offset(-moveX, 0f)
            if (possibleRectF.left <= windowRectF.left && possibleRectF.right >= windowRectF.right) {
                virtualRectF.offset(-moveX, 0f)
            }
            possibleRectF.offset(0f, -moveY)
            if (possibleRectF.top <= windowRectF.top && possibleRectF.bottom >= windowRectF.bottom) {
                virtualRectF.offset(0f, -moveY)
            }
        }
    }

    /**
     * Make sure the seat list contains the given x and y
     */
    fun isSafeSelect(xIndex: Int, yIndex: Int): Boolean {
        return (0 until parameters.xSize).contains(xIndex)
                && (0 until parameters.ySize).contains(yIndex)
    }

    fun getSeat(xIndex: Int, yIndex: Int): SEAT {
        return seats[yIndex][xIndex]
    }

    fun selectSeat(xIndex: Int, yIndex: Int): SEAT? {
        if (isSafeSelect(xIndex, yIndex)) {
            val seatBean = getSeat(xIndex, yIndex)
            if (selectedSeats.contains(seatBean.id()).not()) {
                selectedSeats.add(seatBean.id())
                seatBean.allConnectedSeatIds().forEach {
                    selectedSeats.add(it)
                }
                seatViewListener.seatSelected(seatBean, selectedSeats)
                invalidate()
                return seatBean
            }
        }
        return null
    }

    fun getSeatPosition(id: String): Pair<Int, Int>? {
        seats.forEachIndexed { y, seatsX ->
            seatsX.forEachIndexed { x, seat ->
                if (seat.id() == id) {
                    return Pair(x, y)
                }
            }
        }
        return null
    }

    fun releaseSeat(xIndex: Int, yIndex: Int) {
        if (isSafeSelect(xIndex, yIndex)) {
            val seat = getSeat(xIndex, yIndex)
            if (selectedSeats.contains(seat.id())) {
                selectedSeats.remove(seat.id())
                seat.allConnectedSeatIds().forEach { seatId ->
                    selectedSeats.remove(seatId)
                }
                seatViewListener.seatReleased(seat, selectedSeats)
                invalidate()
            }
        }
    }

}
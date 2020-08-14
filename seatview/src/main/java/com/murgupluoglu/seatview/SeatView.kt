package com.murgupluoglu.seatview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.util.AttributeSet
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import java.math.BigDecimal
import java.util.*
import kotlin.math.floor


class SeatView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    //region public
    var config = SeatViewConfig()
    var seatArray: Array<Array<Seat>> = arrayOf()
    val selectedSeats = HashMap<String, Seat>()
    var rowNames: HashMap<String, String> = hashMapOf()
    var rowCount: Int = 0
    var columnCount: Int = 0
    lateinit var seatViewListener: SeatViewListener
    //endregion

    //region private
    lateinit var windowRectF: RectF
        private set
    lateinit var virtualRectF: RectF
        private set

    private var seatMinHeight: Float = config.seatMinWidth / config.seatWidthHeightRatio
    private var seatMaxHeight: Float = config.seatMaxWidth / config.seatWidthHeightRatio
    private var seatDefaultHeight: Float = config.seatDefaultWidth / config.seatWidthHeightRatio

    private var seatWidth = config.seatDefaultWidth
    private var seatHeight = seatDefaultHeight

    private var seatInlineGap: Float = seatWidth * config.seatInlineGapWidthRatio
    private var seatNewlineGap: Float = seatWidth * config.seatNewlineGapWidthRatio

    private var scaleDetector: ScaleGestureDetector
    private var bitmaps = HashMap<String, Bitmap>()
    private val commonPaint = Paint().apply {
        isAntiAlias = true
    }

    private val debugPaint = Paint()

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
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
                selectedSeats.put(oneSeatInsideMultipe.id!!, oneSeatInsideMultipe)
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

    fun initSeatView(_seatArray: Array<Array<Seat>>, _rowCount: Int, _columnCount: Int, _rowNames: HashMap<String, String>) {
        bitmaps = HashMap()

        //add all pre-selected seat inside selectedSeats
        seatArray.forEachIndexed { rowIndex, arrayOfSeats ->
            arrayOfSeats.forEachIndexed { columnIndex, seat ->
                if (seat.isSelected) {
                    addSeatsInsideSelected(seat)
                }
            }
        }

        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                windowRectF = RectF(
                        config.leftPadding,
                        config.topPadding,
                        measuredWidth.toFloat() - config.rightPadding,
                        measuredHeight.toFloat() - config.bottomPadding
                )
                seatArray = _seatArray
                rowCount = _rowCount
                columnCount = _columnCount
                rowNames = _rowNames
                initParameters()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(config.backgroundColor)
        drawSeat(canvas)

        if (config.centerLineConfig.isActive) {
            drawCenterLine(this, canvas)
        }

        if (config.isDebug) {
            debugPaint.apply {
                color = Color.BLACK
                strokeWidth = 3f
                style = Paint.Style.STROKE
            }
            canvas.drawRect(windowRectF, debugPaint)

            debugPaint.apply {
                color = Color.RED
            }
            canvas.drawRect(virtualRectF, debugPaint)

            debugPaint.apply {
                color = Color.BLUE
                style = Paint.Style.FILL
            }
            canvas.drawCircle(virtualRectF.left, virtualRectF.top, 20f, debugPaint)
            canvas.drawCircle(virtualRectF.right, virtualRectF.bottom, 20f, debugPaint)

            debugPaint.apply {
                color = Color.BLACK
                style = Paint.Style.FILL
            }
            canvas.drawLine(windowRectF.centerX(), windowRectF.centerY(), windowRectF.centerX(), windowRectF.bottom, debugPaint)
        }
    }

    private fun drawSeat(canvas: Canvas) {
        for (rowIndex in 0 until rowCount) {
            for (columnIndex in 0 until columnCount) {
                val seatBean = seatArray[rowIndex][columnIndex]

                val seatRectF = getSeatRect(rowIndex, columnIndex, seatBean)

                if (seatRectF.right < windowRectF.left || seatRectF.left > windowRectF.right
                        || seatRectF.top > windowRectF.bottom || seatRectF.bottom < windowRectF.top) {
                    continue
                }

                if (seatBean.type != Seat.TYPE.NOT_EXIST) {
                    val resourceName: String
                    val resourceColor: String
                    if (seatBean.isSelected) {
                        resourceName = seatBean.selectedDrawableResourceName
                        resourceColor = seatBean.selectedDrawableColor
                    } else {
                        resourceName = seatBean.drawableResourceName
                        resourceColor = seatBean.drawableColor
                    }

                    var calculatedSeatWidth = seatWidth
                    if (seatBean.multipleType == Seat.MULTIPLETYPE.LEFT || seatBean.multipleType == Seat.MULTIPLETYPE.RIGHT) {
                        calculatedSeatWidth = seatWidth + seatInlineGap / 2
                    } else if (seatBean.multipleType == Seat.MULTIPLETYPE.CENTER) {
                        calculatedSeatWidth = seatWidth + seatInlineGap
                    }
                    val seatTypeId = seatBean.type.toString() + "_" + seatBean.multipleType + "_" + seatBean.drawableColor + "_" + seatBean.isSelected
                    val drawBitmap = drawableToBitmap(seatTypeId, calculatedSeatWidth, seatHeight, resourceName, resourceColor)


                    if (drawBitmap != null) canvas.drawBitmap(drawBitmap, null, seatRectF, commonPaint)
                }
            }
        }
    }

    private fun drawableToBitmap(seatType: String, width: Float, height: Float, resourceName: String, color: String): Bitmap? {
        var bitmap: Bitmap? = null
        if (bitmaps[seatType] != null) {
            bitmap = bitmaps[seatType]!!
        } else {
            var drawable = ContextCompat.getDrawable(context, R.drawable.square_seat)
            if (!isInEditMode && resourceName != "null") {
                val resId = resources.getIdentifier(resourceName, "drawable", context.packageName)
                drawable = ContextCompat.getDrawable(context, resId)
            }
            if (drawable != null) {
                if (color != "null") {
                    DrawableCompat.setTint(drawable, Color.parseColor(color))
                }
                bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap!!)
                drawable.setBounds(0, 0, width.toInt(), height.toInt())
                drawable.draw(canvas)
                bitmaps[seatType] = bitmap
            }
        }

        return bitmap
    }


    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return true
        }
        override fun onScale(detector: ScaleGestureDetector): Boolean {

            val newSeatWidth = seatWidth * detector.scaleFactor

            if(newSeatWidth >= config.seatMinWidth && newSeatWidth <= config.seatMaxWidth){
                seatWidth = newSeatWidth
                seatHeight = seatWidth / config.seatWidthHeightRatio
                seatInlineGap = seatWidth * config.seatInlineGapWidthRatio
                seatNewlineGap = seatWidth * config.seatNewlineGapWidthRatio

                val matrix = Matrix()
                matrix.setScale(detector.scaleFactor, detector.scaleFactor)
                matrix.mapRect(virtualRectF)
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
        return RectF(left, right, top, bottom)
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

    fun selectSeat(rowIndex: Int, columnIndex: Int) {
        if (isSafeSelect(rowIndex, columnIndex)) {
            val seatBean = seatArray[rowIndex][columnIndex]
            // TODO centerGivenSeat(seatBean)
            if (!seatBean.isSelected) {
                seatBean.isSelected = true
                addSeatsInsideSelected(seatBean)
                seatViewListener.seatSelected(seatBean, selectedSeats)
                invalidate()
            }
        }
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

        var virtualWidth = columnCount * (seatWidth + seatInlineGap) - seatInlineGap
        if (virtualWidth < windowRectF.width()) {
            //virtualWidth = windowRectF.width()
        }
        val virtualHeight = rowCount * (seatHeight + seatNewlineGap) - seatNewlineGap

        val left = (windowRectF.centerX() - virtualWidth / 2)
        val top = (windowRectF.centerY() - virtualHeight / 2)
        virtualRectF = RectF(
                left,
                top,
                left + virtualWidth,
                top + virtualHeight
        )
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
                || virtualY < 0 || virtualY > virtualRectF.height()) {
            //Touch outside of the seats
            return null
        } else {
            val columnIndex = floor((virtualX / (seatWidth + seatInlineGap)).toDouble()).toInt()
            val rowIndex = floor((virtualY / (seatHeight + seatNewlineGap)).toDouble()).toInt()
            return if (isSafeSelect(rowIndex, columnIndex)) getSeat(rowIndex, columnIndex) else null
        }
    }

    private fun moveSeatView(moveX: Float, moveY: Float) {

        val possibleRectF = RectF(virtualRectF.left, virtualRectF.top, virtualRectF.right, virtualRectF.bottom)
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
     * For make sure seatArray contains given rowIndex and columnIndex
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
        scaleDetector = ScaleGestureDetector(context, ScaleListener())
    }
}

fun Float.dp2px(): Float {
    val scale: Float = Resources.getSystem().displayMetrics.density
    return (this * scale + 0.5f)
}

fun Float.round(decimalPlace: Int): Float {
    var bd = BigDecimal(this.toString())
    bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP)
    return bd.toFloat()
}
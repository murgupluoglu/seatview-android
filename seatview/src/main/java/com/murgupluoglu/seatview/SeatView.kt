package com.murgupluoglu.seatview

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.Build
import android.text.TextUtils
import android.util.AttributeSet
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import java.util.*


class SeatView : View {

    /*
    * @JvmOverloads is not working with isInEditMode
    * */
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initAttributes(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        initAttributes(context, attrs)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int) : super(context, attrs, defStyle, defStyleRes) {
        initAttributes(context, attrs)
    }



    lateinit var seatViewConfig: SeatViewConfig
    private var bitmaps = HashMap<String, Bitmap>()


    private val commonPaint = Paint()
    private val rowNumPaint = Paint()
    private val cinemaScreenPaint = Paint()
    private val cinemaScreenTextPaint = Paint()
    private val seatNamesBarPaint = Paint()
    private val centerLinePaint = Paint()
    private val thumbSeatViewPaint = Paint()

    private var fingerOnScreen = false

    var seatClickListener: SeatViewListener? = null
    val selectedSeats = HashMap<String, Seat>()

    lateinit var mScaleDetector: ScaleGestureDetector

    private val hideThumbViewRunnable = Runnable { if (!fingerOnScreen) invalidate() }

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
            seatViewConfig.moveSeatView(distanceX, distanceY)
            invalidate()
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onSingleTapUp(event: MotionEvent): Boolean {
            val clickedSeat = seatViewConfig.getClickedSeat(event.x, event.y)
            if (clickedSeat != null) {
                if (clickedSeat.type != Seat.TYPE.NOT_EXIST) {
                    if (selectedSeats[clickedSeat.id] != null) {
                        removeSeatsInsideSelected(clickedSeat)
                        seatClickListener!!.seatReleased(clickedSeat, selectedSeats)
                    } else {
                        if (seatClickListener!!.canSelectSeat(clickedSeat, selectedSeats)) {
                            addSeatsInsideSelected(clickedSeat)
                            seatClickListener!!.seatSelected(clickedSeat, selectedSeats)
                        }
                    }
                }
                invalidate()
            } else {
                // click blank area
            }


            if (SeatViewConfig.zoomActive && SeatViewConfig.zoomAfterClickActive
                    && seatViewConfig.seatWidth < seatViewConfig.seatMaxWidth) {
                val x = event.x
                val y = event.y

                postDelayed(object : Runnable {
                    override fun run() {
                        var newSeatWidth = seatViewConfig.seatWidth + 8
                        newSeatWidth = Math.min(newSeatWidth, seatViewConfig.seatMaxWidth)

                        seatViewConfig.setSeatWidth(newSeatWidth, x, y)
                        invalidate()
                        if (newSeatWidth >= seatViewConfig.seatMaxWidth) return
                        postDelayed(this, 10)
                    }
                }, 20)

            }

            return super.onSingleTapUp(event)
        }
    })

    private fun initAttributes(context: Context, attrs: AttributeSet) {
        mScaleDetector = ScaleGestureDetector(context, ScaleListener())

        val a = context.obtainStyledAttributes(attrs, R.styleable.SeatView, 0, 0)
        //Kotlin 'when' doesn't work inside isInEditMode
        if (a.hasValue(R.styleable.SeatView_zoomAfterClickActive)) SeatViewConfig.zoomAfterClickActive = a.getBoolean(R.styleable.SeatView_zoomAfterClickActive, SeatViewConfig.zoomAfterClickActive)
        if (a.hasValue(R.styleable.SeatView_seatViewBackgroundColor)) SeatViewConfig.seatViewBackgroundColor = a.getString(R.styleable.SeatView_seatViewBackgroundColor)
        if (a.hasValue(R.styleable.SeatView_seatNamesBarActive)) SeatViewConfig.seatNamesBarActive = a.getBoolean(R.styleable.SeatView_seatNamesBarActive, SeatViewConfig.seatNamesBarActive)
        if (a.hasValue(R.styleable.SeatView_thumbSeatViewActive)) SeatViewConfig.thumbSeatViewActive = a.getBoolean(R.styleable.SeatView_thumbSeatViewActive, SeatViewConfig.thumbSeatViewActive)
        if (a.hasValue(R.styleable.SeatView_centerLineActive)) SeatViewConfig.centerLineActive = a.getBoolean(R.styleable.SeatView_centerLineActive, SeatViewConfig.centerLineActive)
        if (a.hasValue(R.styleable.SeatView_cinemaScreenViewActive)) SeatViewConfig.cinemaScreenViewActive = a.getBoolean(R.styleable.SeatView_cinemaScreenViewActive, SeatViewConfig.cinemaScreenViewActive)
        if (a.hasValue(R.styleable.SeatView_cinemaScreenViewSide)) SeatViewConfig.cinemaScreenViewSide = a.getInt(R.styleable.SeatView_cinemaScreenViewSide, SeatViewConfig.cinemaScreenViewSide)
        if (a.hasValue(R.styleable.SeatView_zoomActive)) SeatViewConfig.zoomActive = a.getBoolean(R.styleable.SeatView_zoomActive, SeatViewConfig.zoomActive)
        if (a.hasValue(R.styleable.SeatView_cinemaScreenViewText)) SeatViewConfig.cinemaScreenViewText = a.getString(R.styleable.SeatView_cinemaScreenViewText)
        if (a.hasValue(R.styleable.SeatView_cinemaScreenViewBackgroundColor)) SeatViewConfig.cinemaScreenViewBackgroundColor = a.getString(R.styleable.SeatView_cinemaScreenViewBackgroundColor)
        if (a.hasValue(R.styleable.SeatView_cinemaScreenViewTextColor)) SeatViewConfig.cinemaScreenViewTextColor = a.getString(R.styleable.SeatView_cinemaScreenViewTextColor)
        if (a.hasValue(R.styleable.SeatView_centerLineWidth)) SeatViewConfig.centerLineWidth = a.getFloat(R.styleable.SeatView_centerLineWidth, SeatViewConfig.centerLineWidth)
        if (a.hasValue(R.styleable.SeatView_centerLineColor)) SeatViewConfig.centerLineColor = a.getString(R.styleable.SeatView_centerLineColor)
        if (a.hasValue(R.styleable.SeatView_thumbSeatViewBackgroundColor)) SeatViewConfig.thumbSeatViewBackgroundColor = a.getString(R.styleable.SeatView_thumbSeatViewBackgroundColor)
        if (a.hasValue(R.styleable.SeatView_thumbSeatViewPointerColor)) SeatViewConfig.thumbSeatViewPointerColor = a.getString(R.styleable.SeatView_thumbSeatViewPointerColor)
        if (a.hasValue(R.styleable.SeatView_thumbSeatViewPointerWidth)) SeatViewConfig.thumbSeatViewPointerWidth = a.getFloat(R.styleable.SeatView_thumbSeatViewPointerWidth, SeatViewConfig.thumbSeatViewPointerWidth)

        a.recycle()

        if (isInEditMode) { //this is just for layout editor preview
            val widthPixels = Resources.getSystem().displayMetrics.widthPixels
            val heightPixels = Resources.getSystem().displayMetrics.heightPixels

            val rowNamesArray = arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "G", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z")
            val random = Random()

            val rowCount = 10
            val columnCount = 29
            val seatArray = Array(rowCount) { Array(columnCount) { Seat() } }
            val rowNames: HashMap<String, String> = HashMap()
            var virtualIndexForRowName = 0

            seatArray.forEachIndexed { rowIndex, arrayOfSeats ->
                if (rowIndex != 3) {
                    rowNames.put(rowIndex.toString(), rowNamesArray.get(virtualIndexForRowName))
                    virtualIndexForRowName++
                }
                arrayOfSeats.forEachIndexed { columnIndex, seat ->
                    val rInteger = random.nextInt(3)
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

            seatViewConfig = SeatViewConfig(getContext(), widthPixels, heightPixels, seatArray, rowCount, columnCount, rowNames)
            initPaint()
        }
    }

    fun addSeatsInsideSelected(clickedSeat: Seat) {
        if (clickedSeat.multipleType == Seat.MULTIPLETYPE.NOTMULTIPLE) {
            clickedSeat.isSelected = true
            selectedSeats.put(clickedSeat.id!!, clickedSeat)
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

    fun initSeatView(seatArray: Array<Array<Seat>>, rowcount: Int, columncount: Int, rownames: HashMap<String, String>) {
        bitmaps = HashMap()

        //add all pre-selected seat inside selectedSeats
        seatArray.forEachIndexed { rowIndex, arrayOfSeats ->
            arrayOfSeats.forEachIndexed { columnIndex, seat ->
                if (seat.isSelected) {
                    addSeatsInsideSelected(seat)
                }
            }
        }

        val vto = viewTreeObserver
        vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                seatViewConfig = SeatViewConfig(context, measuredWidth, measuredHeight, seatArray, rowcount, columncount, rownames)
                initPaint()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

    }


    private fun initPaint() {
        commonPaint.isAntiAlias = true
        cinemaScreenPaint.color = Color.parseColor(SeatViewConfig.cinemaScreenViewBackgroundColor)
        cinemaScreenPaint.style = Paint.Style.FILL
        cinemaScreenPaint.isAntiAlias = true
        val cornerPathEffect = CornerPathEffect(12f)
        cinemaScreenPaint.pathEffect = cornerPathEffect

        seatNamesBarPaint.color = Color.parseColor(SeatViewConfig.seatNamesBarBackgroundColor)
        seatNamesBarPaint.alpha = SeatViewConfig.seatNamesBarBackgroundAlpha
        seatNamesBarPaint.style = Paint.Style.FILL
        seatNamesBarPaint.isAntiAlias = true

        rowNumPaint.color = Color.WHITE
        rowNumPaint.textAlign = Paint.Align.CENTER
        rowNumPaint.isAntiAlias = true
        rowNumPaint.textSize = seatViewConfig.seatNamesBarTextSize


        cinemaScreenTextPaint.color = Color.parseColor(SeatViewConfig.cinemaScreenViewTextColor)
        cinemaScreenTextPaint.textAlign = Paint.Align.CENTER
        cinemaScreenTextPaint.isAntiAlias = true
        cinemaScreenTextPaint.textSize = seatViewConfig.centerTextSize


        centerLinePaint.style = Paint.Style.STROKE
        centerLinePaint.strokeWidth = SeatViewConfig.centerLineWidth
        val effects = DashPathEffect(floatArrayOf(5f, 5f, 5f, 5f), 1f)
        centerLinePaint.pathEffect = effects
        centerLinePaint.isAntiAlias = true
        centerLinePaint.color = Color.parseColor(SeatViewConfig.centerLineColor)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.parseColor(SeatViewConfig.seatViewBackgroundColor))
        drawSeat(canvas)

        if (SeatViewConfig.seatNamesBarActive) {
            drawSeatNamesView(canvas)
        }

        if (SeatViewConfig.cinemaScreenViewActive) {
            drawCinemaScreen(canvas)
        }

        if (SeatViewConfig.centerLineActive) {
            drawCenterLine(canvas)
        }

        if (SeatViewConfig.thumbSeatViewActive) {
            drawThumbSeatView(canvas)
        }
    }


    private fun drawSeat(canvas: Canvas) {
        for (rowIndex in 0 until seatViewConfig.rowCount) {
            for (columnIndex in 0 until seatViewConfig.columnCount) {
                val seatBean = seatViewConfig.seatArray[rowIndex][columnIndex]

                val seatRectF = seatViewConfig.getSeatRect(rowIndex, columnIndex, seatBean)

                if (seatRectF.right < seatViewConfig.windowRectF.left || seatRectF.left > seatViewConfig.windowRectF.right || seatRectF.top > seatViewConfig
                                .windowRectF.bottom || seatRectF.bottom < seatViewConfig.windowRectF.top) {
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

                    var calculatedSeatWidth = seatViewConfig.seatWidth
                    if (seatBean.multipleType == Seat.MULTIPLETYPE.LEFT || seatBean.multipleType == Seat.MULTIPLETYPE.RIGHT) {
                        calculatedSeatWidth = seatViewConfig.seatWidth + seatViewConfig.seatInlineGap / 2
                    } else if (seatBean.multipleType == Seat.MULTIPLETYPE.CENTER) {
                        calculatedSeatWidth = seatViewConfig.seatWidth + seatViewConfig.seatInlineGap
                    }
                    val seatTypeId = seatBean.type.toString() + "_" + seatBean.multipleType + "_" + seatBean.drawableColor + "_" + seatBean.isSelected
                    val drawBitmap = drawableToBitmap(seatTypeId, calculatedSeatWidth, seatViewConfig.seatHeight, resourceName, resourceColor)


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
                if (!color.equals("null")) {
                    DrawableCompat.setTint(drawable, Color.parseColor(color))
                }
                bitmap = Bitmap.createBitmap(width.toInt(), height.toInt(), Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap!!)
                drawable.setBounds(0, 0, width.toInt(), height.toInt())
                drawable.draw(canvas)
                bitmaps.set(seatType, bitmap)
            }
        }

        return bitmap
    }


    private fun drawSeatNamesView(canvas: Canvas) {
        //draw background
        canvas.drawRoundRect(seatViewConfig.seatNamesBarRect, seatViewConfig.seatNamesBarWidth / 2, seatViewConfig.seatNamesBarWidth / 2, seatNamesBarPaint)

        //draw numbers
        for (rowIndex in 0 until seatViewConfig.rowCount) {
            val rowNumRect = seatViewConfig.getRowNumRect(rowIndex)
            val rowNum = seatViewConfig.rowNames.get(rowIndex.toString())
            if (!TextUtils.isEmpty(rowNum))
                canvas.drawText(rowNum!!, rowNumRect.centerX(), rowNumRect.centerY(), rowNumPaint)
        }
    }

    private fun drawCinemaScreen(canvas: Canvas) {
        canvas.drawPath(seatViewConfig.cinemaScreenViewPath, cinemaScreenPaint)
        var yValue = 0.0f
        if (SeatViewConfig.cinemaScreenViewSide == 0) yValue = seatViewConfig.cinemaScreenViewHeight / 2 + cinemaScreenTextPaint.textSize / 2 - 4 //top
        if (SeatViewConfig.cinemaScreenViewSide == 1) yValue = seatViewConfig.windowHeight - (seatViewConfig.cinemaScreenViewHeight / 2 - (cinemaScreenTextPaint.textSize / 2)) //bottom
        canvas.drawText(SeatViewConfig.cinemaScreenViewText, seatViewConfig.screenCenterX, yValue, cinemaScreenTextPaint)
    }

    private fun drawCenterLine(canvas: Canvas) {
        val linePath = seatViewConfig.centerLinePath
        canvas.drawPath(linePath, centerLinePaint)
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {

            val newWidth = (seatViewConfig.seatWidth * detector.scaleFactor)
            seatViewConfig.setSeatWidth(newWidth, detector.focusX, detector.focusY)

            invalidate()
            return true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        restStatusByAction(event)
        if (event.pointerCount == 1) {
            gestureDetector.onTouchEvent(event)
        } else {
            if (SeatViewConfig.zoomActive) {
                mScaleDetector.onTouchEvent(event)
            }
        }
        return true
    }

    private fun restStatusByAction(event: MotionEvent) {
        val action = event.action and MotionEvent.ACTION_MASK
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                fingerOnScreen = true
                removeCallbacks(hideThumbViewRunnable)
            }
            MotionEvent.ACTION_UP -> {
                fingerOnScreen = false
                postDelayed(hideThumbViewRunnable, 1000)
            }
        }
    }

    private fun getThumbSeatRect(seatWidth: Float, seatHeight: Float, seatGapInLine: Float, seatGapNewLine: Float, padding: Float, rowIndex: Int, columnIndex: Int): RectF {
        val top = padding + rowIndex * (seatHeight + seatGapNewLine)
        val bottom = top + seatHeight
        val left = padding + columnIndex * (seatWidth + seatGapInLine)
        val right = left + seatWidth
        return RectF(left, top, right, bottom)
    }

    private fun getVisibleThumbRect(thumbWidth: Float, thumbHeight: Float): RectF {
        var left = -seatViewConfig.xOffset / seatViewConfig.virtualWidth * thumbWidth
        left = Math.max(left, 0f)
        var top = -seatViewConfig.yOffset / seatViewConfig.virtualHeight * thumbHeight
        top = Math.max(top, 0f)

        var height = thumbHeight * seatViewConfig.windowHeight / seatViewConfig.virtualHeight
        height = Math.min(height, thumbHeight - top)
        var width = thumbWidth * seatViewConfig.windowWidth / seatViewConfig.virtualWidth
        width = Math.min(width, thumbWidth - left)

        val bottom = top + height
        val right = left + width

        return RectF(left, top, right, bottom)
    }

    private fun drawThumbSeatView(canvas: Canvas) {
        if (!fingerOnScreen) return
        if (seatViewConfig.THUMB_HEIGHT <= 0 || seatViewConfig.THUMB_WIDTH <= 0) {
            return
        }

        thumbSeatViewPaint.color = Color.parseColor(SeatViewConfig.thumbSeatViewBackgroundColor)
        thumbSeatViewPaint.style = Paint.Style.FILL
        canvas.drawRect(0f, 0f, seatViewConfig.THUMB_WIDTH, seatViewConfig.THUMB_HEIGHT, thumbSeatViewPaint)

        for (rowIndex in 0 until seatViewConfig.rowCount) {
            for (columnIndex in 0 until seatViewConfig.columnCount) {
                val seatBean = seatViewConfig.seatArray[rowIndex][columnIndex]

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
                    val drawBitmap = drawableToBitmap(seatBean.type.toString() + "_" + seatBean.isSelected, seatViewConfig.seatWidth, seatViewConfig.seatHeight, resourceName, resourceColor)


                    if (drawBitmap != null)
                        canvas.drawBitmap(drawBitmap, null,
                                getThumbSeatRect(
                                        seatViewConfig.THUMB_SEAT_WIDTH, seatViewConfig.THUMB_SEAT_HEIGHT,
                                        seatViewConfig.THUMB_GAP_INLINE, seatViewConfig.THUMB_GAP_NEWLINE,
                                        seatViewConfig.THUMB_PADDING,
                                        rowIndex, columnIndex
                                )
                                , commonPaint)
                }
            }
        }

        thumbSeatViewPaint.style = Paint.Style.STROKE
        thumbSeatViewPaint.strokeWidth = SeatViewConfig.thumbSeatViewPointerWidth
        thumbSeatViewPaint.color = Color.parseColor(SeatViewConfig.thumbSeatViewPointerColor)
        canvas.drawRect(getVisibleThumbRect(seatViewConfig.THUMB_WIDTH, seatViewConfig.THUMB_HEIGHT), thumbSeatViewPaint)
    }

    fun checkColumnIsEmpty(rowIndex: Int): Boolean {
        var isEmpty = true

        for (columnIndex in 0 until seatViewConfig.columnCount) {
            val seatBean = seatViewConfig.seatArray[rowIndex][columnIndex]
            if (seatBean.type != Seat.TYPE.NOT_EXIST) {
                isEmpty = false
            }
        }

        return isEmpty
    }

    fun isSeatSelected(rowIndex: Int, columnIndex: Int): Boolean {
        return if (seatViewConfig.isSafeSelect(rowIndex, columnIndex)) {
            seatViewConfig.seatArray[rowIndex][columnIndex].isSelected
        } else false
    }

    fun findSeatWithName(seatName: String): Seat? {
        seatViewConfig.seatArray.forEachIndexed { rowIndex, arrayOfRows ->
            arrayOfRows.forEachIndexed { columnIndex, seat ->
                if (seatName.equals(seat.seatName)) {
                    return seat
                }
            }
        }
        return null
    }

    fun selectSeat(rowIndex: Int, columnIndex: Int) {
        if (seatViewConfig.isSafeSelect(rowIndex, columnIndex)) {
            val seatBean = seatViewConfig.seatArray[rowIndex][columnIndex]
            if (!seatBean.isSelected) {
                seatBean.isSelected = true
                addSeatsInsideSelected(seatBean)
                seatClickListener!!.seatSelected(seatBean, selectedSeats)
                invalidate()
            }
        }
    }

    fun releaseSeat(rowIndex: Int, columnIndex: Int) {
        if (seatViewConfig.isSafeSelect(rowIndex, columnIndex)) {
            val seatBean = seatViewConfig.seatArray[rowIndex][columnIndex]
            if (seatBean.isSelected) {
                seatBean.isSelected = false
                removeSeatsInsideSelected(seatBean)
                seatClickListener!!.seatReleased(seatBean, selectedSeats)
                invalidate()
            }
        }
    }
}
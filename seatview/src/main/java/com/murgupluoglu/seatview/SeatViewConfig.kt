package com.murgupluoglu.seatview

import android.content.Context
import android.graphics.Path
import android.graphics.RectF
import android.util.Log
import java.math.BigDecimal

class SeatViewConfig(val context: Context) {

    var seatArray: Array<Array<Seat>> = arrayOf()
    var rowNames: HashMap<String, String> = hashMapOf()

    lateinit var windowRectF: RectF

    //Seatview Heigts
    var windowHeight: Float = 0f
    var windowWidth: Float = 0f

    /*seat config*/
    var virtualHeight: Float = 0f
    var virtualWidth: Float = 0f
    var rowCount: Int = 0
    var columnCount: Int = 0

    var seatWidth = 0f
    var seatHeight = 0f
    var seatInlineGap = 0f
    var seatNewlineGap = 0f

    var padding: Float = 0f

    //cinemaScreenView
    var cinemaScreenViewWidth: Float = 0f
    var cinemaScreenViewHeight: Float = 0f
    var centerTextSize: Float = 0f


    //seatNamesBar
    var seatNamesBarWidth: Float = 0f
    var seatNamesBarMarginLeft: Float = 0f
    var seatNamesBarTextSize: Float = 0f

    var seatMinWidth = 0f
    var seatMinHeight = 0f
    var seatMaxWidth = 0f
    var seatMaxHeight = 0f
    var seatDefaultWidth = 0f
    var seatDefaultHeight = 0f

    var xOffset = 0f
    var yOffset = 0f
    var xOffsetDefault: Float = 0f
    var yOffsetDefault: Float = 0f

    //thumbSeatView
    var THUMB_WIDTH: Float = 0f
    var THUMB_HEIGHT: Float = 0f
    var THUMB_SEAT_WIDTH: Float = 0f
    var THUMB_SEAT_HEIGHT: Float = 0f
    var THUMB_GAP_INLINE: Float = 0f
    var THUMB_GAP_NEWLINE: Float = 0f
    var THUMB_PADDING: Float = 0f


    //Cinema Screen
    var cinemaScreenViewActive: Boolean = true
    var cinemaScreenViewText: String = "Screen"
    var cinemaScreenViewBackgroundColor: String = "#E5E5E5"
    var cinemaScreenViewTextColor: String = "#202020"
    var cinemaScreenViewSide: Int = 0
    //
    //Center Line
    var centerLineActive: Boolean = true
    var centerLineColor: String = "#bebebe"
    var centerLineWidth: Float = 0.8f
    //
    //ThumbView
    var thumbSeatViewActive: Boolean = false
    var thumbSeatViewBackgroundColor: String = "#B0000000"
    var thumbSeatViewPointerColor: String = "#FFFFFF00"
    var thumbSeatViewPointerWidth: Float = 3f
    //
    //NumberBar
    var seatNamesBarActive: Boolean = true
    var seatNamesBarBackgroundColor: String = "#9F9F9F"
    var seatNamesBarTextColor: String = "#9F9F9F"
    var seatNamesBarBackgroundAlpha: Int = 200
    //
    var seatViewBackgroundColor: String = "#F4F4F4"
    var zoomActive: Boolean = true
    var zoomAfterClickActive: Boolean = true

    var seatWidthHeightRatio: Float = 1f
    var seatInlineGapWidthRatio: Float = 0.265f
    var seatNewlineGapWidthRatio: Float = 0.304f


    val seatNamesBarRect: RectF
        get() {
            val left = seatNamesBarMarginLeft
            val top = padding - seatNamesBarWidth / 2
            val right = left + seatNamesBarWidth
            val bottom = top + virtualHeight - padding * 2 + seatNamesBarWidth
            val drawRect = RectF(left, top, right, bottom)
            drawRect.offset(0f, yOffset)
            return drawRect
        }


    val screenCenterX: Float
        get() {
            val left = virtualWidth / 2 - cinemaScreenViewWidth / 2 + xOffset
            val right = left + cinemaScreenViewWidth
            return (left + right) / 2f
        }

    val cinemaScreenViewPath: Path
        get() {
            val screenPath = Path()
            val centerX = screenCenterX
            var cinemaScreenViewCalculatedHeight = 0.0f
            if (cinemaScreenViewSide == SIDE_BOTTOM) {
                cinemaScreenViewCalculatedHeight = windowHeight
            }
            screenPath.moveTo(centerX - cinemaScreenViewWidth / 2, cinemaScreenViewCalculatedHeight)
            screenPath.lineTo(centerX - cinemaScreenViewWidth / 2 + 0.03f * windowWidth, if (cinemaScreenViewCalculatedHeight != 0.0f) cinemaScreenViewCalculatedHeight - cinemaScreenViewHeight else cinemaScreenViewHeight)
            screenPath.lineTo(centerX + cinemaScreenViewWidth / 2 - 0.03f * windowWidth, if (cinemaScreenViewCalculatedHeight != 0.0f) cinemaScreenViewCalculatedHeight - cinemaScreenViewHeight else cinemaScreenViewHeight)
            screenPath.lineTo(centerX + cinemaScreenViewWidth / 2, cinemaScreenViewCalculatedHeight)
            screenPath.close()
            return screenPath
        }


    val centerLinePath: Path
        get() {
            val linePath = Path()
            linePath.moveTo(virtualWidth / 2 + xOffset, cinemaScreenViewHeight)
            linePath.lineTo(virtualWidth / 2 + xOffset, virtualHeight - padding + yOffset)
            return linePath
        }

    fun calculateParameters(){
        initSize(context, windowHeight.toInt(), windowWidth.toInt())


        val seatColumnCount = 2 + (4f * columnCount - 1) / 3f
        seatMinWidth = windowWidth / seatColumnCount
        seatMinHeight = seatMinWidth / seatWidthHeightRatio

        seatDefaultWidth = Math.max(seatDefaultWidth, seatMinWidth)
        seatDefaultHeight = seatDefaultWidth / seatWidthHeightRatio

        seatWidth = seatDefaultWidth
        seatHeight = seatDefaultHeight

        seatInlineGap = seatWidth * seatInlineGapWidthRatio
        seatNewlineGap = seatWidth * seatNewlineGapWidthRatio
        padding = seatWidth


        virtualWidth = columnCount * (seatWidth + seatInlineGap) - seatInlineGap + padding * 2
        virtualHeight = rowCount * (seatHeight + seatNewlineGap) - seatNewlineGap + padding * 2

        cinemaScreenViewWidth = windowWidth * 0.55f
        cinemaScreenViewHeight = cinemaScreenViewWidth / 8

        xOffsetDefault = (windowWidth - virtualWidth) / 2
        yOffsetDefault = if (cinemaScreenViewSide == 0) cinemaScreenViewHeight else 0f

        xOffset = xOffsetDefault
        yOffset = yOffsetDefault

        calculateThumbParameters()
    }

    private fun initSize(context: Context, windowHeight: Int, windowWidth: Int) {
        seatMaxWidth = context.resources.getDimensionPixelSize(R.dimen.seat_max_height).toFloat()
        seatMaxHeight = seatMaxWidth / seatWidthHeightRatio
        seatDefaultWidth = context.resources.getDimensionPixelSize(R.dimen.seat_min_height).toFloat()
        seatDefaultHeight = seatDefaultWidth / seatWidthHeightRatio
        this.windowHeight = windowHeight.toFloat()
        this.windowWidth = windowWidth.toFloat()

        seatNamesBarWidth = seatDefaultWidth
        seatNamesBarMarginLeft = seatNamesBarWidth
        seatNamesBarTextSize = context.resources.getDimensionPixelSize(R.dimen.text_size_tiny).toFloat()
        centerTextSize = context.resources.getDimensionPixelSize(R.dimen.text_size_small).toFloat()

        windowRectF = RectF(0f, 0f, windowWidth.toFloat(), windowHeight.toFloat())
    }

    private fun calculateThumbParameters() {
        THUMB_WIDTH = windowWidth * 0.35f

        val seatColumnCount = columnCount.toFloat() + seatInlineGapWidthRatio * (columnCount - 1) + 2f //padding = seatwidth
        THUMB_SEAT_WIDTH = THUMB_WIDTH / seatColumnCount
        THUMB_SEAT_HEIGHT = THUMB_SEAT_WIDTH / seatWidthHeightRatio
        THUMB_GAP_INLINE = THUMB_SEAT_WIDTH * seatInlineGapWidthRatio
        THUMB_GAP_NEWLINE = THUMB_SEAT_WIDTH * seatNewlineGapWidthRatio
        THUMB_PADDING = THUMB_SEAT_WIDTH

        THUMB_HEIGHT = THUMB_SEAT_HEIGHT * rowCount + THUMB_GAP_NEWLINE * (rowCount - 1) + THUMB_PADDING * 2
    }

    fun getSeatRect(rowIndex: Int, columnIndex: Int, seatBean: Seat): RectF {
        var left = padding + columnIndex * (seatInlineGap + seatWidth)
        var right = left + seatWidth
        if (seatBean.multipleType == Seat.MULTIPLETYPE.LEFT) {
            right += seatInlineGap / 2
        } else if (seatBean.multipleType == Seat.MULTIPLETYPE.RIGHT) {
            left -= seatInlineGap / 2
        } else if (seatBean.multipleType == Seat.MULTIPLETYPE.CENTER) {
            left -= seatInlineGap / 2
            right += seatInlineGap / 2
        }

        val top = padding + rowIndex * (seatNewlineGap + seatHeight)
        val bottom = top + seatHeight
        val drawRect = RectF(left, top, right, bottom)
        drawRect.offset(xOffset, yOffset)
        return drawRect
    }


    fun getRowNumRect(rowIndex: Int): RectF {
        val left = seatNamesBarMarginLeft
        val top = padding + rowIndex * (seatHeight + seatNewlineGap) + seatHeight / 2
        val right = left + seatNamesBarWidth
        val bottom = top + seatNamesBarTextSize
        val drawRect = RectF(left, top, right, bottom)
        drawRect.offset(0f, yOffset)
        return drawRect
    }

    fun getClickedSeat(touchX: Float, touchY: Float): Seat? {

        val position = IntArray(2)
        val virtualX = touchX - xOffset
        val virtualY = touchY - yOffset
        if (virtualX < padding || virtualX > virtualWidth - padding
                || virtualY < padding || virtualY > virtualHeight - padding) {
            //touch outsize of the seats
            return null
        } else {
            val rowIndex = Math.floor(((virtualY - padding + seatNewlineGap / 2) / (seatHeight + seatNewlineGap)).toDouble()).toInt()
            val columnIndex = Math.floor(((virtualX - padding + seatInlineGap / 2) / (seatWidth + seatInlineGap)).toDouble()).toInt()
            position[0] = rowIndex
            position[1] = columnIndex

            return if (isSafeSelect(rowIndex, columnIndex)) getSeat(rowIndex, columnIndex) else null
        }
    }

    fun getSeat(rowIndex: Int, columnIndex: Int): Seat? {
        return if (isSafeSelect(rowIndex, columnIndex)) {
            seatArray[rowIndex][columnIndex]
        } else null
    }

    fun Float.round(decimalPlace: Int): Float {
        var bd = BigDecimal(this.toString())
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP)
        return bd.toFloat()
    }

    fun moveSeatView(moveX: Float, moveY: Float) {

        val newXOffset = (xOffset - moveX).round(2)
        val newYOffset = (yOffset - moveY).round(2)

        val seatViewRect = RectF(newXOffset, newYOffset, newXOffset + virtualWidth, newYOffset + virtualHeight)
        val windowRect = RectF(0f, 0f, windowWidth, windowHeight)


        if (moveX < 0) {//move right
            if (seatViewRect.left < windowRect.left + seatNamesBarWidth) {
                xOffset -= moveX
                xOffset = xOffset.round(2)
                Log.e("TAG", xOffset.toString())
            }
        } else {
            if (seatViewRect.right > windowRect.right) {
                this.xOffset -= moveX
                xOffset = xOffset.round(2)
            }
        }

        if (moveY < 0) { //move down
            var topCalculation = windowRect.top//side-bottom
            if (cinemaScreenViewSide == SIDE_TOP) {//TODO this can be imporeve with move outside
                topCalculation += cinemaScreenViewHeight
            }
            if (seatViewRect.top < topCalculation) {
                this.yOffset -= moveY
            }
        } else {
            var bottomCalculation = windowRect.bottom //side-top
            if (cinemaScreenViewSide == SIDE_BOTTOM) {
                bottomCalculation -= cinemaScreenViewHeight
            }
            if (seatViewRect.bottom > bottomCalculation) {
                this.yOffset -= moveY
            }
        }
    }

    fun setSeatWidth(newSeatWidth: Float, touchx: Float, touchy: Float) {

        var touchX = touchx
        var touchY = touchy

        val newSeatHeight = newSeatWidth / seatWidthHeightRatio
        if (newSeatWidth <= seatMinWidth || newSeatHeight <= seatMinHeight
                || newSeatWidth >= seatMaxWidth || newSeatHeight >= seatMaxHeight) {
            return
        }

        if (newSeatWidth < seatWidth) {
            touchX = windowWidth / 2
            touchY = windowHeight / 2
        }

        val virtualX = (touchX - xOffset).toDouble()
        val virtualY = (touchY - yOffset).toDouble()
        val ratioX = virtualX / virtualWidth
        val ratioY = virtualY / virtualHeight

        seatWidth = newSeatWidth.round(2)
        seatHeight = seatWidth / seatWidthHeightRatio
        seatInlineGap = seatWidth * seatInlineGapWidthRatio
        seatNewlineGap = seatWidth * seatNewlineGapWidthRatio
        padding = seatWidth

        virtualWidth = columnCount * (seatWidth + seatInlineGap) - seatInlineGap + padding * 2
        virtualHeight = rowCount * (seatHeight + seatNewlineGap) - seatNewlineGap + padding * 2

        xOffset = touchX - (virtualWidth * ratioX).toInt()
        yOffset = touchY - (virtualHeight * ratioY).toInt()
    }

    fun isSafeSelect(rowIndex: Int, columnIndex: Int): Boolean {
        return rowIndex >= 0 && columnIndex >= 0 && rowIndex < rowCount && columnIndex < columnCount
    }

    companion object {
        const val SIDE_TOP = 0
        const val SIDE_BOTTOM = 1
    }
}
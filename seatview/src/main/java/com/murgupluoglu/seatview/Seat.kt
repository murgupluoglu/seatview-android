package com.murgupluoglu.seatview


class Seat {

    /**
     * please provide unique id
     */
    var id: String? = null
    var type: Int = TYPE.NOT_EXIST
    var isSelected = false
    /**
     * if you have problem with shape,
     * please check SeatViewConfig -> seatWidthHeightRatio
     */
    var drawableResourceName : String = "null"
    var selectedDrawableResourceName : String = "null"
    var drawableColor = "null"
    var selectedDrawableColor = "null"
    var seatName = "null"

    var rowName: String? = null

    var rowIndex: Int = 0
    var columnIndex: Int = 0

    var multipleSeats: ArrayList<String> = ArrayList()
    var multipleType: Int = MULTIPLETYPE.NOTMULTIPLE

    object TYPE {
        val NOT_EXIST = 0
        val SELECTABLE = 1
        val UNSELECTABLE = 2
    }

    object MULTIPLETYPE {
        val NOTMULTIPLE = -1
        val LEFT = 0
        val CENTER = 1
        val RIGHT = 2
    }

}


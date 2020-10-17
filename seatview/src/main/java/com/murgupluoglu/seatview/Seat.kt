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
    var drawableResourceName: String = "null"
    var selectedDrawableResourceName: String = "null"
    var drawableColor = "null"
    var selectedDrawableColor = "null"
    var seatName = "null"

    var rowName: String? = null

    var rowIndex: Int = 0
    var columnIndex: Int = 0

    var multipleSeats: ArrayList<String> = ArrayList()
    var multipleType: Int = MULTIPLETYPE.NOTMULTIPLE

    object TYPE {
        const val NOT_EXIST = 0
        const val SELECTABLE = 1
        const val UNSELECTABLE = 2
    }

    object MULTIPLETYPE {
        const val NOTMULTIPLE = -1
        const val LEFT = 0
        const val CENTER = 1
        const val RIGHT = 2
    }

}


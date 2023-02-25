package com.murgupluoglu.seatviewsample.cinemascreen

import com.murgupluoglu.seatview.Seat

data class MultipleSeat(
    var seatId: String = "",
    var seatName: String = "",
    var type: Int = TYPE.NOT_EXIST,
    var isPreSelectedSeat: Boolean = false
) : Seat {

    override fun id(): String {
        return seatId
    }

    override fun isVisible(): Boolean {
        return type != TYPE.NOT_EXIST
    }

    override fun allConnectedSeatIds(): Array<String> {
        val listOfIds = arrayListOf<String>()
        listOfIds.add(seatId)
        listOfIds.addAll(multipleSeats)
        return listOfIds.toTypedArray()
    }

    override fun leftRectAddition(seatInlineGap: Float): Float {
        return when (multipleType) {
            MULTIPLETYPE.LEFT -> 0f
            MULTIPLETYPE.RIGHT -> -(seatInlineGap / 2)
            MULTIPLETYPE.CENTER -> -(seatInlineGap / 2)

            else -> 0f
        }
    }

    override fun rightRectAddition(seatInlineGap: Float): Float {
        return when (multipleType) {
            MULTIPLETYPE.LEFT -> seatInlineGap / 2
            MULTIPLETYPE.RIGHT -> +(seatInlineGap / 2)
            MULTIPLETYPE.CENTER -> +(seatInlineGap)

            else -> 0f
        }
    }

    override fun canSelect(): Boolean {
        return type == TYPE.SELECTABLE || type == TYPE.DISABLED_PERSON
    }

    override fun isPreSelected(): Boolean {
        return isPreSelectedSeat
    }

    override fun name(): String {
        return seatName
    }

    fun getResourceName(isSelected: Boolean): String {
        when (type) {
            TYPE.SELECTABLE -> {
                if (isSelected) {
                    when (multipleType) {
                        MULTIPLETYPE.LEFT -> return "seat_selected_multiple_left"
                        MULTIPLETYPE.CENTER -> return "seat_selected_multiple_center"
                        MULTIPLETYPE.RIGHT -> return "seat_selected_multiple_right"
                        MULTIPLETYPE.NOTMULTIPLE -> return "seat_selected"
                    }
                } else {
                    when (multipleType) {
                        MULTIPLETYPE.LEFT -> return "seat_available_multiple_left"
                        MULTIPLETYPE.CENTER -> return "seat_available_multiple_center"
                        MULTIPLETYPE.RIGHT -> return "seat_available_multiple_right"
                        MULTIPLETYPE.NOTMULTIPLE -> return "seat_available"
                    }
                }
            }

            TYPE.UNSELECTABLE -> {
                when (multipleType) {
                    MULTIPLETYPE.LEFT -> return "seat_notavailable_multiple_left"
                    MULTIPLETYPE.CENTER -> return "seat_notavailable_multiple_center"
                    MULTIPLETYPE.RIGHT -> return "seat_notavailable_multiple_right"
                    MULTIPLETYPE.NOTMULTIPLE -> return "seat_notavailable"
                }
            }

            TYPE.DISABLED_PERSON -> {
                return if (isSelected) {
                    "seat_selected"
                } else {
                    "seat_disabledperson"
                }
            }
        }

        return ""
    }

    var multipleSeats: ArrayList<String> = ArrayList()
    var multipleType: Int = MULTIPLETYPE.NOTMULTIPLE

    object TYPE {
        const val NOT_EXIST = 0
        const val SELECTABLE = 1
        const val UNSELECTABLE = 2
        const val DISABLED_PERSON = 10
    }

    object MULTIPLETYPE {
        const val NOTMULTIPLE = -1
        const val LEFT = 0
        const val CENTER = 1
        const val RIGHT = 2
    }
}
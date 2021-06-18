package com.murgupluoglu.seatview.compose

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.murgupluoglu.seatview.Seat
import com.murgupluoglu.seatview.SeatViewListener
import java.util.*

@Composable
fun SeatView(
    modifier: Modifier = Modifier,
    clickListener: SeatViewListener? = null,
    seatArray: Array<Array<Seat>>,
    rowCount: Int,
    columnCount: Int
) {

    BoxWithConstraints(
        modifier
            .background(MaterialTheme.colors.background)
    ) {

        val seatWidth = minOf(maxWidth, maxHeight) / maxOf(rowCount, columnCount)

        Row(
            modifier = Modifier.fillMaxSize().background(Color.Cyan),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(rowCount) { seatRow ->
                Column() {
                    repeat(columnCount) { seatColumn ->
                        Box(
                            Modifier
                                .width(seatWidth)
                                .height(seatWidth)
                        ) {
                            SeatCell(seatArray[seatRow][seatColumn], clickListener)
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun SeatCell(seat: Seat, clickListener: SeatViewListener? = null) {

    Box(
        Modifier
            .fillMaxSize()
            .border(1.dp, Color.Gray)
            .background(Color.Yellow)
            .clickable {
                if (seat.type != Seat.TYPE.NOT_EXIST) {
                    if (seat.isSelected) {
                        clickListener?.seatReleased(seat, hashMapOf())
                    } else {
                        if (clickListener?.canSelectSeat(seat, hashMapOf()) == true) {
                            clickListener.seatSelected(seat, hashMapOf())
                        }
                    }
                }
            }

    ) {
        Text(
            "${seat.rowIndex}:${seat.columnIndex}",
            modifier = Modifier.align(Alignment.Center),
            textAlign = TextAlign.Center
        )
    }

}

@Preview(widthDp = 500, heightDp = 1000)
@Composable
fun SudokuPreview() {
    // region Generate Sample
    var seatArray: Array<Array<Seat>> = arrayOf()

    val sRandom = Random()

    val sRowCount = 10
    val sColumnCount = 9
    val sSeatArray = Array(sRowCount) { Array(sColumnCount) { Seat() } }

    sSeatArray.forEachIndexed { rowIndex, arrayOfSeats ->
        arrayOfSeats.forEachIndexed { columnIndex, seat ->
            seat.seatName = "${rowIndex + 1 + (columnIndex * sRowCount)}"
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
    seatArray = sSeatArray
    //endregion Generate Sample

    SeatView(
        seatArray = seatArray,
        rowCount = sRowCount,
        columnCount = sColumnCount
    )
}
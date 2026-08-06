package com.murgupluoglu.seatviewsample

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.common.truth.Truth.assertThat
import com.murgupluoglu.seatview.SeatPosition
import com.murgupluoglu.seatview.SeatView
import com.murgupluoglu.seatviewsample.cinemascreen.CinemaScreenActivity
import com.murgupluoglu.seatviewsample.cinemascreen.MultipleSeat
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Created by Mustafa Urgupluoglu on 3.04.2019.
 */

@LargeTest
@RunWith(AndroidJUnit4::class)
class SeatViewTest {

    @get:Rule
    var rule: ActivityScenarioRule<CinemaScreenActivity> =
        ActivityScenarioRule(CinemaScreenActivity::class.java)

    @Test
    fun selectMultipleSeats() {
        rule.scenario.onActivity {
            val seatView = it.findViewById<SeatView<MultipleSeat>>(R.id.seatView)

            assertThat(seatView.selectedSeatIds).isEmpty()

            // Find a genuinely selectable seat that belongs to a multi-seat group; selecting it
            // must atomically select every connected id. (selectSeat now honors canSelect(), so
            // an unselectable seat can no longer be force-selected.)
            var target: SeatPosition? = null
            var expectedGroup: Set<String> = emptySet()
            loop@ for (row in 0 until seatView.rowCount) {
                for (column in 0 until seatView.columnCount) {
                    val position = SeatPosition(row, column)
                    val seat = seatView.seatAt(position) ?: continue
                    if (seat.canSelect() && seat.allConnectedSeatIds().size >= 2) {
                        target = position
                        expectedGroup = (seat.allConnectedSeatIds().toList() + seat.id()).toSet()
                        break@loop
                    }
                }
            }
            assumeTrue("no selectable multi-seat in sample data", target != null)

            seatView.selectSeat(target!!)

            assertThat(seatView.selectedSeatIds).isEqualTo(expectedGroup)
            assertThat(seatView.selectedSeatIds.size).isAtLeast(2)
        }
    }

    @Test
    fun selectSingleSeat() {
        rule.scenario.onActivity {
            val seatView = it.findViewById<SeatView<MultipleSeat>>(R.id.seatView)

            assertThat(seatView.selectedSeatIds).isEmpty()

            val disabledPersonSeat = seatView.selectSeat("A-3")

            assertThat(disabledPersonSeat).isNotNull()
            assertThat(disabledPersonSeat!!.type).isEqualTo(10)
            assertThat(seatView.selectedSeatIds).hasSize(1)
        }
    }

    @Test
    fun selectNotExistSeat() {
        rule.scenario.onActivity {
            val seatView = it.findViewById<SeatView<MultipleSeat>>(R.id.seatView)

            assertThat(seatView.selectedSeatIds).isEmpty()

            seatView.selectSeat(SeatPosition(row = 17, column = 13))

            assertThat(seatView.selectedSeatIds).isEmpty()
        }
    }

    @Test
    fun zoomActivePassive() {
        rule.scenario.onActivity {
            val seatView = it.findViewById<SeatView<MultipleSeat>>(R.id.seatView)

            assertThat(seatView.isZoomEnabled).isTrue()

            seatView.isZoomEnabled = false

            assertThat(seatView.isZoomEnabled).isFalse()
        }
    }

    @Test
    fun seatConfigsTest() {
        rule.scenario.onActivity {
            val seatView = it.findViewById<SeatView<MultipleSeat>>(R.id.seatView)

            assertThat(seatView.columnCount).isEqualTo(18)
            assertThat(seatView.rowCount).isEqualTo(14)
            assertThat(seatView.isZoomEnabled).isTrue()
        }
    }
}

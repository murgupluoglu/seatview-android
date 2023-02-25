package com.murgupluoglu.seatviewsample

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.murgupluoglu.seatview.SeatView
import com.murgupluoglu.seatviewsample.cinemascreen.CinemaScreenActivity
import com.murgupluoglu.seatviewsample.cinemascreen.MultipleSeat
import org.junit.*
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


    @Before
    fun beforeTest() {

    }

    @After
    fun afterTest() {
    }

    @Test
    fun selectMultipleSeats() {
        rule.scenario.onActivity {
            val seatView = it.findViewById<SeatView<MultipleSeat>>(R.id.seatView)

            Assert.assertEquals(0, seatView.selectedSeats.size)

            seatView.selectSeat(0, 0)

            Assert.assertEquals(2, seatView.selectedSeats.size)
        }
    }

    @Test
    fun selectSingleSeat() {
        rule.scenario.onActivity {
            val seatView = it.findViewById<SeatView<MultipleSeat>>(R.id.seatView)

            Assert.assertEquals(0, seatView.selectedSeats.size)

            val disabledSeatPosition = seatView.getSeatPosition("A-3")

            val disabledPersonSeat =
                seatView.selectSeat(disabledSeatPosition!!.first, disabledSeatPosition.second)

            Assert.assertNotNull(disabledPersonSeat)

            Assert.assertEquals(10, disabledPersonSeat!!.type)

            Assert.assertEquals(1, seatView.selectedSeats.size)
        }
    }

    @Test
    fun selectNotExistSeat() {
        rule.scenario.onActivity {
            val seatView = it.findViewById<SeatView<MultipleSeat>>(R.id.seatView)

            Assert.assertEquals(0, seatView.selectedSeats.size)

            seatView.selectSeat(13, 17)

            Assert.assertEquals(0, seatView.selectedSeats.size)
        }
    }

    @Test
    fun zoomActivePassive() {
        rule.scenario.onActivity {
            val seatView = it.findViewById<SeatView<MultipleSeat>>(R.id.seatView)

            Assert.assertEquals(true, seatView.config.isZoomActive)

            seatView.config.isZoomActive = false

            Assert.assertEquals(false, seatView.config.isZoomActive)
        }
    }

    @Test
    fun seatConfigsTest() {
        rule.scenario.onActivity {
            val seatView = it.findViewById<SeatView<MultipleSeat>>(R.id.seatView)

            Assert.assertEquals(18, seatView.parameters.xSize)

            Assert.assertEquals(14, seatView.parameters.ySize)

            Assert.assertEquals(true, seatView.config.isZoomActive)
        }
    }
}
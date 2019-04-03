package com.murgupluoglu.seatviewsample

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.murgupluoglu.seatview.SeatView
import com.murgupluoglu.seatview.SeatViewConfig
import org.junit.*
import org.junit.runner.RunWith

/**
 * Created by Mustafa Urgupluoglu on 3.04.2019.
 */

@LargeTest
@RunWith(AndroidJUnit4::class)
class SeatViewTest {

    @get:Rule
    val activityTestRule: ActivityTestRule<MockActivity> = ActivityTestRule(MockActivity::class.java)

    @Before
    fun beforeTest(){

    }

    @After
    fun afterTest(){
    }

    @Test
    fun seatConfigsTest() {
        val seatView = activityTestRule.activity.findViewById<SeatView>(R.id.seatView)
        Assert.assertEquals(15, seatView.config.rowCount)
        Assert.assertEquals(18, seatView.config.columnCount)
        Assert.assertEquals("M", seatView.config.rowNames.toList().sortedBy { it.first.toInt() }[0].second)
        Assert.assertEquals("Screen Name Android Test", seatView.config.cinemaScreenViewText)
        Assert.assertEquals(SeatViewConfig.SIDE_TOP, seatView.config.cinemaScreenViewSide)
        Assert.assertEquals(true, seatView.config.cinemaScreenViewActive)
        Assert.assertEquals(true, seatView.config.centerLineActive)
        Assert.assertEquals(true, seatView.config.seatNamesBarActive)
        Assert.assertEquals(true, seatView.config.zoomActive)
        Assert.assertEquals(false, seatView.config.zoomAfterClickActive)
        Assert.assertEquals("#F44336", seatView.config.cinemaScreenViewBackgroundColor)
        Assert.assertEquals("#F4F4F4", seatView.config.seatViewBackgroundColor)
        Assert.assertEquals("#bcb295", seatView.config.thumbSeatViewBackgroundColor)
        Assert.assertEquals("#e600ff", seatView.config.centerLineColor)
        Assert.assertEquals(200, seatView.config.seatNamesBarBackgroundAlpha)
    }
}
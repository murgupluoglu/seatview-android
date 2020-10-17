package com.murgupluoglu.seatview

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

/*
*  Created by Mustafa Ürgüplüoğlu on 17.10.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Assert.assertEquals("com.murgupluoglu.seatview", appContext.packageName)
    }
}
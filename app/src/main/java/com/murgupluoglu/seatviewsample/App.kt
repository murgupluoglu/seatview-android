package com.murgupluoglu.seatviewsample

import android.app.Application
import com.blankj.utilcode.util.Utils

/*
*  Created by Mustafa Ürgüplüoğlu on 25.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
    }
}
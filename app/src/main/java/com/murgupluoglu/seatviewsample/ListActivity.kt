package com.murgupluoglu.seatviewsample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.murgupluoglu.seatviewsample.cinemascreen.CinemaScreenActivity
import com.murgupluoglu.seatviewsample.json.JsonSampleActivity
import com.murgupluoglu.seatviewsample.number.NumbersActivity
import kotlinx.android.synthetic.main.activity_list.*

/*
*  Created by Mustafa Ürgüplüoğlu on 26.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class ListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        cinemaScreenSampleButton?.setOnClickListener {
            ActivityUtils.startActivity(CinemaScreenActivity::class.java)
        }

        numbersSampleButton?.setOnClickListener {
            ActivityUtils.startActivity(NumbersActivity::class.java)
        }

        fromJsonSampleButton?.setOnClickListener {
            ActivityUtils.startActivity(JsonSampleActivity::class.java)
        }

    }
}
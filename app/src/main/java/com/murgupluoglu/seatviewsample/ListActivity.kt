package com.murgupluoglu.seatviewsample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.murgupluoglu.seatviewsample.cinemascreen.CinemaScreenActivity
import com.murgupluoglu.seatviewsample.json.JsonSampleActivity
import com.murgupluoglu.seatviewsample.number.NumbersActivity

/*
*  Created by Mustafa Ürgüplüoğlu on 26.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class ListActivity : AppCompatActivity() {

    private val cinemaScreenSampleButton : Button by lazy{
        findViewById(R.id.cinemaScreenSampleButton)
    }
    private val numbersSampleButton : Button by lazy{
        findViewById(R.id.numbersSampleButton)
    }
    private val fromJsonSampleButton : Button by lazy{
        findViewById(R.id.fromJsonSampleButton)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        cinemaScreenSampleButton.setOnClickListener {
            ActivityUtils.startActivity(CinemaScreenActivity::class.java)
        }

        numbersSampleButton.setOnClickListener {
            ActivityUtils.startActivity(NumbersActivity::class.java)
        }

        fromJsonSampleButton.setOnClickListener {
            ActivityUtils.startActivity(JsonSampleActivity::class.java)
        }

    }
}
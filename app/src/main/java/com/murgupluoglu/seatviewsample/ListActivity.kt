package com.murgupluoglu.seatviewsample

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.ActivityUtils
import com.murgupluoglu.seatviewsample.basic.BasicActivity
import com.murgupluoglu.seatviewsample.cinemascreen.CinemaScreenActivity

/*
*  Created by Mustafa Ürgüplüoğlu on 26.09.2020.
*  Copyright © 2020 Mustafa Ürgüplüoğlu. All rights reserved.
*/

class ListActivity : AppCompatActivity() {

    private lateinit var cinemaScreenSampleButton: Button
    private lateinit var basicSampleButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        cinemaScreenSampleButton = findViewById(R.id.cinemaScreenSampleButton)
        basicSampleButton = findViewById(R.id.basicSampleButton)

        cinemaScreenSampleButton.setOnClickListener {
            ActivityUtils.startActivity(CinemaScreenActivity::class.java)
        }

        basicSampleButton.setOnClickListener {
            ActivityUtils.startActivity(BasicActivity::class.java)
        }

    }
}
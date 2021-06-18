package com.murgupluoglu.seatviewsample.utils

import android.content.Context


fun Context.loadJSONFromAsset(jsonName : String): String {
    val jsonString = assets.open(jsonName).bufferedReader().use {
        it.readText()
    }
    return jsonString
}
package com.nex

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {

    fun som(index: Int, url: String?) {
        println(index)
        println(url)
        println("-")
    }


    @Logger
    fun subscriber(string: String?, second: Int): String {
        return "$string + $second"
    }

    @Logger
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var index = 0
        var url: String? = "http"
        findViewById<View>(R.id.button).setOnClickListener {
            subscriber("some awesome parameter", 20)
        }
    }
}

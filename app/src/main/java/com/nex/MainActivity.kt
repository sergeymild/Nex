package com.nex

import android.annotation.TargetApi
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {

    var url: String? = "http"


    @Subscribe("d")
    @UiThread
    fun debuggableMethod() {
        println("-")
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)





        findViewById<View>(R.id.button).setOnClickListener {

            println(url)
        }
    }
}

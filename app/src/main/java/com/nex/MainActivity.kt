package com.nex

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.MainThread
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity

class Re

class MainActivity : AppCompatActivity() {

    var url: String? = "http"

    @MainThread
    fun list(one: String) {
        println(one)
    }

    @MainThread
    fun list(one: Int, re: Re) {
        println(one)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        var index = 0

        findViewById<View>(R.id.button).setOnClickListener {

            println(url)
        }
    }
}

package com.nex

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {
    @Memoize
    fun debo() {
        println(" parame: ")

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var index = 0
        var url: String? = "http"
        findViewById<View>(R.id.button).setOnClickListener {
            println(index)
            if (index < 2) println(debo())
            //else println(debo("index: $index"))
            index += 1
        }
    }
}

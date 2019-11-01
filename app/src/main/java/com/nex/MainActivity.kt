package com.nex

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.annotation.MainThread
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    @MainThread
    fun repeater(isActive: Boolean, one: String, two: Int) {
        findViewById<Button>(R.id.button).text = one
        println(isActive)
        println(one)
        println(two)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var index = 0
        var url: String? = "http"
        findViewById<View>(R.id.button).setOnClickListener {

            Thread {
                repeater(true, "dsds", 23)
            }.start()

        }
    }
}

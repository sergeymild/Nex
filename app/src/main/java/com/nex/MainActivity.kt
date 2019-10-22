package com.nex

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.View


class MainActivity : AppCompatActivity() {

    @Filter(parameterIndex = 1)
    fun som(index: Int, url: String?) {
        println(index)
        println(url)
        println("-")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var index = 0
        var url: String? = "http"
        findViewById<View>(R.id.button).setOnClickListener {
            index += 1
            if (index == 15) {
                index = 0
            }
            if (index == 0) url = null
            else if ((index % 4) == 0) url = "http://$index"
            som(index, url)
        }
    }
}

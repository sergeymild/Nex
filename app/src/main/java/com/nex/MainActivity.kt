package com.nex

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import java.lang.StringBuilder


class MainActivity : AppCompatActivity() {
    @Debounce(1000)
    fun debo(param: String) {

    }


    @Repeat(every = 2000)
    fun repeatedMethod(isActive: Boolean) {
        System.out.println()
        println("repeat eventy ${System.currentTimeMillis()}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var index = 0
        var url: String? = "http"
        findViewById<View>(R.id.button).setOnClickListener {
            println(index)
            repeatedMethod(index % 2 == 0)
            index += 1
        }
    }
}

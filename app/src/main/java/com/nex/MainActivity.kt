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

@UiThread
fun Context.toast(@StringRes value: Int, duration: Int = Toast.LENGTH_LONG) {
    Toast.makeText(this, value, duration).show()
}

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        var index = 0
        var url: String? = "http"
        findViewById<View>(R.id.button).setOnClickListener {


            Thread {
                applicationContext.toast(R.string.app_name)
            }.start()

        }
    }
}

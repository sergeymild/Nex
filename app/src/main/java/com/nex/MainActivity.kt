package com.nex

import android.content.ClipboardManager
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
private fun Context.getTextFromClipboard(): String? {
    try {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val primaryClip = clipboard.primaryClip ?: return null
        if (primaryClip.itemCount < 1) return null
        val item = primaryClip.getItemAt(0)
        return (item.text ?: item.uri)?.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

class MainActivity : AppCompatActivity() {

    var url: String? = "http"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)





        findViewById<View>(R.id.button).setOnClickListener {

            println(url)
        }
    }
}

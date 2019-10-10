package com.nex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

val staticCacheField: String
    @Memoize get() = "someValueFromStatic"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

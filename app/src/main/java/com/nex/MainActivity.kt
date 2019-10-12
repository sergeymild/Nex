package com.nex

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue


class MainActivity : AppCompatActivity() {
    private val collapsedProgressTranslationY: Float
        @Memoize get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            2f,
            resources.displayMetrics
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}

package com.potados.geomms.common.base

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.potados.geomms.common.extension.resolveThemeColor

/**
 * Base class providing navigation bar coloring.
 * By default, the color is [android.R.attr.windowBackground].
 */
abstract class BaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.systemUiVisibility = when {
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M -> 0
            Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            else -> View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }

        // Some devices don't let you modify android.R.attr.navigationBarColor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window.navigationBarColor = resolveThemeColor(android.R.attr.windowBackground)
        }
    }
}
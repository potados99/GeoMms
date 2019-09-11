
package com.potados.geomms.extension

import android.content.Context
import android.graphics.Color
import android.util.TypedValue


fun Int.dpToPx(context: Context): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).toInt()
}

fun Int.withAlpha(alpha: Int): Int {
    return Color.argb(alpha, Color.red(this), Color.green(this), Color.blue(this))
}

fun Int.forEach(action: (Int) -> Unit) {
    for (index in 0 until this) {
        action(index)
    }
}

fun Float.within(min: Float, max: Float): Float {
    return Math.min(max, Math.max(min, this))
}
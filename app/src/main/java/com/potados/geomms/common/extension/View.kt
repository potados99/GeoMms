/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.common.extension

import android.animation.LayoutTransition
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.potados.geomms.common.widget.CustomBottomSheetBehavior
import timber.log.Timber

fun View.setHidable(hidable: Boolean) {
    CustomBottomSheetBehavior.from(this).isHideable = hidable
}

fun View.hideSheet() {
    CustomBottomSheetBehavior.from(this).state = CustomBottomSheetBehavior.STATE_HIDDEN
}

fun View.collapseSheet() {
    CustomBottomSheetBehavior.from(this).state = CustomBottomSheetBehavior.STATE_COLLAPSED
}

fun View.halfExpandSheet() {
    CustomBottomSheetBehavior.from(this).state = CustomBottomSheetBehavior.STATE_HALF_EXPANDED
}

fun View.expandSheet() {
    CustomBottomSheetBehavior.from(this).state = CustomBottomSheetBehavior.STATE_EXPANDED
}

var View.sheetState: Int
    get() = CustomBottomSheetBehavior.from(this).state
    set(value) {
        CustomBottomSheetBehavior.from(this).state = value
    }

fun View.toggleSheet() {
    CustomBottomSheetBehavior.from(this).apply {
        state = when (state) {
            CustomBottomSheetBehavior.STATE_EXPANDED -> CustomBottomSheetBehavior.STATE_HALF_EXPANDED
            CustomBottomSheetBehavior.STATE_HALF_EXPANDED -> CustomBottomSheetBehavior.STATE_COLLAPSED
            CustomBottomSheetBehavior.STATE_COLLAPSED -> CustomBottomSheetBehavior.STATE_HALF_EXPANDED
            else -> CustomBottomSheetBehavior.STATE_HALF_EXPANDED
        }
    }
}

val View.bottomSheetBehavior
    get() = CustomBottomSheetBehavior.from(this)!!



var ViewGroup.animateLayoutChanges: Boolean
    get() = layoutTransition != null
    set(value) {
        layoutTransition = if (value) LayoutTransition() else null
    }


fun ImageView.setTint(color: Int) {
    imageTintList = ColorStateList.valueOf(color)
}

fun ImageView.setTintRes(@ColorRes colorResId: Int) {
    setTint(resources.getColor(colorResId, null))
}

fun ProgressBar.setTint(color: Int) {
    indeterminateTintList = ColorStateList.valueOf(color)
    progressTintList = ColorStateList.valueOf(color)
}

fun View.setBackgroundTint(color: Int) {

    // API 21 doesn't support this
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
        background?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    backgroundTintList = ColorStateList.valueOf(color)
}

fun View.setPadding(left: Int? = null, top: Int? = null, right: Int? = null, bottom: Int? = null) {
    setPadding(left ?: paddingLeft, top ?: paddingTop, right ?: paddingRight, bottom ?: paddingBottom)
}

var View.isVisible: Boolean
    get() = this.visibility == View.VISIBLE
    set(visible) {
        this.visibility = if (visible) View.VISIBLE else View.GONE
    }

fun View.setVisible(visible: Boolean, invisible: Int = View.GONE) {
    visibility = if (visible) View.VISIBLE else invisible
}

/**
 * If a sheetView captures clicks at all, then the parent won't ever receive touch events. This is a
 * problem when we're trying to capture link clicks, but tapping or long pressing other areas of
 * the sheetView no longer work. Also problematic when we try to long press on an image in the message
 * sheetView
 */
fun View.forwardTouches(parent: View) {
    var isLongClick = false

    setOnLongClickListener {
        isLongClick = true
        true
    }

    setOnTouchListener { v, event ->
        parent.onTouchEvent(event)

        when {
            event.action == MotionEvent.ACTION_UP && isLongClick -> {
                isLongClick = true
                true
            }

            event.action == MotionEvent.ACTION_DOWN -> {
                isLongClick = false
                v.onTouchEvent(event)
            }

            else -> v.onTouchEvent(event)
        }
    }
}

fun ViewPager.addOnPageChangeListener(listener: (Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
        override fun onPageSelected(position: Int) {
            listener(position)
        }
    })
}

fun RecyclerView.scrapViews() {
    val adapter = adapter
    val layoutManager = layoutManager

    this.adapter = null
    this.layoutManager = null

    this.adapter = adapter
    this.layoutManager = layoutManager

    adapter?.notifyDataSetChanged()
}

fun TextView.setBold(bold: Boolean) {
    // TODO default value
    this.setTypeface(this.typeface, if (bold) Typeface.BOLD else Typeface.NORMAL)
}

fun TextView.setTextColorRes(@ColorRes colorResId: Int) {
    resources.getColor(colorResId, null)
}

/**
 * Useful when changing alpha by bottom sheet offset.
 */
fun View.setAlphaByOffset(offset: Float, changeStart: Float = 0.8f) {
    val alphaMax = 1.0f

    val rangeZeroToOne = (1.0f - offset) / (1.0f - changeStart)
    val rangeZeroToAlphaMax = rangeZeroToOne * alphaMax

    alpha = if (offset > changeStart) rangeZeroToAlphaMax else alphaMax
}

/**
 * Useful when changing radius of background drawable by bottom sheet offset.
 * This creates a new drawable for its background.
 */
fun View.setBackgroundRadiusByOffset(offset: Float, changeStart: Float = 0.8f, radiusMax: Float = 30f) {
    val color = Color.parseColor("#EEFFFFFF")

    val rangeZeroToOne = (1.0f - offset) / (1.0f - changeStart)
    val rangeZeroToRadiusMax = rangeZeroToOne * radiusMax

    background = GradientDrawable().apply {
        setColor(color)
        cornerRadius = if (offset > changeStart) rangeZeroToRadiusMax else radiusMax
    }
}

/**
 * Set vertical bias by bottom sheet offset.
 */
fun View.setVerticalBiasByOffset(offset: Float) {
    Timber.i("offset: $offset")
    val param = layoutParams as ConstraintLayout.LayoutParams
    param.verticalBias = offset / 2 + 0.05f
    layoutParams = param
}

fun View.dump() {
    Timber.i("================ View dump start ================")
    Timber.i(flatten().joinToString("\n") { it.toString() })
    Timber.i("================ View dump finished ================")
}

fun View.ids(): List<Int> {
    return flattenMap { it.id }
}

fun <T> View.flattenMap(transform: (View) -> T): List<T> {
    return flatten().map(transform)
}

fun View.flatten(): List<View> {
    return flatten(this)
}

/**
 * Flatten recursive.
 */
fun View.flatten(view: View): List<View> {
    if (view !is ViewGroup) {
        return listOf(view)
    }

    return listOf(view) + List(view.childCount) { view.getChildAt(it) }
        .map { flatten(it) }
        .flatten()
}

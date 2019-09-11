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
import kotlinx.android.synthetic.main.bottom_sheet.view.*
import kotlinx.android.synthetic.main.map_fragment.view.*

fun View.hideSheet() {
    CustomBottomSheetBehavior.from(this).state = CustomBottomSheetBehavior.STATE_HIDDEN
}

fun View.collapseSheet() {
    CustomBottomSheetBehavior.from(this).state = CustomBottomSheetBehavior.STATE_COLLAPSED
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

fun View.bottomSheetBehavior() = CustomBottomSheetBehavior.from(this)



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
 * If a view captures clicks at all, then the parent won't ever receive touch events. This is a
 * problem when we're trying to capture link clicks, but tapping or long pressing other areas of
 * the view no longer work. Also problematic when we try to long press on an image in the message
 * view
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
    val param = layoutParams as ConstraintLayout.LayoutParams
    param.verticalBias = offset / 2 + 0.05f
    layoutParams = param
}
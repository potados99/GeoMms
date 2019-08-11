package com.potados.geomms.common.extension

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import com.potados.geomms.R


/** Returns a [MutableIterator] over the items in this menu. */
operator fun Menu.iterator() = object : MutableIterator<MenuItem> {
    private var index = 0
    override fun hasNext() = index < size()
    override fun next() = getItem(index++) ?: throw IndexOutOfBoundsException()
    override fun remove() = removeItem(--index)
}

fun Menu.setTint(context: Context?, @ColorRes color: Int) {
    context?.let { c ->
        iterator().forEach { item ->
            item.icon.setTint(c.resources.getColor(R.color.primary, c.theme))
        }
    }
}
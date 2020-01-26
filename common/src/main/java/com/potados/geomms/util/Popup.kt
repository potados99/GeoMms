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

/**
 * Copyright (C) 2019 Song Byeong Jun and original authors
 *
 * This file is part of GeoMms.
 *
 * This software makes use of third-party patent which belongs to 
 * KANG MOON KYOU and LEE GWI BONG:
 * System and Method for sharing service of location information
 * 10-1235884-0000 (2013.02.15)
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

package com.potados.geomms.util

import android.app.AlertDialog
import android.content.Context
import android.view.View
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes

class Popup(private val context: Context?) {

    private val dialogBuilder = AlertDialog.Builder(context)
    private val messageBuilder = StringBuilder()

    fun show() {
        if (messageBuilder.isNotEmpty()) {
            dialogBuilder.setMessage(messageBuilder.toString())
        }

        dialogBuilder.show()
    }

    fun withTitle(title: String?): Popup {
        dialogBuilder.setTitle(title)
        return this
    }
    fun withTitle(@StringRes title: Int, vararg formatArgs: Any?): Popup = withTitle(context?.getString(title, *formatArgs))

    fun withMessage(message: String?): Popup {
        messageBuilder.clear()
        messageBuilder.append(message)
        return this
    }
    fun withMessage(@StringRes message: Int, vararg formatArgs: Any?): Popup = withMessage(context?.getString(message, *formatArgs))

    fun withMoreMessage(addition: String?): Popup {
        messageBuilder.append(addition)
        return this
    }
    fun withMoreMessage(@StringRes addition: Int, vararg formatArgs: Any?): Popup = withMoreMessage(context?.getString(addition, *formatArgs))

    fun withNewLine(): Popup = withMoreMessage("\n")

    fun withPositiveButton(text: String?, listener: () -> Unit = {}): Popup {
        dialogBuilder.setPositiveButton(text) { _, _ ->
            listener()
        }
        return this
    }
    fun withPositiveButton(@StringRes text: Int, listener: () -> Unit = {}) = withPositiveButton(context?.getString(text), listener)

    fun withNeutralButton(text: String?, listener: () -> Unit = {}): Popup {
        dialogBuilder.setNeutralButton(text) { _, _ ->
            listener()
        }
        return this
    }
    fun withNeutralButton(@StringRes text: Int, listener: () -> Unit = {}) = withNeutralButton(context?.getString(text), listener)


    fun withNegativeButton(text: String?, listener: () -> Unit = {}): Popup {
        dialogBuilder.setNegativeButton(text) { _, _ ->
            listener()
        }
        return this
    }
    fun withNegativeButton(@StringRes text: Int, listener: () -> Unit = {}) = withNegativeButton(context?.getString(text), listener)

    fun withSingleChoiceItems(
        @ArrayRes itemId: Int,
        defaultSelection: Int = 0,
        listener: (Int) -> Unit = { _ -> }
    ): Popup {
        return this.apply {
            dialogBuilder.setSingleChoiceItems(itemId, defaultSelection) { _, which ->
                listener(which)
            }
        }
    }

    fun withView(view: View): Popup {
        dialogBuilder.setView(view)
        return this
    }

    companion object {
        fun show(context: Context, message: String) {
            AlertDialog.Builder(context).apply {
                setPositiveButton("OK", null)
                setMessage(message)
                show()
            }
        }
    }
}
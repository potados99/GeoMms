package com.potados.geomms.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
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
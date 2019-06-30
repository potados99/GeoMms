package com.potados.geomms.util

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface

/**
 * AlertDialog의 wrapper입니다.
 *
 * 메소드 체인으로 Dialog를 만들어서 띄울 수 있습니다.
 * 아니면 static 메소드를 사용하여 바로 띄울 수도 있습니다.
 */
class Popup(private val context: Context) {

    private val dialogBuilder = AlertDialog.Builder(context).apply {
        setPositiveButton("OK", null)
    }
    private val messageBuilder = StringBuilder()

    fun show() {
        if (messageBuilder.isNotEmpty()) {
            dialogBuilder.setMessage(messageBuilder.toString())
        }

        dialogBuilder.show()
    }

    fun withTitle(title: String): Popup {
        dialogBuilder.setTitle(title)
        return this
    }

    fun withMessage(message: String): Popup {
        messageBuilder.clear()
        messageBuilder.append(message)
        return this
    }

    fun withMoreMessage(addtion: String): Popup {
        messageBuilder.append(addtion)
        return this
    }

    fun withPositiveButton(text: String, listener: DialogInterface.OnClickListener?): Popup {
        dialogBuilder.setPositiveButton(text, listener)
        return this
    }

    companion object {
        /**
         * 위의 것들이 귀찮을 때에 아주 빠르게 쓸 수 있는 간단한 함수입니다.
         * 컨텍스트와 내용을 받아서 다이얼로그를 만들어서 띄웁니다.
         */
        fun show(context: Context, message: String) {
            AlertDialog.Builder(context).apply {
                setPositiveButton("OK", null)
                setMessage(message)
                show()
            }
        }
    }
}
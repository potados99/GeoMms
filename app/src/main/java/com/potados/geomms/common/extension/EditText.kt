package com.potados.geomms.common.extension

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

fun EditText.setOnTextChanged(onChange: (text: CharSequence?) -> Unit) {
    addTextChangedListener(object: TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onChange(s)
        }
    })
}
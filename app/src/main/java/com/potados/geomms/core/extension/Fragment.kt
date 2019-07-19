package com.potados.geomms.core.extension

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

inline fun <reified T: ViewModel> Fragment.viewModel(body: T.() -> Unit): T {
    return ViewModelProviders.of(this).get(T::class.java).apply(body)
}

val Fragment.appContext: Context get() = activity?.applicationContext!!
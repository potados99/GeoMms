package com.potados.geomms.core.extension

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders

inline fun <reified T: ViewModel> FragmentActivity.viewModel(body: T.() -> Unit): T {
    return ViewModelProviders.of(this).get(T::class.java).apply(body)
}
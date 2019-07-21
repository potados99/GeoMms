/**
 * Fragment.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.core.extension

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.ViewModelProviders
import com.potados.geomms.core.platform.BaseActivity
import com.potados.geomms.core.platform.BaseFragment
import kotlinx.android.synthetic.main.base_activity_layout.*

/**
 * Do something in the middle of beginTransaction() and commit().
 */
inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) =
    beginTransaction().func().commit()

/**
 * Get ViewModel of the fragment with ViewModelFactory.
 */
inline fun <reified T : ViewModel> Fragment.getViewModel(factory: Factory, body: T.() -> Unit): T {
    return ViewModelProviders.of(this, factory).get(T::class.java).apply(body)
}

/**
 * Get ViewModel of the fragment without ViewModelFactory.
 */
inline fun <reified T : ViewModel> Fragment.getViewModel(body: T.() -> Unit): T {
    return ViewModelProviders.of(this).get(T::class.java).apply(body)
}

/**
 * For Activity.
 * Get ViewModel of the fragment with ViewModelFactory.
 */
inline fun <reified T : ViewModel> FragmentActivity.getViewModel(factory: Factory, body: T.() -> Unit): T {
    return ViewModelProviders.of(this, factory).get(T::class.java).apply(body)
}

/**
 * For Activity
 * Get ViewModel of the fragment without ViewModelFactory.
 */
inline fun <reified T : ViewModel> FragmentActivity.getViewModel(body: T.() -> Unit): T {
    return ViewModelProviders.of(this).get(T::class.java).apply(body)
}

val BaseFragment.viewContainer: View get() = (activity as BaseActivity).fragment_container
val BaseFragment.appContext: Context get() = activity?.applicationContext!! /* if activity exists, also the context. */
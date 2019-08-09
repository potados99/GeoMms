/**
 * Fragment.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.common.extension

import android.content.Context
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.ViewModelProviders
import com.potados.geomms.common.base.BaseActivity
import com.potados.geomms.common.base.BaseFragment
import kotlinx.android.synthetic.main.base_activity_layout.*

/**
 * Do something in the middle of beginTransaction() and commit().
 */
inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) =
    beginTransaction().func().commit()

/**
 * Do something in the middle of beginTransaction() and commitNow().
 */
inline fun FragmentManager.inImmediateTransaction(func: FragmentTransaction.() -> FragmentTransaction) =
    beginTransaction().func().commitNow()

/**
 * Show only one fragment.
 */
inline fun FragmentManager.showOnly(predicate: (Fragment) -> Boolean): Boolean  {
    inTransaction {
        fragments.forEach { hide(it) }
        fragments.find(predicate)?.let(::show) ?: return false
    }

    return true
}

/**
 * Add multiple fragments. (Collection)
 */
fun FragmentTransaction.addAll(@IdRes containerViewId: Int, fragments: Collection<Fragment>) {
    fragments.forEach {
        add(containerViewId, it)
    }
}

/**
 * Add multiple fragments. (Array)
 */
fun FragmentTransaction.addAll(@IdRes containerViewId: Int, fragments: Array<out Fragment>) {
    fragments.forEach {
        add(containerViewId, it)
    }
}

/**
 * Get ViewModel of the fragment with SingleUseCaseViewModelFactory.
 */
inline fun <reified T : ViewModel> Fragment.getViewModel(factory: Factory, body: T.() -> Unit): T {
    return ViewModelProviders.of(this, factory).get(T::class.java).apply(body)
}

/**
 * Get ViewModel of the fragment without SingleUseCaseViewModelFactory.
 */
inline fun <reified T : ViewModel> Fragment.getViewModel(body: T.() -> Unit): T {
    return ViewModelProviders.of(this).get(T::class.java).apply(body)
}

/**
 * For Activity.
 * Get ViewModel of the fragment with SingleUseCaseViewModelFactory.
 */
inline fun <reified T : ViewModel> FragmentActivity.getViewModel(factory: Factory, body: T.() -> Unit): T {
    return ViewModelProviders.of(this, factory).get(T::class.java).apply(body)
}

/**
 * For Activity
 * Get ViewModel of the fragment without SingleUseCaseViewModelFactory.
 */
inline fun <reified T : ViewModel> FragmentActivity.getViewModel(body: T.() -> Unit): T {
    return ViewModelProviders.of(this).get(T::class.java).apply(body)
}

val BaseFragment.baseActivity: BaseActivity get() = (activity as BaseActivity)
val BaseFragment.viewContainer: View get() = (activity as BaseActivity).fragment_container
val BaseFragment.appContext: Context get() = activity?.applicationContext!!

val BaseFragment.supportActionBar: ActionBar? get() = (activity as BaseActivity).supportActionBar
fun BaseFragment.setSupportActionBar(toolbar: Toolbar) = (activity as BaseActivity).setSupportActionBar(toolbar)
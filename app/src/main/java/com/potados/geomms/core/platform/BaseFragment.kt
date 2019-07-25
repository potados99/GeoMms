/**
 * BaseFragment.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.core.platform

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.provider.Telephony
import android.view.*
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.potados.geomms.R
import com.potados.geomms.core.extension.appContext
import com.potados.geomms.core.extension.baseActivity
import com.potados.geomms.core.extension.setSupportActionBar
import com.potados.geomms.core.extension.viewContainer
import com.potados.geomms.core.platform.interfaces.HasLayout
import com.potados.geomms.core.platform.interfaces.HasSmsReceiver
import com.potados.geomms.core.platform.interfaces.HasToolbar

abstract class BaseFragment : Fragment(),
    HasLayout,  /* layoutId() */
    HasToolbar, /* toolbar(), toolbarMenuId() */
    HasSmsReceiver /* smsReceivedBehavior(), intentFilter() */
{

    /**
     * Default null.
     */
    override fun intentFilter(): IntentFilter? = null
    override fun smsReceivedBehavior(): ((address: String, body: String, date: Long) -> Unit)? = null

    private val receiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (context == null) return
            if (intent == null) return

            val address = intent.getStringExtra(Telephony.Sms.ADDRESS)
            val body = intent.getStringExtra(Telephony.Sms.BODY)
            val date = intent.getLongExtra(Telephony.Sms.DATE, 0)

            smsReceivedBehavior()?.invoke(address, body, date)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(layoutId(), container, false).apply {
            toolbarId()?.let {
                setSupportActionBar(findViewById(it))
                setHasOptionsMenu(true)
            }
        }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) =
        toolbarMenuId()?.let {
            inflater?.inflate(it, menu)
        } ?: super.onCreateOptionsMenu(menu, inflater)

    override fun onStart() {
        super.onStart()

        smsReceivedBehavior()?.let {
            baseActivity.registerReceiver(receiver, intentFilter())
        }
    }

    override fun onStop() {
        super.onStop()

        smsReceivedBehavior()?.let {
            baseActivity.unregisterReceiver(receiver)
        }
    }

    internal fun notify(@StringRes message: Int) =
        Snackbar.make(viewContainer, message, Snackbar.LENGTH_SHORT).show()

    internal fun notify(message: String) =
        Snackbar.make(viewContainer, message, Snackbar.LENGTH_SHORT).show()

    internal fun notifyWithAction(@StringRes message: Int, @StringRes actionText: Int, action: () -> Any) {
        val snackBar = Snackbar.make(viewContainer, message, Snackbar.LENGTH_INDEFINITE)
        snackBar.setAction(actionText) { action.invoke() }
        snackBar.setActionTextColor(ContextCompat.getColor(appContext, R.color.colorPrimary))
        snackBar.show()
    }

    internal fun notifyWithAction(message: String, actionText: String, action: () -> Any) {
        val snackBar = Snackbar.make(viewContainer, message, Snackbar.LENGTH_INDEFINITE)
        snackBar.setAction(actionText) { action.invoke() }
        snackBar.setActionTextColor(ContextCompat.getColor(appContext, R.color.colorPrimary))
        snackBar.show()
    }
}
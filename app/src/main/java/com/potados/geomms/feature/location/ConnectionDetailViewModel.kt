/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
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

package com.potados.geomms.feature.location

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.extension.doAfter
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.feature.location.ConnectionDetailFragment.Companion.ARG_CONNECTION_ID
import com.potados.geomms.model.Connection
import com.potados.geomms.model.Recipient
import com.potados.geomms.preference.MyPreferences
import com.potados.geomms.service.LocationSupportService
import com.potados.geomms.util.Notify
import com.potados.geomms.util.Popup
import io.realm.Realm
import org.koin.core.inject
import timber.log.Timber

class ConnectionDetailViewModel : BaseViewModel() {

    private val service: LocationSupportService by inject()
    private val dateFormatter: DateFormatter by inject()
    private val preferences: MyPreferences by inject()

    private val handler = Handler(Looper.getMainLooper())

    /**
     * Binding elements
     */
    val recipient = MutableLiveData<Recipient>()
    val name = MutableLiveData<String>()
    val status = MutableLiveData<String>()
    val detail = MutableLiveData<String>()
    val positiveButtonText = MutableLiveData<String>()
    val negativeButtonText = MutableLiveData<String>()
    val positiveButtonAlpha = MutableLiveData<Float>().apply { value = 1.0f }

    private lateinit var mConnection: Connection

    init {
        failables += this
        failables += service
        failables += dateFormatter
    }

    fun startWithArguments(fragment: BaseFragment, arguments: Bundle?) {
        arguments ?: return

        val connectionId = arguments
            .getLong(ARG_CONNECTION_ID)
            .takeIf { it != 0L } ?: return

        service.getConnection(connectionId)?.let { connection ->
            setDetails(connection.takeIf { it.isValid })

            // This does not invoke listener right after added.
            connection.addChangeListener<Connection> { changed, _ ->
                if (changed.isValid) {
                    setDetails(changed)

                    Timber.i("Updated mConnection detail.")
                } else {
                    fragment.bottomSheetManager?.pop()
                }
            }
        }
    }

    fun onPositiveButton(fragment: BaseFragment) {
        if (mConnection.recipient == null) {
            fail(R.string.fail_no_recipient, show = true)
            return
        }

        if (mConnection.isTemporal) {
            resendInvitation(fragment, mConnection)
        } else {
            requestUpdate(mConnection)
        }
    }

    private fun resendInvitation(fragment: BaseFragment, temporalConnection: Connection) {
        // Resend invitation.
        val address = temporalConnection.recipient!!.address
        val duration = temporalConnection.duration
        val id = temporalConnection.id

        if (service.requestNewConnectionAgain(address, duration, id)) {
            Notify(fragment.activity).short(R.string.connection_request_sent)
        }
    }

    private fun requestUpdate(connection: Connection) {
        // Connection on sharing
        service.requestUpdate(connection.id)
    }

    fun onNegativeButton(fragment: BaseFragment) {
        if (mConnection.recipient == null) {
            fail(R.string.fail_no_recipient, show = true)
            return
        }

        if (mConnection.isTemporal) {
            askCancelInvitation(fragment, mConnection)
        } else {
            askDisconnect(fragment, mConnection)
        }
    }

    private fun askCancelInvitation(fragment: BaseFragment, temporalConnection: Connection) {
        Popup(fragment.activity)
            .withTitle(R.string.title_cancel_request)
            .withMessage(R.string.dialog_ask_cancel_request, temporalConnection.recipient?.getDisplayName().orEmpty())
            .withPositiveButton(R.string.button_confirm) {
                service.cancelConnectionRequest(temporalConnection)
                fragment.bottomSheetManager?.pop()
            }
            .withNegativeButton(R.string.button_cancel)
            .show()
    }

    private fun askDisconnect(fragment: BaseFragment, connection: Connection) {
        Popup(fragment.activity)
            .withTitle(R.string.title_disconnect)
            .withMessage(R.string.dialog_ask_disconnect, connection.recipient?.getDisplayName().orEmpty())
            .withPositiveButton(R.string.button_confirm) {
                service.requestDisconnect(connection.id)
                fragment.bottomSheetManager?.pop()
            }
            .withNegativeButton(R.string.button_cancel)
            .show()
    }

    private fun setDetails(connection: Connection?) {
        this.mConnection = connection?.takeIf { it.isValid } ?: return

        recipient.value = connection.recipient
        name.value = connection.recipient?.getDisplayName()
        status.value = getStatusString(connection)
        detail.value = getDetailString(connection)
        positiveButtonText.value = getPositiveButtonText(connection)
        negativeButtonText.value = getNegativeButtonText(connection)
        positiveButtonAlpha.value = if (connection.isWaitingForReply) 0.5f else 1.0f
    }

    private fun getStatusString(connection: Connection): String {
        return context.getString(
            when {
                connection.isTemporal -> R.string.dialog_invitation_sent
                connection.lastUpdate == 0L -> R.string.dialog_location_not_available
                else -> R.string.dialog_sharing_location
            }
        )
    }

    private fun getDetailString(connection: Connection): String {
        return when (connection.isTemporal) {
            true -> str(R.string.dialog_connection_id, connection.id.toString()) + "\n" +
                    str(R.string.dialog_sent_at, dateFormatter.getMessageTimestamp(connection.date)) + "\n" +
                    str(R.string.dialog_duration, dateFormatter.getDuration(connection.duration))
            else -> str(R.string.dialog_connection_id, connection.id) + "\n" +
                    str(R.string.dialog_from, dateFormatter.getMessageTimestamp(connection.date)) + "\n" +
                    str(R.string.dialog_until, dateFormatter.getMessageTimestamp(connection.due))
        }
    }

    private fun getPositiveButtonText(connection: Connection): String {
        return str(
            if (connection.isTemporal) R.string.button_resend
            else R.string.button_refresh
        )
    }

    private fun getNegativeButtonText(connection: Connection): String {
        return str(
            if (connection.isTemporal) R.string.button_cancel
            else R.string.button_disconnect
        )
    }

    private fun str(@StringRes res: Int, vararg formatArgs: Any?): String {
        return context.getString(res, *formatArgs)
    }
}
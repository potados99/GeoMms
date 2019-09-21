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
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.feature.location.ConnectionDetailFragment.Companion.ARG_CONNECTION_ID
import com.potados.geomms.model.Connection
import com.potados.geomms.model.Recipient
import com.potados.geomms.service.LocationSupportService
import org.koin.core.inject

class ConnectionDetailViewModel : BaseViewModel() {

    private val service: LocationSupportService by inject()
    private val dateFormatter: DateFormatter by inject()

    init {
        failables += this
        failables += service
        failables += dateFormatter
    }

    fun startWithArguments(arguments: Bundle?) {
        arguments ?: return

        val connectionId = arguments.getLong(ARG_CONNECTION_ID).takeIf { it != 0L } ?: return

        service.getConnection(connectionId)?.let(::setDetails)
    }

    private fun setDetails(connection: Connection) {
        recipient.value = connection.recipient
        name.value = connection.recipient?.getDisplayName()
        status.value = getStatusString(connection)
        detail.value = getDetailString(connection)
        positiveButtonText.value = getPositiveButtonText(connection)
        negativeButtonText.value = getNegativeButtonText(connection)
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
        if (connection.isTemporal) {
            return str(R.string.dialog_connection_id, connection.id.toString()) + "\n" +
                    str(R.string.dialog_sent_at, dateFormatter.getMessageTimestamp(connection.date)) + "\n" +
                    str(R.string.dialog_duration, dateFormatter.getDuration(connection.duration))
        } else {
            return str(R.string.dialog_connection_id, connection.id) + "\n" +
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


    fun str(@StringRes res: Int, vararg formatArgs: Any?): String {
        return context.getString(res, *formatArgs)
    }

    /**
     * Binding elements
     */
    val recipient = MutableLiveData<Recipient>()
    val name = MutableLiveData<String>()
    val status = MutableLiveData<String>()
    val detail = MutableLiveData<String>()
    val positiveButtonText = MutableLiveData<String>()
    val negativeButtonText = MutableLiveData<String>()
}
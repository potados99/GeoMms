/*
 * Copyright (C) 2019-2020 Song Byeong Jun <potados99@gmail.com>
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
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.feature.location.RequestDetailFragment.Companion.ARG_REQUEST_ID
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Recipient
import com.potados.geomms.preference.MyPreferences
import com.potados.geomms.service.LocationSupportService
import org.koin.core.inject
import timber.log.Timber

class RequestDetailViewModel : BaseViewModel() {
    private val service: LocationSupportService by inject()
    private val dateFormatter: DateFormatter by inject()

    /**
     * Binding elements
     */
    val recipient = MutableLiveData<Recipient>()
    val name = MutableLiveData<String>()
    val detail = MutableLiveData<String>()

    private lateinit var mRequest: ConnectionRequest

    init {
        failables += this
        failables += service
        failables += dateFormatter
    }

    fun startWithArguments(fragment: BaseFragment, arguments: Bundle?) {
        arguments ?: return

        val requestId = arguments
            .getLong(ARG_REQUEST_ID)
            .takeIf { it != 0L } ?: return

        service.getRequest(requestId, inbound = true)?.let { request ->
            setDetails(request.takeIf { it.isValid })

            // This does not invoke listener right after added.
            request.addChangeListener<ConnectionRequest> { changed, _ ->
                if (changed.isValid) {
                    setDetails(changed)

                    Timber.i("Updated mConnection detail.")
                } else {
                    fragment.bottomSheetManager?.pop()
                }
            }
        }
    }

    fun onAccept() {
        service.acceptConnectionRequest(mRequest)
    }

    fun onRefuse() {
        service.refuseConnectionRequest(mRequest)
    }

    private fun setDetails(request: ConnectionRequest?) {
        if (request == null || !request.isValid) {
            return
        }

        this.mRequest = request

        recipient.value = request.recipient
        name.value = request.recipient?.getDisplayName()
        detail.value = getDetailString(request)
    }

    private fun getDetailString(request: ConnectionRequest): String {
        return str(R.string.dialog_connection_id, request.connectionId.toString()) + "\n" +
                str(R.string.dialog_sent_at, dateFormatter.getMessageTimestamp(request.date)) + "\n" +
                str(R.string.dialog_duration, dateFormatter.getDuration(request.duration))
    }

    private fun str(@StringRes res: Int, vararg formatArgs: Any?): String {
        return context.getString(res, *formatArgs)
    }
}
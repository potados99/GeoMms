package com.potados.geomms.feature.location

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.lifecycle.MutableLiveData
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.base.BaseViewModel
import com.potados.geomms.common.util.DateFormatter
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
            .getLong(RequestDetailFragment.ARG_REQUEST_ID)
            .takeIf { it != 0L } ?: return

        service.getRequest(requestId, inbound = true)?.let { request ->
            setDetails(request.takeIf { it.isValid })

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
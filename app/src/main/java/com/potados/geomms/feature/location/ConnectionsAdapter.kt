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

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseRealmAdapter
import com.potados.geomms.common.base.BaseViewHolder
import com.potados.geomms.common.extension.doAfter
import com.potados.geomms.common.extension.doEvery
import com.potados.geomms.common.extension.setVisible
import com.potados.geomms.common.util.DateFormatter
import com.potados.geomms.model.Connection
import com.potados.geomms.preference.MyPreferences
import io.realm.Realm
import kotlinx.android.synthetic.main.connection_list_item.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class ConnectionsAdapter : BaseRealmAdapter<Connection>(), KoinComponent {

    private val preferences: MyPreferences by inject()
    private val dateFormatter: DateFormatter by inject()

    private val handler = Handler(Looper.getMainLooper())

    var onConnectionClick: (Connection) -> Unit = {}
    var onRefreshClick: (Connection) -> Unit = {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.connection_list_item, parent, false)

        return TimerViewHolder(view).apply {
            view.setOnClickListener {
                val item = getItem(adapterPosition) ?: return@setOnClickListener
                onConnectionClick(item)
            }
            view.refresh_button.setOnClickListener {
                val item = getItem(adapterPosition) ?: return@setOnClickListener
                onRefreshClick(item)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        val view = holder.containerView
        val item = getItem(position).takeIf { it?.isValid == true } ?: return

        val alpha = if (item.isTemporal) 0.5f else 1.0f
        val timeLeftVisibility = !item.isTemporal
        val refreshVisibility = !item.isTemporal

        with(view.name) {
            text = item.recipient?.getDisplayName()
            setAlpha(alpha)
        }

        with(view.status) {
            setAlpha(alpha)

            text = when (item.isTemporal) {
                true -> context.getString(R.string.connection_request_sent)

                false -> when (item.lastUpdate) {
                    0L -> {
                        context.getString(R.string.connection_location_not_available)
                    }
                    else -> {
                        val update = dateFormatter.getConversationTimestamp(item.lastUpdate)
                        context.getString(R.string.connection_last_update, update)
                    }
                }
            }
        }

        with(view.avatar) {
            setContact(item.recipient)
        }

        with(view.duration_group) {
            setVisible(timeLeftVisibility)
        }

        with(view.time_label) {
            text = when (item.isTemporal) {
                true -> null
                false -> {
                    val duration = dateFormatter.getDuration(item.timeLeft, short = true)
                    context.getString(R.string.connection_time_left, duration)
                }
            }
        }

        with(view.refresh_button) {
            setVisible(refreshVisibility)
            setAlpha(if (item.isWaitingForReply) 0.5f else 1.0f)
            isClickable = !item.isWaitingForReply
        }

        // Refresh left time every second.
        (holder as TimerViewHolder).apply {
            timer?.cancel()

            if (timeLeftVisibility) {
                timer = Timer()
                timer?.doEvery(1000) {
                    handler.post {
                        if (!item.isValid) {
                            timer = null
                            return@post
                        }

                        val duration = dateFormatter.getDuration(item.timeLeft, short = true)

                        with(view.time_label) {
                            text = context.getString(R.string.connection_time_left, duration)
                        }
                    }
                }
            }
        }
    }

    class TimerViewHolder(view: View) : BaseViewHolder(view) {
        var timer: Timer? = null
    }
}

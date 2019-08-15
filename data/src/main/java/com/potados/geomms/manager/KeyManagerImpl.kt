/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.potados.geomms.manager

import android.util.SparseArray
import com.potados.geomms.model.Connection
import com.potados.geomms.model.ConnectionRequest
import com.potados.geomms.model.Message
import io.realm.Realm
import kotlin.math.max
import kotlin.random.Random

class KeyManagerImpl: KeyManager {

    data class Channel(var initialized: Boolean, var maxId: Long)

    private val channels = SparseArray<Channel>().apply {
        append(CHANNEL_MESSAGE, Channel(true, 0))
    }

    override fun reset(channel: Int) {
        channels[channel]?.apply {
            initialized = true
            maxId = 0
        }
    }

    override fun newId(channel: Int): Long {

        val selectedChannel = channels[channel] ?: throw IllegalArgumentException("no such channel as $channel.")

        if (!selectedChannel.initialized) {

            selectedChannel.maxId = when (channel) {
                CHANNEL_MESSAGE -> Realm.getDefaultInstance().use { realm ->
                    realm.where(Message::class.java).max("id")?.toLong() ?: 0L
                }

                else -> throw IllegalArgumentException("no such channel as $channel.")
            }

            selectedChannel.initialized = true
        }

        return ++selectedChannel.maxId
    }

    override fun randomId(max: Long): Long {
        return Random(System.currentTimeMillis()).nextLong(1, max)
    }

    companion object {
        const val CHANNEL_MESSAGE = 1
        // const val CHANNEL_CONNECTION = 2
    }
}
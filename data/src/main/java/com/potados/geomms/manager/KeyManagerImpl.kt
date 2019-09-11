
package com.potados.geomms.manager

import android.util.SparseArray
import com.potados.geomms.model.Message
import io.realm.Realm
import kotlin.random.Random

class KeyManagerImpl: KeyManager() {

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

        if (selectedChannel.initialized) {

            selectedChannel.maxId = when (channel) {
                CHANNEL_MESSAGE -> Realm.getDefaultInstance().use { realm ->
                    realm.where(Message::class.java).max("id")?.toLong() ?: 0L
                }

                else -> throw IllegalArgumentException("No such channel as $channel.")
            }

            selectedChannel.initialized = false
        }

        selectedChannel.maxId += 1

        return selectedChannel.maxId
    }

    override fun randomId(max: Long): Long {
        return Random(System.currentTimeMillis()).nextLong(1, max)
    }

    companion object {
        const val CHANNEL_MESSAGE = 1
        // const val CHANNEL_CONNECTION = 2
    }
}
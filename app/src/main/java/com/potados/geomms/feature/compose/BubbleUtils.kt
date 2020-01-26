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

package com.potados.geomms.feature.compose

import com.potados.geomms.R
import com.potados.geomms.model.Message
import java.util.concurrent.TimeUnit

class BubbleUtils {
    companion object {
        const val TIMESTAMP_THRESHOLD = 10

        fun canGroup(message: Message, other: Message?): Boolean {
            if (other == null) return false
            val diff = TimeUnit.MILLISECONDS.toMinutes(Math.abs(message.date - other.date))
            return message.compareSender(other) && diff < TIMESTAMP_THRESHOLD
        }

        fun getBubble(canGroupWithPrevious: Boolean, canGroupWithNext: Boolean, isMe: Boolean): Int {
            return when {
                !canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_out_first else R.drawable.message_in_first
                canGroupWithPrevious && canGroupWithNext -> if (isMe) R.drawable.message_out_middle else R.drawable.message_in_middle
                canGroupWithPrevious && !canGroupWithNext -> if (isMe) R.drawable.message_out_last else R.drawable.message_in_last
                else -> R.drawable.message_only
            }
        }
    }
}
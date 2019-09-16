
package com.potados.geomms.mapper

import android.database.Cursor
import com.potados.geomms.model.Conversation

interface CursorToConversation : Mapper<Cursor, Conversation> {
    fun getConversationsCursor(dateFrom: Long = 0): Cursor?
}

package com.potados.geomms.mapper

import android.database.Cursor
import com.potados.geomms.model.Recipient

interface CursorToRecipient : Mapper<Cursor, Recipient> {

    fun getRecipientCursor(): Cursor?

    fun getRecipientCursor(id: Long): Cursor?

}
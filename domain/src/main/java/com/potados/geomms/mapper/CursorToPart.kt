
package com.potados.geomms.mapper

import android.database.Cursor
import com.potados.geomms.model.MmsPart

interface CursorToPart : Mapper<Cursor, MmsPart> {

    fun getPartsCursor(messageId: Long): Cursor?

}
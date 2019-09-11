
package com.potados.geomms.mapper

import android.database.Cursor
import com.potados.geomms.model.Contact

interface CursorToContact : Mapper<Cursor, Contact> {


    fun getContactsCursor(): Cursor?

}

package com.potados.geomms.feature.compose.part

import android.view.View
import com.potados.geomms.model.Message
import com.potados.geomms.model.MmsPart

interface PartBinder {

    val partLayout: Int

    fun canBindPart(part: MmsPart): Boolean

    fun bindPart(view: View, part: MmsPart, message: Message, canGroupWithPrevious: Boolean, canGroupWithNext: Boolean)

}
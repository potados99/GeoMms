
package com.potados.geomms.feature.compose.part

import android.content.ContentUris
import android.content.Context
import android.view.View
import com.potados.geomms.R
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.extension.isVCard
import com.potados.geomms.feature.compose.BubbleUtils
import com.potados.geomms.mapper.CursorToPartImpl
import com.potados.geomms.model.Message
import com.potados.geomms.model.MmsPart
import ezvcard.Ezvcard
import kotlinx.android.synthetic.main.mms_vcard_list_item.view.*

class VCardBinder(
    private val context: Context,
    private val navigator: Navigator
) : PartBinder {

    override val partLayout = R.layout.mms_vcard_list_item

    override fun canBindPart(part: MmsPart) = part.isVCard()

    override fun bindPart(
        view: View,
        part: MmsPart,
        message: Message,
        canGroupWithPrevious: Boolean,
        canGroupWithNext: Boolean
    ) {
        val uri = ContentUris.withAppendedId(CursorToPartImpl.CONTENT_URI, part.id)
        val bubble = BubbleUtils.getBubble(canGroupWithPrevious, canGroupWithNext, message.isMe())

        view.setOnClickListener { navigator.saveVcard(uri) }
        view.vcard_background.setBackgroundResource(bubble)

        // TODO: do on background
        context.contentResolver.openInputStream(uri)
            .use { Ezvcard.parse(it).first() }
            .let { view.name?.text = it.formattedName.value }

    }

}
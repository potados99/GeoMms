
package com.potados.geomms.feature.compose.part

import android.content.Context
import android.view.View
import com.bumptech.glide.Glide
import com.potados.geomms.R
import com.potados.geomms.common.extension.setVisible
import com.potados.geomms.common.navigation.Navigator
import com.potados.geomms.common.widget.BubbleImageView
import com.potados.geomms.extension.isImage
import com.potados.geomms.extension.isVideo
import com.potados.geomms.model.Message
import com.potados.geomms.model.MmsPart
import kotlinx.android.synthetic.main.mms_preview_list_item.view.*

class MediaBinder(private val context: Context, private val navigator: Navigator) : PartBinder {

    override val partLayout = R.layout.mms_preview_list_item

    override fun canBindPart(part: MmsPart) = part.isImage() || part.isVideo()

    override fun bindPart(view: View, part: MmsPart, message: Message, canGroupWithPrevious: Boolean, canGroupWithNext: Boolean) {
        view.video.setVisible(part.isVideo())
        view.setOnClickListener { navigator.showMedia(part.id) }

        view.thumbnail.bubbleStyle = when {
            !canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_FIRST else BubbleImageView.Style.IN_FIRST
            canGroupWithPrevious && canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_MIDDLE else BubbleImageView.Style.IN_MIDDLE
            canGroupWithPrevious && !canGroupWithNext -> if (message.isMe()) BubbleImageView.Style.OUT_LAST else BubbleImageView.Style.IN_LAST
            else -> BubbleImageView.Style.ONLY
        }

        Glide.with(context).load(part.getUri()).fitCenter().into(view.thumbnail)
    }

}
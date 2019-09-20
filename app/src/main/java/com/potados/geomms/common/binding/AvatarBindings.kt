package com.potados.geomms.common.binding

import androidx.databinding.BindingAdapter
import com.potados.geomms.common.widget.AvatarView
import com.potados.geomms.model.Recipient

@BindingAdapter("recipient")
fun setContact(avatar: AvatarView, recipient: Recipient?) {
    avatar.setContact(recipient)
}
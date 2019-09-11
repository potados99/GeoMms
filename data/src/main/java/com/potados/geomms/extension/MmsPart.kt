
package com.potados.geomms.extension

import com.google.android.mms.ContentType
import com.potados.geomms.model.MmsPart

fun MmsPart.isSmil() = ContentType.APP_SMIL == type

fun MmsPart.isImage() = ContentType.isImageType(type)

fun MmsPart.isVideo() = ContentType.isVideoType(type)

fun MmsPart.isText() = ContentType.isTextType(type)

fun MmsPart.isVCard() = ContentType.TEXT_VCARD == type

package com.potados.geomms.model

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.view.inputmethod.InputContentInfoCompat

sealed class Attachment {

    data class Image(
        private val uri: Uri? = null,
        private val inputContent: InputContentInfoCompat? = null
    ) : Attachment() {

        fun getUri(): Uri? {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                inputContent?.contentUri ?: uri
            } else {
                uri
            }
        }

        fun isGif(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1 && inputContent != null) {
                inputContent.description.hasMimeType("image/gif")
            } else {
                if (uri == null) false
                else context.contentResolver.getType(uri) == "image/gif"
            }
        }
    }

    data class Contact(val vCard: String) : Attachment()
}

class Attachments(attachments: List<Attachment>) : List<Attachment> by attachments


package com.potados.geomms.repository

import android.graphics.Bitmap
import android.net.Uri

abstract class ImageRepository : Repository() {
    abstract fun loadImage(uri: Uri): Bitmap?

    abstract fun saveImage(uri: Uri)
}
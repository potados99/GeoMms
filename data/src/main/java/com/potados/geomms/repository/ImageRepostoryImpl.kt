package com.potados.geomms.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Environment
import androidx.annotation.StringRes
import androidx.exifinterface.media.ExifInterface
import com.potados.geomms.base.Failable
import com.potados.geomms.data.R
import com.potados.geomms.extension.nullOnFail
import com.potados.geomms.extension.unitOnFail
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class ImageRepostoryImpl(
    private val context: Context
) : ImageRepository() {

    override fun loadImage(uri: Uri): Bitmap? = nullOnFail {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw RuntimeException("Failed to open input stream.")

        val exif = ExifInterface(inputStream)
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(uri))
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        return@nullOnFail when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
            else -> bitmap
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap? = nullOnFail {
        val w = bitmap.width
        val h = bitmap.height

        val mtx = Matrix()
        mtx.postRotate(degree)

        return@nullOnFail Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true)
    }

    override fun saveImage(uri: Uri) = unitOnFail {
        val type = context.contentResolver.getType(uri)?.split("/") ?: return@unitOnFail
        val dir = File(Environment.getExternalStorageDirectory(), "QKSMS").apply { mkdirs() }
        val file = File(dir, "${type.first()}${System.currentTimeMillis()}.${type.last()}")

        try {
            val outputStream = FileOutputStream(file)
            val inputStream = context.contentResolver.openInputStream(uri)

            inputStream?.copyTo(outputStream, 1024)

            inputStream?.close()
            outputStream.close()
        } catch (e: Exception) {
            setFailure(Failable.Failure(context.getString(R.string.fail_stream_io), show = false))
        }

        MediaScannerConnection.scanFile(context, arrayOf(file.path), null, null)
    }
}

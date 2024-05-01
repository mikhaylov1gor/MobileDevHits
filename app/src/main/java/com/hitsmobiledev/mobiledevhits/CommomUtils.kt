package com.hitsmobiledev.mobiledevhits

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

private fun getBitmapFromUri(uri: Uri, context: Context): Bitmap {
    val bitmap: Bitmap
    if ("content".equals(uri.scheme, ignoreCase = true)) {
        val inputStream = context.contentResolver.openInputStream(uri)
        bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream!!.close()
    } else if ("file".equals(uri.scheme, ignoreCase = true)) {
        bitmap = BitmapFactory.decodeFile(uri.path)
    } else {
        bitmap = BitmapFactory.decodeResource(context.resources, uri.lastPathSegment!!.toInt())
    }
    return bitmap
}

private fun getByteCacheFromBitmap(bitmap: Bitmap, context: Context): File {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
    val byteArray = stream.toByteArray()

    val outputFile = File(context.cacheDir, "bitmap.tmp")
    FileOutputStream(outputFile).use { fos ->
        fos.write(byteArray)
    }

    return outputFile
}

private fun getBitmapFromCachedByteArray(context: Context, cacheFileName: String): Bitmap {
    val cacheFile = File(context.cacheDir, cacheFileName)
    val inputStream = FileInputStream(cacheFile)

    val buffer = ByteArray(inputStream.available())
    inputStream.read(buffer)
    inputStream.close()

    return BitmapFactory.decodeByteArray(buffer, 0, buffer.size)
}


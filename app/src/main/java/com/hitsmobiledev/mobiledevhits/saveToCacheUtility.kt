package com.hitsmobiledev.mobiledevhits

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val cacheDir = context.cacheDir
    val filename = UUID.randomUUID().toString()
    val file = File(cacheDir, "$filename.png")
    try {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return Uri.fromFile(file)
}

fun deleteFileFromCache(uri: Uri) {
    val file = uri.path?.let { File(it) }
    if (file != null) {
        if (file.exists()) {
            file.delete()
        }
    }
}
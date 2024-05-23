package com.hitsmobiledev.mobiledevhits
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun saveBitmapToCache(context: Context, bitmap: Bitmap, filename: String): Uri {
    val cacheDir = context.cacheDir
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
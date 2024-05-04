package com.hitsmobiledev.mobiledevhits

import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.truncate

class BlurActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var maskButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_blur)

        imageView = findViewById(R.id.currentPhoto)

        val imageUri = intent.getParcelableExtra<Uri>("currentPhoto")
        imageView.setImageURI(imageUri)

        maskButton = findViewById(R.id.mask_button)
        maskButton.setOnClickListener() {
            val bitmap =
                MediaStore.Images.Media.getBitmap(this@BlurActivity.contentResolver, imageUri)

            imageView.setImageBitmap(unshapredMask(bitmap, 2))
        }
    }

    private fun gaussFilter(imageBitmap: Bitmap, radius: Int): IntArray {
        val width = imageBitmap.width
        val height = imageBitmap.height
        val pixels = IntArray(width * height)
        val sigma = radius.toFloat() / 2
        val windowSize = radius
        val window = FloatArray(2 * windowSize + 1)

        window[windowSize] = 1.0f
        for (i in 1..windowSize) {
            val expVal = exp(-i * i / (2 * sigma * sigma))
            window[windowSize + i] = expVal
            window[windowSize - i] = expVal
        }

        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val blurredPixels = IntArray(width * height)

        for (y in 0 until height) {
            for (x in 0 until width) {
                var sumRed = 0.0f
                var sumGreen = 0.0f
                var sumBlue = 0.0f
                var sumWeight = 0.0f

                for (i in -windowSize..windowSize) {
                    val currentX = x + i
                    if (currentX in 0 until width) {
                        val color = pixels[y * width + currentX]
                        val weight = window[windowSize + i]
                        sumRed += Color.red(color) * weight
                        sumGreen += Color.green(color) * weight
                        sumBlue += Color.blue(color) * weight
                        sumWeight += weight
                    }
                }

                val newRed = (sumRed / sumWeight).toInt().coerceIn(0, 255)
                val newGreen = (sumGreen / sumWeight).toInt().coerceIn(0, 255)
                val newBlue = (sumBlue / sumWeight).toInt().coerceIn(0, 255)

                blurredPixels[y * width + x] = Color.rgb(newRed, newGreen, newBlue)
            }
        }

        return blurredPixels
    }

    private fun unshapredMask(bitmap: Bitmap, amount: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        val outputPixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val blurredPixels = gaussFilter(bitmap, 3)
        val mask = getMask(blurredPixels, pixels)

        for (i in pixels.indices) {
            val originalColor = pixels[i]
            val maskColor = mask[i]

            val newRed = (Color.red(originalColor) + amount * Color.red(maskColor)).coerceIn(0, 255)
            val newGreen =
                (Color.green(originalColor) + amount * Color.green(maskColor)).coerceIn(0, 255)
            val newBlue =
                (Color.blue(originalColor) + amount * Color.blue(maskColor)).coerceIn(0, 255)

            outputPixels[i] = Color.rgb(newRed, newGreen, newBlue)
        }

        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        outputBitmap.setPixels(outputPixels, 0, width, 0, 0, width, height)

        return outputBitmap
    }

    private fun getMask(blurredPixels: IntArray, originalPixels: IntArray): IntArray {
        val mask = IntArray(originalPixels.size)

        for (i in originalPixels.indices) {
            val sharpColor = blurredPixels[i]
            val originalColor = originalPixels[i]

            val newRed = Color.red(originalColor) - Color.red(sharpColor)
            val newGreen = Color.green(originalColor) - Color.green(sharpColor)
            val newBlue = Color.blue(originalColor) - Color.blue(sharpColor)

            mask[i] = Color.rgb(newRed, newGreen, newBlue)
        }

        return mask
    }
}

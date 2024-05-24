package com.hitsmobiledev.mobiledevhits

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import com.google.android.material.slider.Slider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.truncate

class BlurActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var maskButton: Button
    private lateinit var tresholdSlider: Slider
    private lateinit var radiusSlider: Slider
    private lateinit var amountSlider: Slider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_blur)

        imageView = findViewById(R.id.currentPhoto)

        val imageUri = intent.getParcelableExtra<Uri>("currentPhoto")
        imageView.setImageURI(imageUri)

        tresholdSlider = findViewById(R.id.treshold_slider)
        radiusSlider = findViewById(R.id.radius_slider)
        amountSlider = findViewById(R.id.amount_slider)

        maskButton = findViewById(R.id.mask_button)
        var finalBitmap : Bitmap = MediaStore.Images.Media.getBitmap(this@BlurActivity.contentResolver, imageUri)
        maskButton.setOnClickListener() {
            val bitmap =
                MediaStore.Images.Media.getBitmap(this@BlurActivity.contentResolver, imageUri)


            CoroutineScope(Dispatchers.Main).launch {

                val masked = unsharpenMask(bitmap, amountSlider.value, radiusSlider.value.toInt(), tresholdSlider.value.toInt())
                imageView.setImageBitmap(masked)
                finalBitmap = masked
            }
        }

        val saveChangesButton: ImageButton = findViewById(R.id.button_save_changes)
        saveChangesButton.setOnClickListener {
            val newUri = saveBitmapToCache(this, finalBitmap)
            val intent = Intent(this@BlurActivity, ChooseFilterActivity::class.java)
            intent.putExtra("currentPhoto", newUri)
            if (imageUri != null) {
                deleteFileFromCache(imageUri)
            }
            this@BlurActivity.startActivity(intent)
        }
    }

    private suspend fun gaussFilter(pixels: IntArray, width: Int, height: Int, radius: Int): IntArray = coroutineScope {
        val sigma = radius.toFloat() / 2
        val windowSize = radius
        val window = FloatArray(2 * windowSize + 1)

        window[windowSize] = 1.0f
        for (i in 1..windowSize) {
            val expVal = exp(-i * i / (2 * sigma * sigma))
            window[windowSize + i] = expVal
            window[windowSize - i] = expVal
        }

        val blurredPixels = IntArray(width * height)
        val jobs = mutableListOf<Job>()

        for (y in 0 until height) {
            val job = launch(Dispatchers.Default) {
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
            jobs.add(job)
        }

        jobs.joinAll()
        return@coroutineScope blurredPixels
    }

    suspend fun unsharpenMask(bitmap: Bitmap, amount: Float, radius: Int, treshold: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        val mask = getMask(gaussFilter(pixels, width, height, radius), pixels, treshold)
        val outputPixels = IntArray(width * height)

        for (i in pixels.indices) {
            val originalColor = pixels[i]
            val maskColor = mask[i]

            val newAlpha = Color.alpha(originalColor)
            val newRed = (Color.red(originalColor) + amount * Color.red(maskColor)).toInt().coerceIn(0, 255)
            val newGreen = (Color.green(originalColor) + amount * Color.green(maskColor)).toInt().coerceIn(0, 255)
            val newBlue = (Color.blue(originalColor) + amount * Color.blue(maskColor)).toInt().coerceIn(0, 255)

            outputPixels[i] = Color.argb(newAlpha, newRed, newGreen, newBlue)
        }

        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        outputBitmap.setPixels(outputPixels, 0, width, 0, 0, width, height)

        return outputBitmap
    }

    private fun getMask(blurredPixels: IntArray, originalPixels: IntArray, treshold: Int): IntArray {
        val mask = IntArray(originalPixels.size)

        for (i in originalPixels.indices) {
            val sharpColor = blurredPixels[i]
            val originalColor = originalPixels[i]

            val diffAlpha = Color.alpha(originalColor)
            val diffRed = Color.red(originalColor) - Color.red(sharpColor)
            val diffGreen = Color.green(originalColor) - Color.green(sharpColor)
            val diffBlue = Color.blue(originalColor) - Color.blue(sharpColor)

            val newAlpha = diffAlpha
            val newRed = if (abs(diffRed) > treshold) diffRed else 0
            val newGreen = if (abs(diffGreen) > treshold) diffGreen else 0
            val newBlue = if (abs(diffBlue) > treshold) diffBlue else 0

            mask[i] = Color.argb(newAlpha, newRed, newGreen, newBlue)
        }

        return mask
    }
}
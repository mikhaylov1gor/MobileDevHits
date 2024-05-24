package com.hitsmobiledev.mobiledevhits

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import kotlin.math.abs
import kotlin.math.exp


class ColorCorrectionActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri
    private lateinit var mosaicSizeSeekBar: SeekBar
    private lateinit var contrastSeekBar: SeekBar
    private lateinit var gaussSeekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_color_correction)

        imageView = findViewById(R.id.ColorFilterPhoto)
        imageUri = intent.getParcelableExtra("currentPhoto")!!
        imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageView.setImageBitmap(imageBitmap)

        var currentBitmap: Bitmap = imageBitmap

        // button listeners
        val saveChangesButton: ImageButton = findViewById(R.id.button_save_changes)
        saveChangesButton.setOnClickListener {
            saveChanges(currentBitmap)
        }

        val returnToFiltersButton: ImageButton = findViewById(R.id.button_redu)
        returnToFiltersButton.setOnClickListener {
            returnToFilters()
        }

        // default filter
        val defaultFilterButton: ImageButton = findViewById(R.id.button_standart_filter)
        defaultFilterButton.setOnClickListener {
            hideOtherFilterParameters()
            defaultFilter(imageBitmap)
        }

        // negative filter
        val negativeFilterButton: ImageButton = findViewById(R.id.button_first_filter)
        negativeFilterButton.setOnClickListener {
            hideOtherFilterParameters()
            currentBitmap = negativeFilter(imageBitmap)
        }

        // mosaic filter
        val mosaicFilterButton: ImageButton = findViewById(R.id.button_second_filter)
        mosaicSizeSeekBar = findViewById(R.id.mosaicSeekBar)
        mosaicFilterButton.setOnClickListener {
            hideOtherFilterParameters()
            mosaicSizeSeekBar.visibility = View.VISIBLE
            mosaicFilter(imageBitmap, mosaicSizeSeekBar.progress)
        }

        // contrast filter
        val contrastFilterButton: ImageButton = findViewById(R.id.button_third_filter)
        contrastSeekBar = findViewById(R.id.contrastSeekBar)
        contrastFilterButton.setOnClickListener {
            hideOtherFilterParameters()
            contrastSeekBar.visibility = View.VISIBLE
            contrastFilter(imageBitmap, contrastSeekBar.progress)
        }

        // median filter
        val medianFilterButton: ImageButton = findViewById(R.id.button_fourth_filter)
        medianFilterButton.setOnClickListener {
            hideOtherFilterParameters()
            medianFilter(imageBitmap, 7)
        }

        // gauss filter
        val gaussFilterButton: ImageButton = findViewById(R.id.button_fifth_filter)
        gaussSeekBar = findViewById(R.id.gaussSeekBar)
        gaussFilterButton.setOnClickListener {
            hideOtherFilterParameters()
            gaussSeekBar.visibility = View.VISIBLE
            gaussFilter(imageBitmap, gaussSeekBar.progress)
        }
    }

    private fun hideOtherFilterParameters() {
        mosaicSizeSeekBar.visibility = View.GONE
        contrastSeekBar.visibility = View.GONE
        gaussSeekBar.visibility = View.GONE

    }

    private fun returnToFilters() {
        val intent = Intent(this@ColorCorrectionActivity, ChooseFilterActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        this@ColorCorrectionActivity.startActivity(intent)
    }

    private fun saveChanges(currentBitmap: Bitmap) {
        returnToFilters()
    }


    private fun defaultFilter(imageBitmap: Bitmap) {
        imageView.setImageBitmap(imageBitmap)
    }

    private fun negativeFilter(imageBitmap: Bitmap): Bitmap {
        val width = imageBitmap.width
        val height = imageBitmap.height

        val pixels = IntArray(width * height)

        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            pixels[i] = pixels[i] xor 0x00FFFFFF
        }

        val negativeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        negativeBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        imageView.setImageBitmap(negativeBitmap)

        return negativeBitmap
    }

    private fun mosaicFilter(imageBitmap: Bitmap, blockSize: Int) {
        val width = imageBitmap.width
        val height = imageBitmap.height

        val pixels = IntArray(width * height)

        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (x in 0 until width step blockSize) {
            for (y in 0 until height step blockSize) {
                var sumRed = 0
                var sumGreen = 0
                var sumBlue = 0
                var count = 0

                for (i in x until minOf(x + blockSize, width)) {
                    for (j in y until minOf(y + blockSize, height)) {
                        val color = pixels[j * width + i]

                        sumRed += Color.red(color)
                        sumGreen += Color.green(color)
                        sumBlue += Color.blue(color)
                        count++
                    }
                }

                val blockColor = Color.rgb((sumRed / count), (sumGreen / count), (sumBlue / count))

                for (i in x until minOf(x + blockSize, width)) {
                    for (j in y until minOf(y + blockSize, height)) {
                        pixels[j * width + i] = blockColor
                    }
                }
            }
        }

        val mosaicBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mosaicBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        imageView.setImageBitmap(mosaicBitmap)
    }


    private fun trunk(num: Float): Float {
        return if (num > 255) {
            255f
        } else if (num < 0) {
            0f
        } else {
            num
        }
    }

    private fun contrastFilter(imageBitmap: Bitmap, contrast: Int) {
        val factor: Float = (259 * (contrast.toFloat() + 255)) / (255 * (259 - contrast.toFloat()))

        val width = imageBitmap.width
        val height = imageBitmap.height

        val pixels = IntArray(width * height)

        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (i in pixels.indices) {
            val red = Color.red(pixels[i])
            val green = Color.green(pixels[i])
            val blue = Color.blue(pixels[i])

            val newRed = trunk(factor * (red.toFloat() - 128) + 128)
            val newGreen = trunk(factor * (green - 128) + 128)
            val newBlue = trunk(factor * (blue - 128) + 128)

            pixels[i] = Color.argb(255, newRed.toInt(), newGreen.toInt(), newBlue.toInt())
        }

        val contrastBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        contrastBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        imageView.setImageBitmap(contrastBitmap)
    }

    private fun medianFilter(imageBitmap: Bitmap, windowSize: Int) {
        val width = imageBitmap.width
        val height = imageBitmap.height
        val pixels = IntArray(width * height)

        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val startX = x - windowSize / 2
                val startY = y - windowSize / 2
                val endX = x + windowSize / 2
                val endY = y + windowSize / 2

                val windowPixels = mutableListOf<Int>()

                for (i in startX..endX) {
                    for (j in startY..endY) {
                        if (i in 0 until width && j in 0 until height) {
                            windowPixels.add(pixels[j * width + i])
                        }
                    }
                }

                windowPixels.sort()

                val medianPixel = windowPixels[windowPixels.size / 2]

                pixels[y * width + x] = medianPixel
            }
        }
        val medianBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        medianBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        imageView.setImageBitmap(medianBitmap)
    }

    private fun gaussFilter(imageBitmap: Bitmap, radius:Int) {
        val width = imageBitmap.width
        val height = imageBitmap.height
        val pixels = IntArray(width * height)

        val sigma = radius.toFloat() / 2
        val windowSize = radius
        val window = FloatArray(2 * windowSize)

        window[windowSize] = 1.0f
        for (i in windowSize + 1 until 2 * windowSize) {
            window[i] = exp(-i * i / (2 * sigma * sigma))
            window[i - windowSize] = window[i]
        }

        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (y in 0 until height){
            for (x in 0 until width){
                var sum = 0.0f
                var sumRed = 0.0f
                var sumGreen = 0.0f
                var sumBlue = 0.0f

                for (i in -windowSize..windowSize) {
                    for (j in -windowSize..windowSize) {
                        val currentX = x + i
                        val currentY = y + i
                        if (currentX in 0 until width && currentY in 0 until height) {
                            val color = pixels[currentY * width + currentX]
                            sumRed += Color.red(color) * window[abs(j)]
                            sumGreen += Color.green(color) * window[abs(j)]
                            sumBlue += Color.blue(color) * window[abs(j)]
                            sum += window[abs(j)]
                        }
                    }
                }

                pixels[y * width + x] = Color.argb(255, (sumRed / sum).toInt(), (sumGreen / sum).toInt(), (sumBlue / sum).toInt())
            }
        }

        val gaussBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        gaussBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        imageView.setImageBitmap(gaussBitmap)
    }
}
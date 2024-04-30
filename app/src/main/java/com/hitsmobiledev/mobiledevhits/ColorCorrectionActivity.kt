package com.hitsmobiledev.mobiledevhits

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge


class ColorCorrectionActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_color_correction)

        imageView = findViewById(R.id.ColorFilterPhoto)
        imageUri = intent.getParcelableExtra("currentPhoto")!!
        imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageView.setImageBitmap(imageBitmap)

        var currentBitmap : Bitmap = imageBitmap

        // обработчик кнопок
        val saveChangesButton: ImageButton = findViewById(R.id.button_save_color_changes)
        saveChangesButton.setOnClickListener{
            saveChanges(currentBitmap)
        }

        val returnToFiltersButton: ImageButton = findViewById(R.id.button_back_to_filters)
        returnToFiltersButton.setOnClickListener{
            returnToFilters()
        }

        val negativeFilterButton: ImageButton = findViewById(R.id.button_first_filter)
        negativeFilterButton.setOnClickListener{
            currentBitmap = negativeFilter(imageBitmap)
        }

        val defaultFilterButton: ImageButton = findViewById(R.id.button_standart_filter)
        defaultFilterButton.setOnClickListener{
            defaultFilter(imageBitmap)
        }

        val mozaikFilterButton: ImageButton = findViewById(R.id.button_second_filter)
        mozaikFilterButton.setOnClickListener{
            mozaikFilter(imageBitmap)
        }

        val contrastFilterButton: ImageButton = findViewById(R.id.button_third_filter)
        contrastFilterButton.setOnClickListener{
            contrastFilter(imageBitmap, 100)
        }

        val medianFilterButton: ImageButton = findViewById(R.id.button_fourth_filter)
        medianFilterButton.setOnClickListener{
            medianFilter(imageBitmap)
        }
    }

    private fun returnToFilters(){
        val intent = Intent(this@ColorCorrectionActivity, ChooseFilterActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        this@ColorCorrectionActivity.startActivity(intent)
    }
    private fun saveChanges(currentBitmap: Bitmap) {
        returnToFilters()
    }


    private fun defaultFilter(imageBitmap: Bitmap){
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

    private fun medianFilter(imageBitmap: Bitmap){
        val windowSize = 9

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

    private fun trunc(num: Int): Int {
        return if (num > 255) {
            255
        } else if (num < 0) {
            0
        } else {
            num
        }
    }

    private fun contrastFilter(imageBitmap: Bitmap, contrast: Int){
        val factor = (259 * (contrast + 255)) / (255 * (259 - contrast))

        val width = imageBitmap.width
        val height = imageBitmap.height

        val pixels = IntArray(width * height)

        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (x in 0 until width){
            for (y in 0 until height){
                var red = (pixels[y*width+x] shr 16) and 0xFF
                var green = (pixels[y*width+x] shr 8) and 0xFF
                var blue = pixels[y*width+x] and 0xFF

                red = trunc(factor * (red - 128) + 128)
                green = trunc(factor * (green - 128) + 128)
                blue = trunc(factor * (blue - 128) + 128)

                pixels[y*width+x]= Color.argb(255, red, green, blue)
            }
        }

        val contrastBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        contrastBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        imageView.setImageBitmap(contrastBitmap)
    }

    private fun mozaikFilter(imageBitmap: Bitmap){
        val blockSize = 20

        val width = imageBitmap.width
        val height = imageBitmap.height

        val pixels = IntArray(width * height)

        imageBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        for (x in 0 until width step blockSize) {
            for (y in 0 until height step blockSize) {
                val blockColor = pixels[y * width + x]

                for (i in x until x + blockSize) {
                    for (j in y until y + blockSize) {

                        if (i < width && j < height) {
                            pixels[j * width + i] = blockColor
                        }
                    }
                }
            }
        }

        val mozaikBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        mozaikBitmap.setPixels(pixels, 0, width, 0, 0, width, height)

        imageView.setImageBitmap(mozaikBitmap)
    }
}
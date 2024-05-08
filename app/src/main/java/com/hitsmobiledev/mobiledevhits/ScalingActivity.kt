package com.hitsmobiledev.mobiledevhits

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge

class ScalingActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri

    private var scalingValue = 1f

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scaling)

        imageView = findViewById(R.id.currentPhoto)
        imageUri = intent.getParcelableExtra("currentPhoto")!!
        imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageView.setImageBitmap(imageBitmap)

        var currentBitmap : Bitmap = imageBitmap
        var seekBar = findViewById<SeekBar>(R.id.scalingScale)
        seekBar.max = 4000
        seekBar.min = 500
        seekBar.progress = 1000

        val scalingValueView = findViewById<TextView>(R.id.scalingValue)
        scalingValueView.text = "1"
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                scalingValue = progress.toFloat() / 1000
                scalingValueView.text = "$scalingValue"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        val scalingButton: Button = findViewById<Button>(R.id.scaling)
        scalingButton.setOnClickListener{
            val width = imageBitmap.width
            val height = imageBitmap.height
            val newWidth = (width * scalingValue).toInt()
            val newHeight = (height * scalingValue).toInt()
            val prevPixels = IntArray(width * height)
            var newPixels = IntArray(newWidth * newHeight)
            imageBitmap.getPixels(prevPixels, 0, width, 0, 0, width, height)

            if (scalingValue < 1){
                newPixels = trilinearFiltering(prevPixels, width, height, newWidth, newHeight)
            } else {
                newPixels = bilinearFiltering(prevPixels, newWidth, newHeight)
            }

            val newBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
            newBitmap.setPixels(newPixels, 0, newWidth, 0, 0, newWidth, newHeight)
            imageBitmap = newBitmap
            imageView.setImageBitmap(newBitmap)
            Log.d("myApp", "end")
        }
    }

    private fun bilinearFiltering(prevPixels: IntArray, newWidth: Int, newHeight: Int): IntArray {
        var newPixels = IntArray(newWidth * newHeight)
        val width = imageBitmap.width
        val height = imageBitmap.height
        val coeffWidth = (newWidth - 1).toFloat() / (width - 1).toFloat()
        val coeffHeight = (newHeight - 1).toFloat() / (height - 1).toFloat()
        for (x in 0 until newWidth){
            for (y in 0 until newHeight){
                var coordX : Float = x.toFloat() / coeffWidth
                var coordY : Float = y.toFloat() / coeffHeight

                var tempX = coordX.toInt().coerceIn(0, width - 2)
                var tempY = coordY.toInt().coerceIn(0, height - 2)

                coordX -= tempX
                coordY -= tempY

                val firstCoeff = (1 - coordX) * (1 - coordY)
                val secondCoeff = coordX * (1 - coordY)
                val thirdCoeff = coordX * coordY
                val fourthCoeff = (1 - coordX) * coordY

                val firstPixel = prevPixels[tempY * width + tempX]
                val secondPixel = prevPixels[tempY * width + tempX + 1]
                val thirdPixel = prevPixels[(tempY + 1) * width + tempX + 1]
                val fourthPixel = prevPixels[(tempY + 1) * width + tempX]

                var red = firstCoeff * Color.red(firstPixel) + secondCoeff * Color.red(secondPixel)
                red += thirdCoeff * Color.red(thirdPixel) + fourthCoeff * Color.red(fourthPixel)
                var green = firstCoeff * Color.green(firstPixel) + secondCoeff * Color.green(secondPixel)
                green += thirdCoeff * Color.green(thirdPixel) + fourthCoeff * Color.green(fourthPixel)
                var blue = firstCoeff * Color.blue(firstPixel) + secondCoeff * Color.blue(secondPixel)
                blue += thirdCoeff * Color.blue(thirdPixel) + fourthCoeff * Color.blue(fourthPixel)

                newPixels[y * newWidth + x] = Color.rgb(red.toInt(), green.toInt(), blue.toInt())
            }
        }
        return newPixels
    }

    private fun trilinearFiltering(firstLevelPixels: IntArray, width: Int, height: Int, newWidth: Int, newHeight: Int) : IntArray {
        var newPixels = IntArray(newWidth * newHeight)
        val secondWidth = (newWidth.toFloat() * scalingValue).toInt()
        val secondHeight = (newHeight.toFloat() * scalingValue).toInt()
        var secondLevelPixels = bilinearFiltering(firstLevelPixels, secondWidth, secondHeight)

        val firstCoeffWidth = (newWidth - 1).toFloat() / (width - 1).toFloat()
        val firstCoeffHeight = (newHeight - 1).toFloat() / (height - 1).toFloat()
        val secondCoeffWidth = (newWidth - 1).toFloat() / (secondWidth - 1).toFloat()
        val secondCoeffHeight = (newHeight - 1).toFloat() / (secondHeight - 1).toFloat()

        for (x in 0 until newWidth) {
            for (y in 0 until newHeight) {
                val firstX: Float = x.toFloat() / firstCoeffWidth
                val firstY: Float = y.toFloat() / firstCoeffHeight

                val secondX = x.toFloat() / secondCoeffWidth
                val secondY = y.toFloat() / secondCoeffHeight
                val firstPixel = getInterpolatedColor(firstLevelPixels, firstX, firstY, width, height)
                val secondPixel = getInterpolatedColor(secondLevelPixels, secondX, secondY, secondWidth, secondHeight)

                val weight = (firstX % 1) * (firstY % 1)

                val red = (Color.red(firstPixel) * (1 - weight) + Color.red(secondPixel) * weight).toInt()
                val green = (Color.green(firstPixel) * (1 - weight) + Color.green(secondPixel) * weight).toInt()
                val blue = (Color.blue(firstPixel) * (1 - weight) + Color.blue(secondPixel) * weight).toInt()

                newPixels[y * newWidth + x] = Color.rgb(red, green, blue)
            }
        }

        return newPixels
    }

    private fun getInterpolatedColor(pixels: IntArray, coordX : Float, coordY : Float, width: Int, height: Int): Int {
        var tempX = coordX.toInt().coerceIn(0, width - 2)
        var tempY = coordY.toInt().coerceIn(0, height - 2)

        var x = coordX - tempX
        var y = coordY - tempY

        val firstCoeff = (1 - x) * (1 - y)
        val secondCoeff = x * (1 - y)
        val thirdCoeff = x * y
        val fourthCoeff = (1 - x) * y

        val firstPixel = pixels[tempY * width + tempX]
        val secondPixel = pixels[tempY * width + tempX + 1]
        val thirdPixel = pixels[(tempY * width) + tempX + 1]
        val fourthPixel = pixels[(tempY + 1) * width + tempX]

        var red = firstCoeff * Color.red(firstPixel) + secondCoeff * Color.red(secondPixel)
        red += thirdCoeff * Color.red(thirdPixel) + fourthCoeff * Color.red(fourthPixel)
        var green = firstCoeff * Color.green(firstPixel) + secondCoeff * Color.green(secondPixel)
        green += thirdCoeff * Color.green(thirdPixel) + fourthCoeff * Color.green(fourthPixel)
        var blue = firstCoeff * Color.blue(firstPixel) + secondCoeff * Color.blue(secondPixel)
        blue += thirdCoeff * Color.blue(thirdPixel) + fourthCoeff * Color.blue(fourthPixel)

        return Color.rgb(red.toInt(), green.toInt(), blue.toInt())
    }
}



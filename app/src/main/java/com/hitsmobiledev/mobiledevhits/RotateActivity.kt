package com.hitsmobiledev.mobiledevhits

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class RotateActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var slider: Slider
    private lateinit var button: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rotate)

        imageView = findViewById(R.id.currentPhoto)
        val imageUri = intent.getParcelableExtra<Uri>("currentPhoto")
        imageView.setImageURI(imageUri)

        slider = findViewById(R.id.rotate_slider)
        button = findViewById(R.id.rotate_button)

        button.setOnClickListener {
            imageView.setImageBitmap(rotate(slider.value.toInt(), MediaStore.Images.Media.getBitmap(this@RotateActivity.contentResolver, imageUri)))
        }
    }

    private fun getRotatedCord(pointCords: Pair<Int,Int>, centerPointCords: Pair<Int,Int>, angle: Double) : Pair<Int, Int> {
        val (pointX, pointY) = pointCords
        val (centerX, centerY) = centerPointCords

        val rotatedX = (pointX - centerX) * cos(angle) - (pointY - centerY) * sin(angle) + centerX
        val rotatedY = (pointX - centerX) * sin(angle) + (pointY - centerY) * cos(angle) + centerY

        return Pair(rotatedX.toInt(), rotatedY.toInt())
    }

    private fun rotate(angle: Int, image: Bitmap): Bitmap {
        val angleRadians = Math.toRadians(angle.toDouble())
        val inputWidth = image.width
        val inputHeight = image.height
        val imageCenterX = inputWidth / 2
        val imageCenterY = inputHeight / 2

        val imagePixels = IntArray(inputWidth * inputHeight)
        image.getPixels(imagePixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

        val firstPoint = getRotatedCord(Pair(0, 0), Pair(imageCenterX, imageCenterY), angleRadians)
        val secondPoint = getRotatedCord(Pair(inputWidth, 0), Pair(imageCenterX, imageCenterY), angleRadians)
        val thirdPoint = getRotatedCord(Pair(0, inputHeight), Pair(imageCenterX, imageCenterY), angleRadians)
        val fourthPoint = getRotatedCord(Pair(inputWidth, inputHeight), Pair(imageCenterX, imageCenterY), angleRadians)

        val max_X = max(max(firstPoint.first, secondPoint.first), max(thirdPoint.first, fourthPoint.first))
        val max_Y = max(max(firstPoint.second, secondPoint.second), max(thirdPoint.second, fourthPoint.second))
        val min_X = min(min(firstPoint.first, secondPoint.first), min(thirdPoint.first, fourthPoint.first))
        val min_Y = min(min(firstPoint.second, secondPoint.second), min(thirdPoint.second, fourthPoint.second))

        val outputWidth = max_X - min_X
        val outputHeight = max_Y - min_Y

        val rotatedBitmap = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
        val rotatedPixels = IntArray(outputWidth * outputHeight)

        for (y in 0 until outputHeight) {
            for (x in 0 until outputWidth) {
                val rotatedPoint = getRotatedCord(Pair(x + min_X, y + min_Y), Pair(imageCenterX, imageCenterY), angleRadians)
                if (rotatedPoint.first in 0 until inputWidth && rotatedPoint.second in 0 until inputHeight) {
                    val color = imagePixels[rotatedPoint.second * inputWidth + rotatedPoint.first]
                    rotatedPixels[y * outputWidth + x] = color
                }
            }
        }

        rotatedBitmap.setPixels(rotatedPixels, 0, outputWidth, 0, 0, outputWidth, outputHeight)
        return rotatedBitmap
    }
}
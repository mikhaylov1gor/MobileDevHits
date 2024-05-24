package com.hitsmobiledev.mobiledevhits

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

class RotateActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var button: MaterialButton
    private var angleRotation: Int = 180

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_rotate)

        imageView = findViewById(R.id.currentPhoto)
        val imageUri = intent.getParcelableExtra<Uri>("currentPhoto")
        imageView.setImageURI(imageUri)

        val angleSeekBar = findViewById<SeekBar>(R.id.angle_seek_bar)
        angleSeekBar.max = 360
        angleSeekBar.min = 0
        angleSeekBar.progress = 180
        val retouchingValueView = findViewById<TextView>(R.id.angle_value)
        retouchingValueView.text = "$angleRotation"
        angleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                angleRotation = progress
                retouchingValueView.text = "$angleRotation"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        button = findViewById(R.id.rotate_button)

        val coroutineScope = CoroutineScope(Dispatchers.Main)
        var finalBitmap: Bitmap? = null

        button.setOnClickListener {
            coroutineScope.launch {
                val rotatedImage = rotate(
                    angleRotation,
                    MediaStore.Images.Media.getBitmap(this@RotateActivity.contentResolver, imageUri)
                )
                finalBitmap = rotatedImage
                imageView.setImageBitmap(rotatedImage)
            }
        }

        val saveChangesButton: ImageButton = findViewById(R.id.button_save_changes)
        saveChangesButton.setOnClickListener {
            val newUri = finalBitmap?.let { it1 -> saveBitmapToCache(this, it1) }
            val intent = Intent(this@RotateActivity, ChooseFilterActivity::class.java)
            intent.putExtra("currentPhoto", newUri)
            if (imageUri != null) {
                deleteFileFromCache(imageUri)
            }
            this@RotateActivity.startActivity(intent)
            finish()
        }

        val cancelChangesButton: ImageButton = findViewById(R.id.button_cancel)
        cancelChangesButton.setOnClickListener {
            val intent = Intent(this@RotateActivity, ChooseFilterActivity::class.java)
            intent.putExtra("currentPhoto", imageUri)
            this@RotateActivity.startActivity(intent)
            finish()
        }
    }

    private fun getRotatedCord(
        pointCords: Pair<Int, Int>,
        centerPointCords: Pair<Int, Int>,
        angle: Double
    ): Pair<Int, Int> {
        val (pointX, pointY) = pointCords
        val (centerX, centerY) = centerPointCords

        val rotatedX = (pointX - centerX) * cos(angle) - (pointY - centerY) * sin(angle) + centerX
        val rotatedY = (pointX - centerX) * sin(angle) + (pointY - centerY) * cos(angle) + centerY

        return Pair(rotatedX.toInt(), rotatedY.toInt())
    }

    private suspend fun rotate(angle: Int, image: Bitmap): Bitmap =
        withContext(Dispatchers.Default) {
            val angleRadians = Math.toRadians(angle.toDouble())
            val inputWidth = image.width
            val inputHeight = image.height
            val imageCenterX = inputWidth / 2
            val imageCenterY = inputHeight / 2

            val imagePixels = IntArray(inputWidth * inputHeight)
            image.getPixels(imagePixels, 0, inputWidth, 0, 0, inputWidth, inputHeight)

            val firstPoint =
                getRotatedCord(Pair(0, 0), Pair(imageCenterX, imageCenterY), angleRadians)
            val secondPoint =
                getRotatedCord(Pair(inputWidth, 0), Pair(imageCenterX, imageCenterY), angleRadians)
            val thirdPoint =
                getRotatedCord(Pair(0, inputHeight), Pair(imageCenterX, imageCenterY), angleRadians)
            val fourthPoint = getRotatedCord(
                Pair(inputWidth, inputHeight),
                Pair(imageCenterX, imageCenterY),
                angleRadians
            )

            val max_X = max(
                max(firstPoint.first, secondPoint.first),
                max(thirdPoint.first, fourthPoint.first)
            )
            val max_Y = max(
                max(firstPoint.second, secondPoint.second),
                max(thirdPoint.second, fourthPoint.second)
            )
            val min_X = min(
                min(firstPoint.first, secondPoint.first),
                min(thirdPoint.first, fourthPoint.first)
            )
            val min_Y = min(
                min(firstPoint.second, secondPoint.second),
                min(thirdPoint.second, fourthPoint.second)
            )

            val outputWidth = max_X - min_X
            val outputHeight = max_Y - min_Y

            val rotatedBitmap =
                Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
            val rotatedPixels = IntArray(outputWidth * outputHeight)

            val jobs = mutableListOf<Job>()

            for (y in 0 until outputHeight) {
                val job = launch {
                    for (x in 0 until outputWidth) {
                        val rotatedPoint = getRotatedCord(
                            Pair(x + min_X, y + min_Y),
                            Pair(imageCenterX, imageCenterY),
                            angleRadians
                        )
                        if (rotatedPoint.first in 0 until inputWidth && rotatedPoint.second in 0 until inputHeight) {
                            val color =
                                imagePixels[rotatedPoint.second * inputWidth + rotatedPoint.first]
                            rotatedPixels[y * outputWidth + x] = color
                        }
                    }
                }
                jobs.add(job)
            }

            jobs.forEach { it.join() }

            rotatedBitmap.apply {
                setPixels(rotatedPixels, 0, outputWidth, 0, 0, outputWidth, outputHeight)
            }
        }
}
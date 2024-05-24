package com.hitsmobiledev.mobiledevhits

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import kotlinx.coroutines.*
import kotlin.math.exp

class ScalingActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri

    private var scaleValue = 1f

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_scaling)

        imageView = findViewById(R.id.currentPhoto)
        imageUri = intent.getParcelableExtra("currentPhoto")!!
        imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageView.setImageBitmap(imageBitmap)

        val coroutineScopeMain = CoroutineScope(Dispatchers.Main)

        val scaleValueSeekBar = findViewById<SeekBar>(R.id.scaleValueSeekBar)
        scaleValueSeekBar.max = 4000
        scaleValueSeekBar.min = 500
        scaleValueSeekBar.progress = 1000
        val scaleValueView = findViewById<TextView>(R.id.scaleValue)
        scaleValueView.text = "$scaleValue"
        scaleValueSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                scaleValue = progress.toFloat() / 1000
                scaleValueView.text = "$scaleValue"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val scalingButton: Button = findViewById<Button>(R.id.scaling)
        scalingButton.setOnClickListener {
            val width = imageBitmap.width
            val height = imageBitmap.height
            val newWidth = (width * scaleValue).toInt()
            val newHeight = (height * scaleValue).toInt()

            if (newWidth * newHeight > 16000000) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.size_warning)
                builder.setMessage(R.string.large_size_warning)
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            } else if (newWidth * newHeight < 10) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.size_warning)
                builder.setMessage(R.string.small_size_warning)
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            } else {
                coroutineScopeMain.launch {
                    val prevPixels = IntArray(width * height)
                    var newPixels: IntArray
                    imageBitmap.getPixels(prevPixels, 0, width, 0, 0, width, height)
                    if (scaleValue < 1) {
                        newPixels =
                            trilinearFiltering(prevPixels, width, height, newWidth, newHeight)
                    } else {
                        newPixels = bilinearFiltering(prevPixels, newWidth, newHeight)
                    }

                    val newBitmap =
                        Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
                    newBitmap.setPixels(newPixels, 0, newWidth, 0, 0, newWidth, newHeight)
                    imageBitmap = newBitmap
                    imageView.setImageBitmap(newBitmap)
                }
            }
        }

        val saveChangesButton: ImageButton = findViewById(R.id.button_save_changes)
        saveChangesButton.setOnClickListener {
            val newUri = saveBitmapToCache(this, imageBitmap)
            val intent = Intent(this@ScalingActivity, ChooseFilterActivity::class.java)
            intent.putExtra("currentPhoto", newUri)
            if (imageUri != null) {
                deleteFileFromCache(imageUri)
            }
            this@ScalingActivity.startActivity(intent)
            finish()
        }

        val cancelChangesButton: ImageButton = findViewById(R.id.button_cancel)
        cancelChangesButton.setOnClickListener {
            val intent = Intent(this@ScalingActivity, ChooseFilterActivity::class.java)
            intent.putExtra("currentPhoto", imageUri)
            this@ScalingActivity.startActivity(intent)
            finish()
        }
    }


    private suspend fun bilinearFiltering(
        prevPixels: IntArray,
        newWidth: Int,
        newHeight: Int
    ): IntArray = withContext(Dispatchers.Default) {
        var newPixels = IntArray(newWidth * newHeight)
        val width = imageBitmap.width
        val height = imageBitmap.height
        val coeffWidth = (newWidth - 1).toFloat() / (width - 1).toFloat()
        val coeffHeight = (newHeight - 1).toFloat() / (height - 1).toFloat()
        val jobs = mutableListOf<Job>()
        for (x in 0 until newWidth) {
            val job = launch {
                for (y in 0 until newHeight) {
                    var coordX: Float = x.toFloat() / coeffWidth
                    var coordY: Float = y.toFloat() / coeffHeight

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

                    var red =
                        firstCoeff * Color.red(firstPixel) + secondCoeff * Color.red(secondPixel)
                    red += thirdCoeff * Color.red(thirdPixel) + fourthCoeff * Color.red(fourthPixel)
                    var green =
                        firstCoeff * Color.green(firstPixel) + secondCoeff * Color.green(secondPixel)
                    green += thirdCoeff * Color.green(thirdPixel) + fourthCoeff * Color.green(
                        fourthPixel
                    )
                    var blue =
                        firstCoeff * Color.blue(firstPixel) + secondCoeff * Color.blue(secondPixel)
                    blue += thirdCoeff * Color.blue(thirdPixel) + fourthCoeff * Color.blue(
                        fourthPixel
                    )

                    newPixels[y * newWidth + x] =
                        Color.rgb(red.toInt(), green.toInt(), blue.toInt())
                }
            }
            jobs.add(job)
        }
        jobs.forEach { it.join() }
        return@withContext newPixels
    }

    private suspend fun trilinearFiltering(
        prevPixels: IntArray,
        width: Int,
        height: Int,
        newWidth: Int,
        newHeight: Int
    ): IntArray = withContext(Dispatchers.Main) {
        var newPixels = IntArray(newWidth * newHeight)
        var firstWidth = width
        var firstHeight = height;
        var firstLevelPixels = prevPixels;
        if (width * height < 960 * 720) {
            firstWidth = (width.toFloat() / scaleValue).toInt()
            firstHeight = (height.toFloat() / scaleValue).toInt()
            runBlocking {
                firstLevelPixels = blur(
                    bilinearFiltering(prevPixels, firstWidth, firstHeight),
                    firstWidth,
                    firstHeight
                )
            }
        }

        val secondWidth = (width.toFloat() * scaleValue * scaleValue * 3 / 2).toInt()
        val secondHeight = (height.toFloat() * scaleValue * scaleValue * 3 / 2).toInt()
        var secondLevelPixels: IntArray
        runBlocking {
            secondLevelPixels = bilinearFiltering(prevPixels, secondWidth, secondHeight)
        }

        val firstCoeffWidth = (newWidth - 1).toFloat() / (firstWidth - 1).toFloat()
        val firstCoeffHeight = (newHeight - 1).toFloat() / (firstHeight - 1).toFloat()
        val secondCoeffWidth = (newWidth - 1).toFloat() / (secondWidth - 1).toFloat()
        val secondCoeffHeight = (newHeight - 1).toFloat() / (secondHeight - 1).toFloat()

        val jobs = mutableListOf<Job>()
        for (x in 0 until newWidth) {
            val job = launch {
                for (y in 0 until newHeight) {
                    val firstX: Float = x.toFloat() / firstCoeffWidth
                    val firstY: Float = y.toFloat() / firstCoeffHeight

                    val secondX = x.toFloat() / secondCoeffWidth
                    val secondY = y.toFloat() / secondCoeffHeight

                    val firstPixel = getInterpolatedColor(
                        firstLevelPixels,
                        firstX,
                        firstY,
                        firstWidth,
                        firstHeight
                    )
                    val secondPixel = getInterpolatedColor(
                        secondLevelPixels,
                        secondX,
                        secondY,
                        secondWidth,
                        secondHeight
                    )

                    val weight = (firstX % 1) * (firstY % 1)

                    val red =
                        (Color.red(firstPixel) * (1 - weight) + Color.red(secondPixel) * weight).toInt()
                    val green =
                        (Color.green(firstPixel) * (1 - weight) + Color.green(secondPixel) * weight).toInt()
                    val blue =
                        (Color.blue(firstPixel) * (1 - weight) + Color.blue(secondPixel) * weight).toInt()

                    newPixels[y * newWidth + x] = Color.rgb(red, green, blue)
                }
            }
            jobs.add(job)
        }
        jobs.forEach { it.join() }
        return@withContext newPixels
    }

    private fun getInterpolatedColor(
        pixels: IntArray,
        coordX: Float,
        coordY: Float,
        width: Int,
        height: Int
    ): Int {
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
        val thirdPixel = pixels[(tempY + 1) * width + tempX + 1]
        val fourthPixel = pixels[(tempY + 1) * width + tempX]

        var red = firstCoeff * Color.red(firstPixel) + secondCoeff * Color.red(secondPixel)
        red += thirdCoeff * Color.red(thirdPixel) + fourthCoeff * Color.red(fourthPixel)
        var green = firstCoeff * Color.green(firstPixel) + secondCoeff * Color.green(secondPixel)
        green += thirdCoeff * Color.green(thirdPixel) + fourthCoeff * Color.green(fourthPixel)
        var blue = firstCoeff * Color.blue(firstPixel) + secondCoeff * Color.blue(secondPixel)
        blue += thirdCoeff * Color.blue(thirdPixel) + fourthCoeff * Color.blue(fourthPixel)

        return Color.rgb(red.toInt(), green.toInt(), blue.toInt())
    }

    private fun calculateGaussianWeight(x: Int, sigma: Double): Double {
        return (1.0 / (2.0 * Math.PI * sigma * sigma)) * exp(-(x * x) / (2.0 * sigma * sigma));
    }

    private suspend fun blur(pixels: IntArray, width: Int, height: Int): IntArray =
        withContext(Dispatchers.Default) {
            var newPixels = IntArray(width * height);
            var radius = 4;
            var sigma = 1.0 + 960.0 * 720.0 / width.toFloat() / height.toFloat();
            val jobs = mutableListOf<Job>()
            for (x in 0 until width) {
                val job = launch {
                    for (y in 0 until height) {
                        var red = 0.0;
                        var green = 0.0;
                        var blue = 0.0;
                        var weightSum = 0.0;
                        var coord = y * width + x;

                        for (i in -radius..radius) {
                            for (j in -radius..radius) {
                                val newX = x + i;
                                val newY = y + j;

                                if (newX in 0 until width && newY in 0 until height) {
                                    var weight =
                                        calculateGaussianWeight(i, sigma) * calculateGaussianWeight(
                                            j,
                                            sigma
                                        )
                                    red += Color.red(pixels[coord]).toDouble() * weight;
                                    green += Color.green(pixels[coord]).toDouble() * weight;
                                    blue += Color.blue(pixels[coord]).toDouble() * weight;
                                    weightSum += weight;
                                }
                            }
                        }
                        red /= weightSum;
                        green /= weightSum;
                        blue /= weightSum;

                        newPixels[coord] = Color.rgb(red.toInt(), green.toInt(), blue.toInt())
                    }
                }
                jobs.add(job)
            }
            jobs.forEach { it.join() }
            return@withContext newPixels;
        }
}

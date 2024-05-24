package com.hitsmobiledev.mobiledevhits

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import java.util.LinkedList
import java.util.Queue
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class Point(val x: Int, val y: Int)

class RetouchActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri
    private val queue: Queue<Point> = LinkedList()
    private var topBorder = 0
    private var bottomBorder = 0
    private var leftBorder = 0
    private var rightBorder = 0
    private var scaling = 0f
    private var brushSize = 25f
    private var coefficient = 0.5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_retouch)

        imageView = findViewById(R.id.currentPhoto)
        imageUri = intent.getParcelableExtra("currentPhoto")!!
        imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageView.setImageBitmap(imageBitmap)

        val brushScaleSeekBar = findViewById<SeekBar>(R.id.brush_seek_bar)
        brushScaleSeekBar.max = 5000
        brushScaleSeekBar.min = 100
        brushScaleSeekBar.progress = 2500
        val brushSizeValueView = findViewById<TextView>(R.id.brush_size_value)
        brushSizeValueView.text = "$brushSize"
        brushScaleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brushSize = progress.toFloat() / 100
                brushSizeValueView.text = "$brushSize"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val retouchingSeekBar = findViewById<SeekBar>(R.id.retouch_seek_bar)
        retouchingSeekBar.max = 100
        retouchingSeekBar.min = 1
        retouchingSeekBar.progress = 50
        val retouchingValueView = findViewById<TextView>(R.id.retouch_value)
        retouchingValueView.text = "$coefficient"
        retouchingSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                coefficient = progress.toFloat() / 100
                retouchingValueView.text = "$coefficient"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        val saveChangesButton: ImageButton = findViewById(R.id.button_save_changes)
        saveChangesButton.setOnClickListener {
            val newUri = saveBitmapToCache(this, imageBitmap)
            val intent = Intent(this@RetouchActivity, ChooseFilterActivity::class.java)
            intent.putExtra("currentPhoto", newUri)
            if (imageUri != null) {
                deleteFileFromCache(imageUri)
            }
            this@RetouchActivity.startActivity(intent)
            finish()
        }

        val cancelChangesButton: ImageButton = findViewById(R.id.button_cancel)
        cancelChangesButton.setOnClickListener {
            val intent = Intent(this@RetouchActivity, ChooseFilterActivity::class.java)
            intent.putExtra("currentPhoto", imageUri)
            this@RetouchActivity.startActivity(intent)
            finish()
        }
    }

    private fun calculateBorders() {
        val drawable = imageView.drawable
        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight

        if (intrinsicWidth < intrinsicHeight) {
            val top = imageView.top
            val bottom = imageView.bottom
            scaling = (bottom - top).toFloat() / intrinsicHeight.toFloat()
            val space = (imageView.right - imageView.left - intrinsicWidth * scaling) / 2
            topBorder = imageView.top
            bottomBorder = imageView.bottom
            leftBorder = (imageView.left + space).toInt()
            rightBorder = (imageView.right - space).toInt()
        } else {
            val left = imageView.left
            val right = imageView.right
            scaling = (right - left).toFloat() / intrinsicWidth.toFloat()
            val space = (imageView.bottom - imageView.top - intrinsicHeight * scaling) / 2
            topBorder = (imageView.top + space).toInt()
            bottomBorder = (imageView.bottom - space).toInt()
            leftBorder = imageView.left
            rightBorder = imageView.right
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN) {
            if (scaling == 0f) {
                calculateBorders()
            }

            var x = event.x
            var y = event.y

            if (topBorder <= y && y <= bottomBorder && leftBorder <= x && x <= rightBorder) {
                x -= leftBorder
                y -= topBorder
                val newX = (x / scaling).toInt()
                val newY = (y / scaling).toInt()

                queue.add(Point(newX, newY))
                val point = queue.poll()
                if (point != null) {
                    retouching(point.x, point.y)
                }
            }
        }

        if (event.action == MotionEvent.ACTION_UP) {
            while (!queue.isEmpty()) {
                val point = queue.poll()
                if (point != null) {
                    retouching(point.x, point.y)
                }
            }
        }

        return true
    }

    private fun retouching(x: Int, y: Int) {
        val brushSizeInt = brushSize.toInt()
        val width = 1 + min(x, brushSizeInt) + min(imageBitmap.width - x - 1, brushSizeInt)
        val height = 1 + min(y, brushSizeInt) + min(imageBitmap.height - y - 1, brushSizeInt)

        val pixels = IntArray(width * height)
        val startX = max(0, x - brushSizeInt)
        val startY = max(0, y - brushSizeInt)
        val endX = startX + width
        val endY = startY + height

        val editedBitmap = imageBitmap.copy(imageBitmap.config, true)
        imageBitmap.getPixels(pixels, 0, width, startX, startY, width, height)

        var sumRed = 0f
        var sumGreen = 0f
        var sumBlue = 0f

        var count = 0f
        for (pixelY in startY until endY) {
            for (pixelX in startX until endX) {
                val diffX = (pixelX - x).toDouble()
                val diffY = (pixelY - y).toDouble()
                val dist = sqrt(diffX * diffX + diffY * diffY)
                if (brushSizeInt > dist) {
                    val i = (pixelY - startY) * width + pixelX - startX
                    val pixel = pixels[i]
                    sumRed += Color.red(pixel)
                    sumGreen += Color.green(pixel)
                    sumBlue += Color.blue(pixel)
                    count++
                }
            }
        }

        val avgRed = sumRed / count
        val avgGreen = sumGreen / count
        val avgBlue = sumBlue / count

        for (pixelY in startY until endY) {
            for (pixelX in startX until endX) {
                val diffX = (pixelX - x).toDouble()
                val diffY = (pixelY - y).toDouble()
                val dist = sqrt(diffX * diffX + diffY * diffY)
                if (brushSizeInt > dist) {
                    val newCoeff = coefficient * (1f - dist / brushSizeInt.toFloat())
                    val i = (pixelY - startY) * width + pixelX - startX
                    val pixel = pixels[i]
                    val red =
                        ((newCoeff * avgRed) + ((1.0 - newCoeff) * Color.red(pixel).toFloat()))
                    val green =
                        ((newCoeff * avgGreen) + ((1.0 - newCoeff) * Color.green(pixel).toFloat()))
                    val blue =
                        ((newCoeff * avgBlue) + ((1.0 - newCoeff) * Color.blue(pixel).toFloat()))
                    pixels[i] = Color.rgb(red.toInt(), green.toInt(), blue.toInt())
                }
            }
        }

        editedBitmap.setPixels(pixels, 0, width, startX, startY, width, height)
        imageBitmap = editedBitmap
        imageView.setImageBitmap(editedBitmap)
        imageBitmap.setPixels(pixels, 0, width, startX, startY, width, height)
    }
}

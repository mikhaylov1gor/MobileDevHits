package com.hitsmobiledev.mobiledevhits

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MotionEvent
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.pow
import kotlin.math.sqrt

class AffineActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri
    private var startPoints = mutableListOf<Point>()
    private var finishPoints = mutableListOf<Point>()

    private var topBorder = 0
    private var bottomBorder = 0
    private var leftBorder = 0
    private var rightBorder = 0
    private var scaling = 1f

    private var a = 0f
    private var b = 0f
    private var tx = 0f

    private var c = 0f
    private var d = 0f
    private var ty = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_affine)

        imageView = findViewById(R.id.currentPhoto)
        imageUri = intent.getParcelableExtra("currentPhoto")!!
        imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageView.setImageBitmap(imageBitmap)

        var currentBitmap: Bitmap = imageBitmap
        calculateBorders()

        val returnToFiltersButton: ImageButton = findViewById(R.id.button_back_to_filters)
        returnToFiltersButton.setOnClickListener {
            returnToFilters()
        }

        val setStartPoints: Button = findViewById(R.id.button_start_points)
        setStartPoints.setOnClickListener {
            setStartPoints(currentBitmap)
        }

        val setFinishPoints: Button = findViewById(R.id.button_end_points)
        setFinishPoints.setOnClickListener {
            setEndPoints(currentBitmap)
        }

        val startTransform: Button = findViewById(R.id.button_confirm)
        startTransform.setOnClickListener {
            affineTransform(currentBitmap)
        }
    }

    private fun returnToFilters() {
        val intent = Intent(this@AffineActivity, ChooseFilterActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        startActivity(intent)
    }

    private fun calculateCoeffs(){
        val detA = calculateMatrix(arrayOf(
            floatArrayOf(startPoints[0].x.toFloat(), startPoints[0].y.toFloat(), 1f),
            floatArrayOf(startPoints[1].x.toFloat(), startPoints[1].y.toFloat(), 1f),
            floatArrayOf(startPoints[2].x.toFloat(), startPoints[2].y.toFloat(), 1f)
        ))

        val detA1 = calculateMatrix(arrayOf(
            floatArrayOf(finishPoints[0].x.toFloat(), startPoints[0].y.toFloat(), 1f),
            floatArrayOf(finishPoints[1].x.toFloat(), startPoints[1].y.toFloat(), 1f),
            floatArrayOf(finishPoints[2].x.toFloat(), startPoints[2].y.toFloat(), 1f)
        ))

        val detA2 = calculateMatrix(arrayOf(
            floatArrayOf(startPoints[0].x.toFloat(), finishPoints[0].x.toFloat(), 1f),
            floatArrayOf(startPoints[1].x.toFloat(), finishPoints[1].x.toFloat(), 1f),
            floatArrayOf(startPoints[2].x.toFloat(), finishPoints[2].x.toFloat(), 1f)
        ))

        val detA3 = calculateMatrix(arrayOf(
            floatArrayOf(startPoints[0].x.toFloat(), startPoints[0].y.toFloat(), finishPoints[0].x.toFloat()),
            floatArrayOf(startPoints[1].x.toFloat(), startPoints[1].y.toFloat(), finishPoints[1].x.toFloat()),
            floatArrayOf(startPoints[2].x.toFloat(), startPoints[2].y.toFloat(), finishPoints[2].x.toFloat())
        ))

        val detB1 = calculateMatrix(arrayOf(
            floatArrayOf(finishPoints[0].y.toFloat(), startPoints[0].y.toFloat(), 1f),
            floatArrayOf(finishPoints[1].y.toFloat(), startPoints[1].y.toFloat(), 1f),
            floatArrayOf(finishPoints[2].y.toFloat(), startPoints[2].y.toFloat(), 1f)
        ))

        val detB2 = calculateMatrix(arrayOf(
            floatArrayOf(startPoints[0].x.toFloat(), finishPoints[0].y.toFloat(), 1f),
            floatArrayOf(startPoints[1].x.toFloat(), finishPoints[1].y.toFloat(), 1f),
            floatArrayOf(startPoints[2].x.toFloat(), finishPoints[2].y.toFloat(), 1f)
        ))

        val detB3 = calculateMatrix(arrayOf(
            floatArrayOf(startPoints[0].x.toFloat(), startPoints[0].y.toFloat(), finishPoints[0].y.toFloat()),
            floatArrayOf(startPoints[1].x.toFloat(), startPoints[1].y.toFloat(), finishPoints[1].y.toFloat()),
            floatArrayOf(startPoints[2].x.toFloat(), startPoints[2].y.toFloat(), finishPoints[2].y.toFloat())
        ))

         a = detA1 / detA
         b = detA2 / detA
         tx = detA3 / detA

         c = detB1 / detA
         d = detB2 / detA
         ty = detB3 / detA
    }
    private fun calculateBorders(){
        val drawable = imageView.drawable
        val intrinsicWidth = drawable.intrinsicWidth
        val intrinsicHeight = drawable.intrinsicHeight

        if (intrinsicWidth < intrinsicHeight){
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

    private fun setStartPoints(currentBitmap: Bitmap) {
        edit(startPoints,currentBitmap)
        imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (startPoints.size == 3) {
                    startPoints = mutableListOf()
                }

                var x = event.x
                var y = event.y

                if (topBorder <= y && y <= bottomBorder && leftBorder <= x && x <= rightBorder) {
                    x -= leftBorder
                    y -= topBorder
                    x = (x / scaling)
                    y = (y / scaling)
                }


                startPoints.add(Point(x.toInt(), y.toInt()))
                edit(startPoints, currentBitmap)
                imageView.invalidate()
            }
            true
        }
    }

    private fun setEndPoints(currentBitmap: Bitmap) {
        edit(finishPoints,currentBitmap)
        imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (finishPoints.size == 3) {
                    finishPoints = mutableListOf()
                }
                finishPoints.add(Point(event.x.toInt(), event.y.toInt()))
                edit(finishPoints, currentBitmap)
                imageView.invalidate()
            }
            true
        }
    }

    private fun edit(points: MutableList<Point>, currentBitmap: Bitmap) {
        val editedBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(editedBitmap)

        val paints = listOf(
            Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            },
            Paint().apply {
                color = Color.GREEN
                style = Paint.Style.FILL
            },
            Paint().apply {
                color = Color.BLUE
                style = Paint.Style.FILL
            }
        )

        points.forEachIndexed { index, point ->
            if (index < 3) {
                canvas.drawCircle(point.x.toFloat(), point.y.toFloat(), 10f, paints[index])
            }
        }

        imageView.setImageBitmap(editedBitmap)
    }

    private fun calculateMatrix(matrix: Array<FloatArray>): Float {
        return matrix[0][0] * (matrix[1][1] * matrix[2][2] - matrix[1][2] * matrix[2][1]) -
                matrix[0][1] * (matrix[1][0] * matrix[2][2] - matrix[1][2] * matrix[2][0]) +
                matrix[0][2] * (matrix[1][0] * matrix[2][1] - matrix[1][1] * matrix[2][0])
    }

    private fun calculateNewSize(): Pair<Int, Int> {
        val distancesStart = listOf(
            distance(startPoints[0], startPoints[1]),
            distance(startPoints[1], startPoints[2]),
            distance(startPoints[2], startPoints[0])
        )

        val distancesFinish = listOf(
            distance(finishPoints[0], finishPoints[1]),
            distance(finishPoints[1], finishPoints[2]),
            distance(finishPoints[2], finishPoints[0])
        )

        val scale = (distancesFinish[0] / distancesStart[0] + distancesFinish[1] / distancesStart[1] + distancesFinish[2] / distancesStart[2]) / 3


        val newWidth = (imageBitmap.width * scale).toInt()
        val newHeight = (imageBitmap.height * scale).toInt()

        return Pair(newWidth, newHeight)
    }

    private fun affineTransform(currentBitmap: Bitmap) {
        if (startPoints.size < 3 || finishPoints.size < 3) {
            Toast.makeText(this, "Мало точек", Toast.LENGTH_SHORT).show()
            return
        }
        calculateCoeffs()

        val width = currentBitmap.width
        val height = currentBitmap.height

        val (newWidth, newHeight) = calculateNewSize()
        val isScalingUp = newWidth > currentBitmap.width || newHeight > currentBitmap.height

        val transformedBitmap = Bitmap.createBitmap(width, height, currentBitmap.config)

        for (y in 0 until height) {
            for (x in 0 until width) {
                val newX = a * x + b * y + tx
                val newY = c * x + d * y + ty

                val color = if (newX < 0 || newX >= width || newY < 0 || newY >= height) {
                    Color.TRANSPARENT
                } else {
                    if (isScalingUp) {
                        getBilinearColor(currentBitmap, newX, newY)
                    } else {
                        getTrilinearColor(currentBitmap, newX, newY)
                    }
                }
                transformedBitmap.setPixel(x, y, color)
            }
        }

        imageView.setImageBitmap(transformedBitmap)
    }

    private fun distance(p1: Point, p2: Point): Float {
        return sqrt(((p2.x - p1.x).toDouble()).pow(2.0) + (p2.y - p1.y).toDouble().pow(2.0)).toFloat()
    }


    private fun getBilinearColor(bitmap: Bitmap, x: Float, y: Float): Int {
        val x1 = x.toInt()
        val y1 = y.toInt()
        val x2 = (x1 + 1).coerceIn(0, bitmap.width - 1)
        val y2 = (y1 + 1).coerceIn(0, bitmap.height - 1)

        val firstColor = bitmap.getPixel(x1, y1)
        val secondColor = bitmap.getPixel(x1, y2)
        val thirdColor = bitmap.getPixel(x2, y1)
        val fourthColor = bitmap.getPixel(x2, y2)

        var red = bilinearInterpolate(Color.red(firstColor), Color.red(thirdColor), Color.red(secondColor), Color.red(fourthColor), x - x1, y - y1)
        var green = bilinearInterpolate(Color.green(firstColor), Color.green(thirdColor), Color.green(secondColor), Color.green(fourthColor), x - x1, y - y1)
        var blue = bilinearInterpolate(Color.blue(firstColor), Color.blue(thirdColor), Color.blue(secondColor), Color.blue(fourthColor), x - x1, y - y1)
        var a = bilinearInterpolate(Color.alpha(firstColor), Color.alpha(thirdColor), Color.alpha(secondColor), Color.alpha(fourthColor), x - x1, y - y1)


        return Color.argb(a,red, green, blue)
    }

    private fun bilinearInterpolate(c00: Int, c10: Int, c01: Int, firstColor: Int, tx: Float, ty: Float): Int {
        val a = c00 * (1 - tx) + c10 * tx
        val b = c01 * (1 - tx) + firstColor * tx
        return (a * (1 - ty) + b * ty).toInt()
    }

    private fun getTrilinearColor(bitmap: Bitmap, x: Float, y: Float): Int {
        val lowerBitmap = bitmap
        val upperBitmap = bitmap

        val lowerColor = getBilinearColor(upperBitmap, x, y)
        val upperColor = getBilinearColor(upperBitmap, x, y)

        val z = 0.5f
        val red = linearInterpolate(Color.red(lowerColor), Color.red(upperColor), z)
        val green = linearInterpolate(Color.green(lowerColor), Color.green(upperColor), z)
        val blue = linearInterpolate(Color.blue(lowerColor), Color.blue(upperColor), z)
        val alpha = linearInterpolate(Color.alpha(lowerColor), Color.alpha(upperColor), z)

        return Color.argb(alpha, red, green, blue)
    }


    private fun linearInterpolate(c0: Int, c1: Int, t: Float): Int {
        return (c0 * (1 - t) + c1 * t).toInt()
    }

    private fun getPixelSafe(currentBitmap: Bitmap, x: Int, y: Int): Int {
        val px = x.coerceIn(0, currentBitmap.width - 1)
        val py = y.coerceIn(0, currentBitmap.height - 1)

        return currentBitmap.getPixel(px, py)
    }
}

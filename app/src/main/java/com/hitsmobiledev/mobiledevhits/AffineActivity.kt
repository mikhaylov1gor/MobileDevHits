package com.hitsmobiledev.mobiledevhits

import android.content.ContentValues
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
import kotlin.math.roundToInt

class AffineActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri
    private lateinit var currentBitmap: Bitmap
    private var conditions = mutableListOf<Bitmap>()
    private var startPoints = mutableListOf<Point>()
    private var finishPoints = mutableListOf<Point>()

    private var topBorder = 0
    private var bottomBorder = 0
    private var leftBorder = 0
    private var rightBorder = 0
    private var scaling = 1f
    private var scale = 1f
    private var currentIndex = 0

    private var a = 1f
    private var b = 1f
    private var tx = 1f

    private var c = 1f
    private var d = 1f
    private var ty = 1f

    private var invA = 1f
    private var invB = 1f
    private var invTx = 1f

    private var invC = 1f
    private var invD = 1f
    private var invTy = 1f


    private var minX = 9999
    private var minY = -9999

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_affine)

        imageView = findViewById(R.id.currentPhoto)
        imageUri = intent.getParcelableExtra("currentPhoto")!!
        imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageView.setImageBitmap(imageBitmap)

        currentBitmap = imageBitmap
        conditions.add(currentBitmap)
        calculateBorders()

        val saveChangesButton: ImageButton = findViewById(R.id.button_save_changes)
        saveChangesButton.setOnClickListener {
            saveChanges(currentBitmap)
        }

        val returnToFiltersButton: ImageButton = findViewById(R.id.button_undo)
        returnToFiltersButton.setOnClickListener {
            returnToFilters()
        }

        val reduButton: ImageButton = findViewById(R.id.button_redu)
        reduButton.setOnClickListener{
            redu()
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
        if (currentIndex > 0) {
            currentBitmap = conditions[currentIndex - 1]
            imageView.setImageBitmap(currentBitmap)
            currentIndex--
        }
    }

    private fun redu(){
        if (currentIndex < conditions.size - 1){
            currentBitmap = conditions[currentIndex + 1]
            imageView.setImageBitmap(currentBitmap)
            currentIndex++
        }
    }

    private fun calculateCoeffs() {
        val startMatrix = arrayOf(
            floatArrayOf(startPoints[0].x.toFloat(), startPoints[0].y.toFloat(), 1f),
            floatArrayOf(startPoints[1].x.toFloat(), startPoints[1].y.toFloat(), 1f),
            floatArrayOf(startPoints[2].x.toFloat(), startPoints[2].y.toFloat(), 1f)
        )

        val detA = calculateMatrix(startMatrix)

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

    private fun saveChanges(currentBitmap: Bitmap) {
        val filename = "${System.currentTimeMillis()}.jpg"
        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos.use {
            if (it != null) {
                currentBitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            }
        }

        Toast.makeText(this, "Изображение сохранено", Toast.LENGTH_SHORT).show()
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

    private fun affineFunc(x: Int, y: Int): Point {
        return Point((a * x + b * y + tx).toInt(), (c * x + d * y + ty).toInt())
    }
    private fun calculateNewSize(currentBitmap: Bitmap): Pair<Int, Int> {
        val width = currentBitmap.width
        val height = currentBitmap.height


        var topLeft: Point = affineFunc(0,0)
        var topRight: Point = affineFunc(width - 1, 0)
        var bottomLeft:Point = affineFunc(0, height -1)
        var bottomRight:Point = affineFunc(width -1, height -1)

        minX = listOf(topLeft.x, topRight.x, bottomLeft.x, bottomRight.x).minOrNull() ?: 0
        minY = listOf(topLeft.y, topRight.y, bottomLeft.y, bottomRight.y).minOrNull() ?: 0
        val maxX = listOf(topLeft.x, topRight.x, bottomLeft.x, bottomRight.x).maxOrNull() ?: 0
        val maxY = listOf(topLeft.y, topRight.y, bottomLeft.y, bottomRight.y).maxOrNull() ?: 0


        val newWidth = maxX - minX
        val newHeight = maxY - minY

        return Pair(newWidth, newHeight)
    }

    private fun calculateInverseCoefficients() {
        val det = a * d - b * c

        invA = d / det
        invB = -b / det
        invC = -c / det
        invD = a / det

        invTx = (b * ty - d * tx) / det
        invTy = (c * tx - a * ty) / det
    }

    private fun findOld(x:Int, y:Int): Point{
        val x = invA * (x + minX) + invB * (y + minY) + invTx
        val y = invC * (x + minX) + invD * (y + minY) + invTy

        return Point(x.toInt(),y.toInt())
    }

    private fun affineTransform(currentBitmap: Bitmap) {


        if (startPoints.size < 3 || finishPoints.size < 3) {
            Toast.makeText(this, "Мало точек", Toast.LENGTH_SHORT).show()
            return
        }

        calculateCoeffs()
        calculateInverseCoefficients()

        val width = currentBitmap.width
        val height = currentBitmap.height
        val (newWidth, newHeight) = calculateNewSize(currentBitmap)

        scale = Math.max((newWidth.toFloat()/width.toFloat()),(newHeight.toFloat()/height.toFloat()))

        Toast.makeText(this, "$scale", Toast.LENGTH_SHORT).show()

        val transformedBitmap = Bitmap.createBitmap(newWidth, newHeight, currentBitmap.config)

        for (y in 0 until newHeight) {
            for (x in 0 until newWidth) {
                val newPoint: Point = findOld(x,y)


                val color = if (newPoint.x in 0 until width && newPoint.y in 0 until height){
                    if (scale > 1f) {
                        getBilinearColor(currentBitmap, newPoint.x.toFloat(), newPoint.y.toFloat())
                    }
                    else {
                        getTrilinearInterpolate(currentBitmap, newPoint.x, newPoint.y)
                    }
                }
                else {
                    Color.TRANSPARENT
                }
                transformedBitmap.setPixel(x, y, color)
            }
        }

        imageView.setImageBitmap(transformedBitmap)
        this.currentBitmap = transformedBitmap
        conditions.add(transformedBitmap)
        currentIndex++
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

        val red = bilinearInterpolate(Color.red(firstColor), Color.red(thirdColor), Color.red(secondColor), Color.red(fourthColor), x - x1, y - y1)
        val green = bilinearInterpolate(Color.green(firstColor), Color.green(thirdColor), Color.green(secondColor), Color.green(fourthColor), x - x1, y - y1)
        val blue = bilinearInterpolate(Color.blue(firstColor), Color.blue(thirdColor), Color.blue(secondColor), Color.blue(fourthColor), x - x1, y - y1)
        val a = bilinearInterpolate(Color.alpha(firstColor), Color.alpha(thirdColor), Color.alpha(secondColor), Color.alpha(fourthColor), x - x1, y - y1)

        return Color.argb(a,red, green, blue)
    }

    private fun bilinearInterpolate(c00: Int, c10: Int, c01: Int, firstColor: Int, tx: Float, ty: Float): Int {
        val a = c00 * (1 - tx) + c10 * tx
        val b = c01 * (1 - tx) + firstColor * tx
        return (a * (1 - ty) + b * ty).toInt()
    }

    private fun getTrilinearInterpolate(currentBitmap: Bitmap,x:Int,y:Int): Int{
        val color = currentBitmap.getPixel((x.toFloat()).toInt(),(y.toFloat()).toInt())
        var z = scale - scale.roundToInt()
        return (( 1 - z ) * color + z * color).toInt()

    }

    private fun getPixelSafe(currentBitmap: Bitmap, x: Int, y: Int): Int {
        val px = x.coerceIn(0, currentBitmap.width - 1)
        val py = y.coerceIn(0, currentBitmap.height - 1)

        return currentBitmap.getPixel(px, py)
    }
}

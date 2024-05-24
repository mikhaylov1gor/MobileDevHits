package com.hitsmobiledev.mobiledevhits

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge

class Interpolator(points: MutableList<Point>) {

    private val n = points.size - 1
    public val A: FloatArray = FloatArray(n + 1)
    public val B: FloatArray = FloatArray(n)
    public val C: FloatArray = FloatArray(n + 1)
    public val D: FloatArray = FloatArray(n)

    private val h: FloatArray = FloatArray(n)
    private val cProblemValue: FloatArray = FloatArray(n)

    private val s1: FloatArray = FloatArray(n + 1)
    private val s2: FloatArray = FloatArray(n + 1)
    private val s3: FloatArray = FloatArray(n + 1)

    init {

        for (i in 0 until points.size) {
            A[i] = points[i].y.toFloat()
        }

        for (i in 0 until n) {
            h[i] = (points[i + 1].x - points[i].x).toFloat()

            if (i > 0) {
                cProblemValue[i] = 3 * (A[i + 1] - A[i]) / h[i] - 3 * (A[i] - A[i - 1]) / h[i - 1];
            }
        }

        s1[0] = 1f
        s2[0] = 0f
        s3[0] = 0f

        for (i in 1 until n) {
            s1[i] =
                2 * (points[i + 1].x.toFloat() - points[i - 1].x.toFloat()) - h[i - 1] * s2[i - 1]
            s2[i] = h[i] / s1[i]
            s3[i] = (cProblemValue[i] - h[i - 1] * s3[i - 1]) / s1[i]
        }

        s1[n] = 1f
        s2[n] = 0f
        s3[n] = 0f

        for (i in n - 1 downTo 0) {
            C[i] = s3[i] - s2[i] * C[i + 1]
            B[i] = (A[i + 1] - A[i]) / h[i] - h[i] * (C[i + 1] + 2 * C[i]) / 3
            D[i] = (C[i + 1] - C[i]) / (3 * h[i])
        }
    }
}

class VectorEditActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri
    private var points = mutableListOf<Point>()

    @SuppressLint("ClickableViewAccessibility", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_vector_edit)

        imageView = findViewById(R.id.currentPhoto)
        imageUri = intent.getParcelableExtra("currentPhoto")!!
        imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageView.setImageBitmap(imageBitmap)

        var currentBitmap: Bitmap = imageBitmap



        val editButton: ImageButton = findViewById(R.id.button_edit)
        editButton.setOnClickListener {
            points = mutableListOf<Point>()
            edit(currentBitmap)
        }

        val startButton: ImageButton = findViewById(R.id.button_start)
        startButton.setOnClickListener {
            processing(currentBitmap, points)
        }

        imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                points.add(Point(event.x.toInt(), event.y.toInt()))
                imageView.invalidate()
                edit(currentBitmap)
            }
            true
        }

        val saveChangesButton: ImageButton = findViewById(R.id.button_save_changes)
        saveChangesButton.setOnClickListener {
            val newUri = saveBitmapToCache(this, imageBitmap)
            val intent = Intent(this@VectorEditActivity, ChooseFilterActivity::class.java)
            intent.putExtra("currentPhoto", newUri)
            if (imageUri != null) {
                deleteFileFromCache(imageUri)
            }
            this@VectorEditActivity.startActivity(intent)
            finish()
        }

        val cancelChangesButton: ImageButton = findViewById(R.id.button_cancel)
        cancelChangesButton.setOnClickListener {
            val intent = Intent(this@VectorEditActivity, ChooseFilterActivity::class.java)
            intent.putExtra("currentPhoto", imageUri)
            this@VectorEditActivity.startActivity(intent)
            finish()
        }
    }

    private fun returnToFilters() {
        val intent = Intent(this@VectorEditActivity, ChooseFilterActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        this@VectorEditActivity.startActivity(intent)
        finish()
    }


    private fun edit(currentBitmap: Bitmap) {
        val editedBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(editedBitmap)
        val paintCircle = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
        }
        val paintLine = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
            strokeWidth = 5f
        }

        if (points.size > 0) {
            canvas.drawCircle(points[0].x.toFloat(), points[0].y.toFloat(), 5f, paintCircle)

            for (i in 1 until points.size) {
                canvas.drawLine(
                    points[i - 1].x.toFloat(),
                    points[i - 1].y.toFloat(),
                    points[i].x.toFloat(),
                    points[i].y.toFloat(),
                    paintLine
                )
                canvas.drawCircle(points[i].x.toFloat(), points[i].y.toFloat(), 5f, paintCircle)
            }
        }
        imageView.setImageBitmap(editedBitmap)
    }

    private fun processing(currentBitmap: Bitmap, points: MutableList<Point>) {
        val editedBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(editedBitmap)

        val paintLine = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }

        val interpolator = Interpolator(points)
        val path = mutableListOf<Point>()
        for (i in 0 until points.size - 1) {
            var currentX = points[i].x

            if (currentX < points[i + 1].x) {
                while (currentX < points[i + 1].x) {
                    val currentY =
                        interpolator.A[i] + interpolator.B[i] * (currentX - points[i].x) + interpolator.C[i] * (currentX - points[i].x) * (currentX - points[i].x) + interpolator.D[i] * (currentX - points[i].x) * (currentX - points[i].x) * (currentX - points[i].x)
                    path.add(Point(currentX, currentY.toInt()))
                    currentX += 5
                }
            }

            if (currentX > points[i + 1].x) {
                while (currentX > points[i + 1].x) {
                    val currentY =
                        interpolator.A[i] + interpolator.B[i] * (currentX - points[i].x) + interpolator.C[i] * (currentX - points[i].x) * (currentX - points[i].x) + interpolator.D[i] * (currentX - points[i].x) * (currentX - points[i].x) * (currentX - points[i].x)
                    path.add(Point(currentX, currentY.toInt()))
                    currentX -= 5
                }
            }
        }

        for (i in 1 until path.size) {
            canvas.drawLine(
                path[i - 1].x.toFloat(),
                path[i - 1].y.toFloat(),
                path[i].x.toFloat(),
                path[i].y.toFloat(),
                paintLine
            )
        }

        imageView.setImageBitmap(editedBitmap)
        imageBitmap = editedBitmap
    }
}
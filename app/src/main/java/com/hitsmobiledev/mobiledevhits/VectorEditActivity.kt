package com.hitsmobiledev.mobiledevhits

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

class Interpolator(points: MutableList<Point>){

    private val size = points.size - 1
    public val aCoefs : FloatArray = FloatArray(size + 1)
    public val bCoefs : FloatArray = FloatArray(size)
    public val cCoefs : FloatArray = FloatArray(size + 1)
    public val dCoefs : FloatArray = FloatArray(size)
    private val h : FloatArray = FloatArray(size)
    private val cProblemValue : FloatArray = FloatArray(size)

    private val s1 : FloatArray = FloatArray(size+1)
    private val s2 : FloatArray = FloatArray(size+1)
    private val s3 : FloatArray = FloatArray(size+1)

    init {
        for (i in 0 until size){
            aCoefs[i] = points[i].y.toFloat()
            h[i] = (points[i+1].x - points[i].x).toFloat()

            if (i > 0){
                cProblemValue[i] = 3 * (aCoefs[i + 1] - aCoefs[i]) / h[i] - 3 * (aCoefs[i] - aCoefs[i - 1]) / h[i - 1]
            }
        }

        s1[0] = 1f
        s2[0] = 0f
        s3[0] = 0f

        s1[size] = 1f
        s2[size] = 0f
        s3[size] = 0f

        for (i in 1 until size){
            s1[i] = 2 * (points[i+1].x.toFloat() - points[i-1].x.toFloat()) - h[i-1] * s2[i-1]
            s2[i] = h[i] / s1[i]
            s3[i] = (cProblemValue[i] - h[i-1] * s3[i-1]) / s1[i]
        }

        for (i in size - 1 downTo  0){
            cCoefs[i] = s3[i] - s2[i] * cCoefs[i+1]
            bCoefs[i] = (aCoefs[i + 1] - aCoefs[i]) / h[i] - h[i] * (cCoefs[i + 1] + 2 * cCoefs[i]) / 3
            dCoefs[i] = (cCoefs[i + 1] - cCoefs[i]) / (3 * h[i])
        }
    }

}

class VectorEditActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri
    private var points = mutableListOf<Point>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_vector_edit)

        imageView = findViewById(R.id.currentPhoto)
        imageUri = intent.getParcelableExtra("currentPhoto")!!
        imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageView.setImageBitmap(imageBitmap)

        var currentBitmap: Bitmap = imageBitmap

        // button listeners
        val saveChangesButton: ImageButton = findViewById(R.id.button_save_vector_edit_changes)
        saveChangesButton.setOnClickListener {
            saveChanges(currentBitmap)
        }

        val returnToFiltersButton: ImageButton = findViewById(R.id.button_back_to_filters)
        returnToFiltersButton.setOnClickListener {
            returnToFilters()
        }

        // draw func
        val editButton: ImageButton = findViewById(R.id.button_edit)
        editButton.setOnClickListener {
            points = mutableListOf<Point>()
            edit(currentBitmap)
        }

        val startButton: ImageButton = findViewById(R.id.button_start)
        startButton.setOnClickListener {
            processing(currentBitmap, points)
        }

        // Set onTouchListener to imageView
        imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                points.add(Point(event.x.toInt(), event.y.toInt()))
                imageView.invalidate()
                edit(currentBitmap)
            }
            true
        }
    }

    private fun returnToFilters() {
        val intent = Intent(this@VectorEditActivity, ChooseFilterActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        this@VectorEditActivity.startActivity(intent)
    }

    private fun saveChanges(currentBitmap: Bitmap) {
        returnToFilters()
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

        val paintCircle = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
        }

        points.sortBy { it.x }
        val interpolator = Interpolator(points)
        val path = mutableListOf<Point>()
        for (i in 1 until points.size - 1) {
            var currentX = points[i-1].x

            while (currentX < points[i].x.toFloat()) {
                val currentY =
                    interpolator.aCoefs[i-1] + interpolator.bCoefs[i-1] * (currentX - points[i-1].x) + interpolator.cCoefs[i-1] * (currentX - points[i-1].x) * (currentX - points[i-1].x) + interpolator.dCoefs[i-1] * (currentX - points[i-1].x) * (currentX - points[i-1].x) * (currentX - points[i-1].x)
                path.add(Point(currentX, currentY.toInt()))
                currentX += 1
            }
        }

        for (i in 0 until path.size) {
            canvas.drawCircle(path[i].x.toFloat(), path[i].y.toFloat(), 5f, paintCircle)
        }

        imageView.setImageBitmap(editedBitmap)
    }
}
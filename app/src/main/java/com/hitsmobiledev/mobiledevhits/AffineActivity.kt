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
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge

class AffineActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri
    private var startPoints = mutableListOf<Point>()
    private var finishPoints = mutableListOf<Point>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_affine)

        imageView = findViewById(R.id.currentPhoto)
        imageUri = intent.getParcelableExtra("currentPhoto")!!
        imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageView.setImageBitmap(imageBitmap)

        val imageUri = intent.getParcelableExtra<Uri>("currentPhoto")
        imageView.setImageURI(imageUri)

        var currentBitmap: Bitmap = imageBitmap

        val saveChangesButton: ImageButton = findViewById(R.id.button_save_affine_changes)
        saveChangesButton.setOnClickListener {
            saveChanges(currentBitmap)
        }

        val returnToFiltersButton: ImageButton = findViewById(R.id.button_back_to_filters)
        returnToFiltersButton.setOnClickListener {
            returnToFilters()
        }

        val setStartPoints: Button = findViewById(R.id.button_start_points)
        setStartPoints.setOnClickListener{
            setStartPoints(currentBitmap)
        }

        val setFinishPoints: Button = findViewById(R.id.button_end_points)
        setFinishPoints.setOnClickListener{
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
        this@AffineActivity.startActivity(intent)
    }

    private fun saveChanges(currentBitmap: Bitmap) {
        returnToFilters()
    }

    private fun setStartPoints(currentBitmap:Bitmap){
        edit(startPoints,currentBitmap)
        imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (startPoints.size == 3){startPoints= mutableListOf<Point>()}
                startPoints.add(Point(event.x.toInt(), event.y.toInt()))
                edit(startPoints, currentBitmap)
                imageView.invalidate()
            }
            true
        }
    }

    private fun setEndPoints(currentBitmap: Bitmap){
        edit(finishPoints,currentBitmap)
        imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (finishPoints.size == 3){finishPoints= mutableListOf<Point>()}
                finishPoints.add(Point(event.x.toInt(), event.y.toInt()))
                edit(finishPoints, currentBitmap)
                imageView.invalidate()
            }
            true
        }
    }

    private fun edit(points: MutableList<Point>, currentBitmap: Bitmap){
        val editedBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(editedBitmap)

        val firstCircle = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        val secondCircle = Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL
        }

        val thirdCircle = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }

        for (i in 0 until points.size){
            if (i == 0){canvas.drawCircle(points[0].x.toFloat(), points[0].y.toFloat(), 10f, firstCircle)}
            if (i == 1){canvas.drawCircle(points[1].x.toFloat(), points[1].y.toFloat(), 10f, secondCircle)}
            if (i == 2){canvas.drawCircle(points[2].x.toFloat(), points[2].y.toFloat(), 10f, thirdCircle)}
        }

        imageView.setImageBitmap(editedBitmap)
    }

    private fun affineTransform(currentBitmap: Bitmap) {
        // Создание матрицы аффинного преобразования
        val matrix = android.graphics.Matrix()

        // Установка аффинного преобразования на основе заданных точек
        matrix.setPolyToPoly(
            floatArrayOf(
                startPoints[0].x.toFloat(), startPoints[0].y.toFloat(),
                startPoints[1].x.toFloat(), startPoints[1].y.toFloat(),
                startPoints[2].x.toFloat(), startPoints[2].y.toFloat()
            ), 0,
            floatArrayOf(
                finishPoints[0].x.toFloat(), finishPoints[0].y.toFloat(),
                finishPoints[1].x.toFloat(), finishPoints[1].y.toFloat(),
                finishPoints[2].x.toFloat(), finishPoints[2].y.toFloat()
            ), 0,
            3
        )

        val transformedBitmap = Bitmap.createBitmap(
            (currentBitmap.width * 1.5f).toInt(),
            (currentBitmap.height * 1.5f).toInt(),
            currentBitmap.config
        )

        // Применение аффинного преобразования к битмапу
        val canvas = android.graphics.Canvas(transformedBitmap)
        canvas.drawBitmap(currentBitmap, matrix, null)

        imageView.setImageBitmap(transformedBitmap)
    }
}

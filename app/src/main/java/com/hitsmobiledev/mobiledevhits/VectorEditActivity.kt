package com.hitsmobiledev.mobiledevhits

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.MotionEvent
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge

class VectorEditActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri
    private val points = mutableListOf<Point>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_vector_edit)

        imageView = findViewById(R.id.currentPhoto)
        imageUri = intent.getParcelableExtra("currentPhoto")!!
        imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
        imageView.setImageBitmap(imageBitmap)

        var currentBitmap :Bitmap = imageBitmap

        // button listeners
        val saveChangesButton: ImageButton = findViewById(R.id.button_save_vector_edit_changes)
        saveChangesButton.setOnClickListener{
            saveChanges(currentBitmap)
        }

        val returnToFiltersButton: ImageButton = findViewById(R.id.button_back_to_filters)
        returnToFiltersButton.setOnClickListener{
            returnToFilters()
        }

        // draw func
        val editButton: ImageButton = findViewById(R.id.button_edit)
        editButton.setOnClickListener{
            startDrawing()
        }

        val startButton: ImageButton = findViewById(R.id.button_start)
        startButton.setOnClickListener{
            startProcessing()
        }

        // Set onTouchListener to imageView
        imageView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                points.add(Point(event.x.toInt(), event.y.toInt()))
                imageView.invalidate()
            }
            true
        }
    }

    private fun returnToFilters(){
        val intent = Intent(this@VectorEditActivity, ChooseFilterActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        this@VectorEditActivity.startActivity(intent)
    }

    private fun saveChanges(currentBitmap: Bitmap) {
        returnToFilters()
    }

    private fun edit(currentBitmap: Bitmap){
        val editedBitmap = currentBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(editedBitmap)
        val paintCircle = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
        }
        val paintLine = Paint().apply {
            color = Color.YELLOW
            style = Paint.Style.FILL
            strokeWidth = 2f
        }

        canvas.drawCircle(points[0].x.toFloat(),points[0].y.toFloat(),5f,paintCircle)
        for (i in 1 until points.size){
            canvas.drawLine(points[i-1].x.toFloat(),points[i-1].y.toFloat(),points[i].x.toFloat(),points[i].y.toFloat(),paintLine)
            canvas.drawCircle(points[i].x.toFloat(),points[i].y.toFloat(),5f,paintCircle)
        }
        imageView.setImageBitmap(editedBitmap)
    }

    private fun processing(){

    }

    private fun startDrawing() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            edit(imageBitmap)
        }, 0)
    }

    private fun startProcessing() {
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            processing()
        }, 0)
    }
}
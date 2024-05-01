package com.hitsmobiledev.mobiledevhits

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge

class VectorEditActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var imageBitmap: Bitmap
    private lateinit var imageUri: Uri


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
            edit(currentBitmap)
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
    }

}

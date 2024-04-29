package com.hitsmobiledev.mobiledevhits

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val permissionCode = 123

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val cameraButton: ImageButton = findViewById(R.id.CameraButton)
        val galleryButton: ImageButton = findViewById(R.id.ShowGalleryButton)

        cameraButton.setOnClickListener {
            Toast.makeText(this, "Кнопка нажата", Toast.LENGTH_SHORT).show()
        }

        galleryButton.setOnClickListener{
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Запрашиваем разрешение, если оно не было предоставлено
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), permissionCode)
            } else {
                loadGalleryPhotos()
            }
        }
    }

    private fun loadGalleryPhotos () {
        val imageList = ArrayList<Uri>()
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val limit = 10
            var count = 0
            while (it.moveToNext() && count < limit) {
                count++
                val imageId = it.getLong(columnIndex)
                val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val imageUri = Uri.withAppendedPath(contentUri, imageId.toString())
                imageList.add(imageUri)
            }
        }
        displayGalleryPhotos(imageList)
    }

    private fun displayGalleryPhotos(imageList: List<Uri>) {
        val linearLayout: LinearLayout = findViewById(R.id.PhotosLinearLayout)
        linearLayout.removeAllViews()

        for (imageUri in imageList) {
            val imageView = ImageView(this)
            imageView.setImageURI(imageUri)
            linearLayout.addView(imageView)
        }
    }
}

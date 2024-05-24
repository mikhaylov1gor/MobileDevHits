package com.hitsmobiledev.mobiledevhits

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private val permissionCode = 123

    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>
    private var selectedImages = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val galleryButton: ImageButton = findViewById(R.id.ShowGalleryButton)
        galleryButton.setOnClickListener {
            loadGalleryPhotos()
        }

        val cameraButton: ImageButton = findViewById(R.id.CameraButton)
        cameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        pickImagesLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                    val data = result.data
                    if (data?.clipData != null) {
                        val clipData = data.clipData
                        for (i in 0 until clipData!!.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            selectedImages.add(uri)
                        }
                    } else if (data?.data != null) {
                        val uri = data.data!!
                        selectedImages.add(uri)
                    }
                    displayGalleryPhotos()
                }
            }
    }

    @SuppressLint("QueryPermissionsNeeded")
    fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, permissionCode)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == permissionCode && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap

            val uri = saveImageToGallery(imageBitmap)

            uri?.let {
                selectedImages.add(it)
                displayGalleryPhotos()
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap): Uri? {
        val contentResolver = applicationContext.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "image_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }

        val imageUri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        imageUri?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            }
            return uri
        }
        return null
    }

    private fun checkCameraPermission(): Boolean {
        return checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        requestPermissions(arrayOf(Manifest.permission.CAMERA), permissionCode)
    }

    private fun openToolSelector(image: Uri) {
        val intent = Intent(this@MainActivity, ChooseFilterActivity::class.java)
        intent.putExtra("currentPhoto", image)
        this@MainActivity.startActivity(intent)
    }

    private fun loadGalleryPhotos() {
        val intent = if (Build.VERSION.SDK_INT >= 33) {
            Intent(MediaStore.ACTION_PICK_IMAGES).apply {
                type = "image/*"
                putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 10)
            }
        } else {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
        pickImagesLauncher.launch(intent)
    }

    private fun displayGalleryPhotos() {
        val linearLayout: LinearLayout = findViewById(R.id.PhotosLinearLayout)
        linearLayout.removeAllViews()

        for (imageUri in selectedImages) {
            val imageButton = ImageButton(this)

            val buttonSize = 750
            val layoutParams = LinearLayout.LayoutParams(buttonSize, buttonSize)
            layoutParams.setMargins(16, 16, 16, 16)
            layoutParams.gravity = Gravity.CENTER
            imageButton.layoutParams = layoutParams
            imageButton.scaleType = ImageView.ScaleType.FIT_CENTER

            imageButton.setOnClickListener {
                openToolSelector(imageUri)
            }

            imageButton.setImageURI(imageUri)
            linearLayout.addView(imageButton)
        }
    }
}
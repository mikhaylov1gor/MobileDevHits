package com.hitsmobiledev.mobiledevhits

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
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

    private lateinit var pickImagesLauncher: ActivityResultLauncher<Intent>

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

        pickImagesLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImages = mutableListOf<Uri>()
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
                displayGalleryPhotos(selectedImages)
            }
        }
    }

    fun openCamera(view: View) {
        val intent = Intent(this@MainActivity, CameraActivity::class.java)
        this@MainActivity.startActivity(intent)
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
                putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 10)  // or any other number for maximum images
            }
        } else {
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }
        pickImagesLauncher.launch(intent)
    }

    private fun displayGalleryPhotos(imageList: List<Uri>) {
        val linearLayout: LinearLayout = findViewById(R.id.PhotosLinearLayout)
        linearLayout.removeAllViews()

        for (imageUri in imageList) {
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
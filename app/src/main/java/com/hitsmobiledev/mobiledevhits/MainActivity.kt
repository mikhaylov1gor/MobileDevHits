package com.hitsmobiledev.mobiledevhits

import android.Manifest
import android.R.attr.value
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
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

        val galleryButton: ImageButton = findViewById(R.id.ShowGalleryButton)

        galleryButton.setOnClickListener{
            loadGalleryPhotos()
        }
    }

     fun openCamera(view: View) {
         val intent: Intent = Intent(this@MainActivity, CameraActivity::class.java)
         this@MainActivity.startActivity(intent)
     }

    private fun openToolSelector(image: Uri){
        val intent: Intent = Intent(this@MainActivity, ChooseToolActivity::class.java)
        intent.putExtra("currentPhoto", image)
        this@MainActivity.startActivity(intent)
    }


    private val PICK_IMAGE_REQUEST = 1
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImages = mutableListOf<Uri>()
            if (data.clipData != null) {
                val clipData = data.clipData
                for (i in 0 until clipData!!.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    selectedImages.add(uri)
                }
            } else if (data.data != null) {
                val uri = data.data!!
                selectedImages.add(uri)
            }

            displayGalleryPhotos(selectedImages)
        }
    }
    private fun loadGalleryPhotos() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    private fun displayGalleryPhotos(imageList: List<Uri>) {
        val linearLayout: LinearLayout = findViewById(R.id.PhotosLinearLayout)
        linearLayout.removeAllViews()

        for (imageUri in imageList) {
            val imageView = ImageButton(this)

            imageView.setOnClickListener{
                openToolSelector(imageUri)
            }

            imageView.setImageURI(imageUri)
            linearLayout.addView(imageView)
        }
    }
}

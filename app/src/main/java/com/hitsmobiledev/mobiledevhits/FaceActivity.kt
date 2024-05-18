package com.hitsmobiledev.mobiledevhits

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

class FaceActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_face)

        imageView = findViewById(R.id.currentPhoto)

        val imageUri = intent.getParcelableExtra<Uri>("currentPhoto")
        imageView.setImageURI(imageUri)
    }
}

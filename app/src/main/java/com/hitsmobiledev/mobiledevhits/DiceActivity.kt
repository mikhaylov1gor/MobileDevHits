package com.hitsmobiledev.mobiledevhits

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge

class DiceActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dice)

        imageView = findViewById(R.id.currentPhoto)

        val imageUri = intent.getParcelableExtra<Uri>("currentPhoto")
        imageView.setImageURI(imageUri)
    }
}

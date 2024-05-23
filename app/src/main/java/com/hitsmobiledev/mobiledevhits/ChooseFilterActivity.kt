package com.hitsmobiledev.mobiledevhits

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider

class ChooseFilterActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var filterViewModel: filterViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_filter)

        filterViewModel = ViewModelProvider(this).get(filterViewModel::class.java)

        imageView = findViewById(R.id.currentPhoto)

        val imageUri = intent.getParcelableExtra<Uri>("currentPhoto")
        imageView.setImageURI(imageUri)
    }
}

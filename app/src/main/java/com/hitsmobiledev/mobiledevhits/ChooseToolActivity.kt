package com.hitsmobiledev.mobiledevhits

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class ChooseToolActivity :  AppCompatActivity() {
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tools)

        imageView = findViewById(R.id.currentPhoto)
        val imageUri = intent.getParcelableExtra<Uri>("currentPhoto")
        imageUri?.let { uri ->
            imageView.setImageURI(uri)
        }

        val buttonReturn : ImageButton = findViewById(R.id.button_return_2)
        buttonReturn.setOnClickListener{
            returnBack()
        }
    }

    fun returnBack(){
        val intent: Intent = Intent(
            this@ChooseToolActivity,
            MainActivity::class.java
        )

        this@ChooseToolActivity.startActivity(intent)
    }
}
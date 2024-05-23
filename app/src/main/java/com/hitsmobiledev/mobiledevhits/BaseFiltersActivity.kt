package com.hitsmobiledev.mobiledevhits

import NavigateFragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.hitsmobiledev.mobiledevhits.cube.CubeActivity


open class BaseFiltersActivity : AppCompatActivity() {
    private lateinit var imageUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigate)

        imageUri = intent.getParcelableExtra("currentPhoto")!!

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, NavigateFragment())
            .commit()
    }

    fun chooseRotateFilter(view: View){
        val intent = Intent(this@BaseFiltersActivity, RotateActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        startActivity(intent)
        super.onStop()
    }

    fun chooseColorFilter(view: View){
        val intent = Intent(this@BaseFiltersActivity, ColorCorrectionActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        startActivity(intent)
        super.onStop()
    }

    fun chooseAffineFilter(view: View){
        val intent = Intent(this@BaseFiltersActivity, AffineActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        startActivity(intent)
        super.onStop()
    }

    fun chooseBlurFilter(view: View){
        val intent = Intent(this@BaseFiltersActivity, BlurActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        startActivity(intent)
        super.onStop()
    }

    fun chooseFaceFilter(view: View){
        val intent = Intent(this@BaseFiltersActivity, FaceActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        startActivity(intent)
        super.onStop()
    }

    fun chooseDiceFilter(view: View){
        val intent = Intent(this@BaseFiltersActivity, CubeActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        startActivity(intent)
        super.onStop()
    }

    fun chooseRetouchFilter(view: View){
        val intent = Intent(this@BaseFiltersActivity, RetouchActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        startActivity(intent)
        super.onStop()
    }

    fun chooseVectorEditFilter(view: View){
        val intent = Intent(this@BaseFiltersActivity, VectorEditActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        startActivity(intent)
        super.onStop()
    }

    fun chooseScaleFilter(view: View){
        val intent = Intent(this@BaseFiltersActivity, ScalingActivity::class.java)
        intent.putExtra("currentPhoto", imageUri)
        startActivity(intent)
        super.onStop()
    }

    fun returnBack(view: View){
        val intent = Intent(this@BaseFiltersActivity, MainActivity::class.java)
        startActivity(intent)
        super.onStop()
    }
}
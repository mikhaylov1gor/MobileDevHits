package com.hitsmobiledev.mobiledevhits

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import com.google.android.material.button.MaterialButton
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect
import org.opencv.core.Point
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.CascadeClassifier
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class FaceActivity : BaseFiltersActivity() {
    private lateinit var imageView: ImageView
    private lateinit var detectButton: MaterialButton
    private lateinit var cascadeClassifier: CascadeClassifier
    private lateinit var resultBitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_face)

        imageView = findViewById(R.id.currentPhoto)
        detectButton = findViewById(R.id.detect_button)

        val imageUri = intent.getParcelableExtra<Uri>("currentPhoto")
        imageView.setImageURI(imageUri)
        resultBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)

        detectButton.setOnClickListener {
            val cascadeFile = getCascadeFile(resources)
            var i = OpenCVLoader.initDebug()
            cascadeClassifier = CascadeClassifier(cascadeFile.absolutePath)

            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            if (bitmap != null) {
                detectFaces(bitmap)
            }
        }

        val saveChangesButton: ImageButton = findViewById(R.id.button_save_changes)
        saveChangesButton.setOnClickListener {
            val newUri = saveBitmapToCache(this, resultBitmap)
            val intent = Intent(this@FaceActivity, ChooseFilterActivity::class.java)
            intent.putExtra("currentPhoto", newUri)
            if (imageUri != null) {
                deleteFileFromCache(imageUri)
            }
            this@FaceActivity.startActivity(intent)
            finish()
        }

        val cancelChangesButton: ImageButton = findViewById(R.id.button_cancel)
        cancelChangesButton.setOnClickListener {
            val intent = Intent(this@FaceActivity, ChooseFilterActivity::class.java)
            intent.putExtra("currentPhoto", imageUri)
            this@FaceActivity.startActivity(intent)
            finish()
        }

    }

    private fun detectFaces(bitmap : Bitmap) {
        val mat = Mat()
        Utils.bitmapToMat(bitmap, mat)

        val grayMat = Mat()
        Imgproc.cvtColor(mat, grayMat, Imgproc.COLOR_RGBA2GRAY)

        val faces = MatOfRect()
        cascadeClassifier.detectMultiScale(
            grayMat,
            faces,
            1.1,
            2,
            2,
            Size(30.0, 30.0),
            Size()
        )

        val faceArray = faces.toArray()
        for (rect in faceArray) {
            Imgproc.rectangle(
                mat,
                Point(rect.x.toDouble(), rect.y.toDouble()),
                Point((rect.x + rect.width).toDouble(), (rect.y + rect.height).toDouble()),
                Scalar(0.0, 255.0, 0.0, 255.0),
                3
            )
        }


        resultBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)

        Utils.matToBitmap(mat, resultBitmap)
        imageView.setImageBitmap(resultBitmap)

        mat.release()
    }
    private fun getCascadeFile(resources: Resources): File {
        val cascadeDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val cascadeFile = File(cascadeDir, "haarcascade_frontalface_default.xml")

        resources.openRawResource(R.raw.haarcascade_frontalface_default).use { inputStream ->
            FileOutputStream(cascadeFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return cascadeFile
    }
}

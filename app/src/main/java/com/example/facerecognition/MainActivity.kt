package com.example.facerecognition

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var predictButton: Button
    private lateinit var retrofit: Retrofit
    private lateinit var imagePredictionAPI: ImagePredictionAPI

    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val REQUEST_IMAGE_CAPTURE = 200
        private const val BASE_URL = "http://localhost:8000/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        predictButton = findViewById(R.id.predictButton)

//         Buat Retrofit instance
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Buat instance ImagePredictionAPI
        imagePredictionAPI = retrofit.create(ImagePredictionAPI::class.java)

        // Aksi klik pada tombol prediksi
        predictButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }
    }

    // Fungsi untuk membuka kamera
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

    // Callback setelah pengguna mengambil gambar
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(imageBitmap)

            // Mengirim gambar ke URL API
            sendImage(imageBitmap)
        }
    }

    // Fungsi untuk mengirim gambar ke URL API
    private fun sendImage(imageBitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()

        val requestFile = byteArray.toRequestBody("image/jpg".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile)

        // Permintaan API
        val call: Call<PredictionResponse> = imagePredictionAPI.uploadImage(body)
        call.enqueue(object : Callback<PredictionResponse> {
            override fun onResponse(
                call: Call<PredictionResponse>,
                response: Response<PredictionResponse>
            ) {
                if (response.isSuccessful) {
                    val predictionResponse = response.body()
                    predictionResponse?.let {
                        val predictionName = it.name
                        // Menampilkan prediksi nama
                        Toast.makeText(
                            applicationContext,
                            "Prediction: $predictionName",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Failed to get prediction",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<PredictionResponse>, t: Throwable) {
                Log.e("Prediction", "Error: ${t.message}")
                Toast.makeText(
                    applicationContext,
                    "Failed to get prediction",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    // Callback untuk hasil permintaan izin kamera
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Camera permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

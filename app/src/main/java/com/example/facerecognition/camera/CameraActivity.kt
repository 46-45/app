package com.example.facerecognition.camera

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.facerecognition.ImagePredictionAPI
import com.example.facerecognition.MainActivity
import com.example.facerecognition.PredictionResponse
import com.example.facerecognition.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream

class CameraActivity : AppCompatActivity() {

    val REQUEST_CODE = 200

    private val LOCATION_PERMISSION_REQ_CODE = 1000;
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    lateinit var logo: ImageView
    lateinit var txtMap : TextView
    lateinit var btnBack : TextView
    lateinit var btnCamera : ImageButton
    lateinit var llDetail : LinearLayout

    private lateinit var retrofit: Retrofit
    private lateinit var imagePredictionAPI: ImagePredictionAPI

    companion object {
        private const val BASE_URL = "http://localhost:8000/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Buat instance ImagePredictionAPI
        imagePredictionAPI = retrofit.create(ImagePredictionAPI::class.java)

        logo = findViewById(R.id.iv)
        txtMap = findViewById(R.id.tvMap)
        btnBack = findViewById(R.id.btnBack)
        btnCamera = findViewById(R.id.btnCamera)
        llDetail = findViewById(R.id.llDetail)


        if (ContextCompat.checkSelfPermission(this@CameraActivity,
                Manifest.permission.ACCESS_FINE_LOCATION) !==
            PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@CameraActivity,
                    Manifest.permission.CAMERA)) {
                ActivityCompat.requestPermissions(this@CameraActivity,
                    arrayOf(Manifest.permission.CAMERA), 1)
            } else {
                ActivityCompat.requestPermissions(this@CameraActivity,
                    arrayOf(Manifest.permission.CAMERA), 1)
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnBack.setOnClickListener {
            onBackPressed()
        }

        btnCamera.setOnClickListener {
            capturePhoto()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(this@CameraActivity,
                            Manifest.permission.CAMERA) ===
                                PackageManager.PERMISSION_GRANTED)) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                        capturePhoto()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
            LOCATION_PERMISSION_REQ_CODE -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                } else {
                    // permission denied
                    Toast.makeText(this, "You need to grant permission to access location",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun capturePhoto() {

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
        cameraIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
        cameraIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        startActivityForResult(cameraIntent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE && data != null){
            logo.setImageBitmap(data.extras?.get("data") as Bitmap)
            sendImage(data.extras?.get("data") as Bitmap)
            llDetail.visibility = View.VISIBLE
            getCurrentLocation()
        }
    }

    private fun getCurrentLocation() {
        // checking location permission
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request permission
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQ_CODE);
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // getting the last known or current location
                latitude = location.latitude
                longitude = location.longitude
//                tvLatitude.text = "Latitude: ${location.latitude}"
//                tvLongitude.text = "Longitude: ${location.longitude}"
                txtMap.text = "${latitude}, ${longitude} -> GD 5"
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed on getting current location",
                    Toast.LENGTH_SHORT).show()
            }
    }

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
}
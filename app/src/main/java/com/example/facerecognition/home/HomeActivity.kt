package com.example.facerecognition.home

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.facerecognition.R
import com.example.facerecognition.camera.CameraActivity

class HomeActivity : AppCompatActivity() {

    lateinit var btnAttend : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        btnAttend = findViewById(R.id.btn_attend)

        btnAttend.setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
    }
}
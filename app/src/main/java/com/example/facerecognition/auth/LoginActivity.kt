package com.example.facerecognition.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.facerecognition.MainActivity
import com.example.facerecognition.R
import com.example.facerecognition.home.HomeActivity

class LoginActivity : AppCompatActivity() {

    lateinit var btnLogin : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        btnLogin = findViewById(R.id.buttonLogin)

        btnLogin.setOnClickListener {
            startActivity(Intent(this,HomeActivity::class.java))
            finish()
        }

    }
}
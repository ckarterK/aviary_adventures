package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

class welcomePage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_page)


        var loginButton = findViewById<Button>(R.id.WelcomePage_LoginButton)
        var registerButton = findViewById<Button>(R.id.WelcomePage_RegisterButton)


        loginButton.setOnClickListener {

            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }

        registerButton.setOnClickListener {


            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)








        }
    }


}
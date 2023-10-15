package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

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

            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }
    }
}
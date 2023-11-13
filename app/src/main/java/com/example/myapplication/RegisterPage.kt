package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.myapplication.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        var registerButton = findViewById<Button>(R.id.RegisterPage_RegisterButton)
        var loginButton=findViewById<Button>(R.id.RegisterPage_LoginButton)
        val auth=FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()

        loginButton.setOnClickListener {
            val intent = Intent(this, LoginPage::class.java)
            startActivity(intent)
        }

        registerButton.setOnClickListener {
            val fullName = findViewById<EditText>(R.id.RegisterPage_fullName).text.toString()
            val email = findViewById<EditText>(R.id.RegisterPage_Email)
            var emailText=email.text.toString()
            val password = findViewById<EditText>(R.id.RegisterPage_Password).text.toString()
            Log.d("email", emailText)
            var isvalid=true
            if (fullName.isEmpty()) {
                findViewById<EditText>(R.id.RegisterPage_fullName).error = "Please enter your full name"
                isvalid=false
            }

            if (emailText.isEmpty()) {
                findViewById<EditText>(R.id.RegisterPage_Email).error = "Please enter a valid email"
                isvalid=false
            }else if (!isEmailValid(emailText)) {
                email.error = "please enter a valid email e.g: sam@gmail.com"
                isvalid = false
            }

            if (password.isEmpty()) {
                findViewById<EditText>(R.id.RegisterPage_Password).error = "Please enter a valid password"
                isvalid=false
            }else if(password.length<6){
                findViewById<EditText>(R.id.RegisterPage_Password).error = "password must be 6 characters long"
            }

            if(isvalid){
            auth.createUserWithEmailAndPassword(emailText, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful, user is signed in
                    val user: FirebaseUser? = auth.currentUser
                    User.staticUser.setName(fullName)
                    User.staticUser.setEmail(emailText)
                    User.staticUser.setUid(user!!.uid)

                    // Create a DatabaseReference to the user's data
                    val databaseReference: DatabaseReference = database.getReference("Users").child(user.uid)

                    // Save the user's data to the database
                    databaseReference.setValue(User.staticUser.createUserToMap()).addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            // Data saved successfully
                            val intent = Intent(this, MapsActivity::class.java)
                            startActivity(intent)
                        } else {
                            // Error saving data to the database
                            val dbException = dbTask.exception
                            // Handle the error (e.g., show a message to the user)
                        }
                    }
                } else {
                    // Registration failed
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        email.error="this email already exits"
                        Log.d("RegisterPage", "Email is already in use.")

                    } else {
                        // Handle other registration errors
                        // Show a general error message to the user
                        Log.e("RegisterPage", "Registration failed", exception)
                    }
                }
            }
        }
        }

    }
    private fun isEmailValid(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

}
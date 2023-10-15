package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_page)

        var registerButton = findViewById<Button>(R.id.RegisterPage_RegisterButton)
        val auth=FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()



        registerButton.setOnClickListener {
            val fullName = findViewById<EditText>(R.id.RegisterPage_fullName).text.toString()
            val email = findViewById<EditText>(R.id.RegisterPage_Email).text.toString()
            val password = findViewById<EditText>(R.id.RegisterPage_Password).text.toString()
            Log.d("email", email)

            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Registration successful, user is signed in
                    val user: FirebaseUser? = auth.currentUser
                    User.staticUser.setName(fullName)
                    User.staticUser.setEmail(email)
                    User.staticUser.setUid(user!!.uid)

                    // Create a DatabaseReference to the user's data
                    val databaseReference: DatabaseReference = database.getReference("Users").child(user.uid)

                    // Save the user's data to the database
                    databaseReference.setValue(User.staticUser.toMap()).addOnCompleteListener { dbTask ->
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
                    // Handle the error (e.g., show a message to the user)
                }
            }

        }
    }
}
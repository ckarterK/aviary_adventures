package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.myapplication.Models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginPage : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_page)

        auth = FirebaseAuth.getInstance()
        var registerButton = findViewById<Button>(R.id.LoginPage_RegisterButton)
        var loginButton= findViewById<Button>(R.id.LoginPage_LoginButton)

        registerButton.setOnClickListener {

            val intent = Intent(this, RegisterPage::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val email = findViewById<EditText>(R.id.LoginPage_Email)
            var emailText=email.text.toString()
            val password = findViewById<EditText>(R.id.LoginPage_Password)
            var passwordText=password.text.toString()

            if (emailText.isEmpty() || passwordText.isEmpty()) {
                // Display an error message for empty email or password fields
                Toast.makeText(this@LoginPage, "Email and password are required", Toast.LENGTH_LONG).show()

                email.error="please enter a email"
                password.error="please enter a password"

            } else {
                auth.signInWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user?.uid.toString()
                            databaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
                            databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        val dbemail = snapshot.child("email").getValue(String::class.java)
                                        val dbname = snapshot.child("name").getValue(String::class.java)
                                        var dbMeasurement = snapshot.child("preferedMeasurement").getValue(String::class.java)
                                        var dbRadius = snapshot.child("PreferedRadius").getValue(Long::class.java)

                                        if (dbemail != null && dbname != null) {
                                            User.staticUser.setUid(userId)
                                            User.staticUser.setEmail(dbemail)
                                            User.staticUser.setName(dbname)
                                            if (dbMeasurement != null) {
                                                User.staticUser.setpreferedMeasurement(dbMeasurement)
                                            } else {
                                                User.staticUser.setpreferedMeasurement(User.Measurement.KILOMETERS)
                                            }
                                            if (dbRadius != null) {
                                                User.staticUser.setPreferedRadius(dbRadius.toInt())
                                            } else {
                                                User.staticUser.setPreferedRadius(0)
                                            }

                                            val intent = Intent(this@LoginPage, createObservation::class.java)
                                            startActivity(intent)
                                        } else {
                                            // Notify the user that their email or password is incorrect
                                            Toast.makeText(this@LoginPage, "Incorrect email or password", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        // Notify the user that their email or password is incorrect
                                        Toast.makeText(this@LoginPage, "Incorrect email or password", Toast.LENGTH_LONG).show()
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle database errors
                                    Toast.makeText(this@LoginPage, "Database Error: ${error.message}", Toast.LENGTH_LONG).show()
                                }
                            })
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle other authentication failures
                        Toast.makeText(this@LoginPage, "this account doesn't exist", Toast.LENGTH_LONG).show()
                        email.error=" invalid email address"
                        password.error=" invalid password"
                    }
            }
        }



    }
}
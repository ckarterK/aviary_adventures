package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
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

            val email = findViewById<EditText>(R.id.LoginPage_Email).text.toString()
            val password = findViewById<EditText>(R.id.LoginPage_Password).text.toString()
            Log.d("outside sign in","before")
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("inside sign in","is succesfull")
                                val user = auth.currentUser
                                val userId = user?.uid.toString()
                                Log.d("inside snapshot","${userId}")
                                databaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(userId)
                                databaseReference.addValueEventListener(object :
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val dbemail = snapshot.child("email").getValue(String::class.java)
                                            val dbname = snapshot.child("name").getValue(String::class.java)

                                            Log.d("inside snapshot","u inside")
                                            if (dbemail != null && dbname != null) {
                                                User.staticUser.setUid(userId)
                                                User.staticUser.setEmail(dbemail)
                                                User.staticUser.setName(dbname)

                                                val intent = Intent(this@LoginPage, createObservation::class.java)
                                                startActivity(intent)
                                                
                                            } else {
                                                Toast.makeText(this@LoginPage, "No snapshot", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(this@LoginPage, "Database Error: ${error.message}", Toast.LENGTH_LONG).show()
                                    }
                                })
                            }
                        }
                        .addOnFailureListener {
                            Log.d("faliure","is succesfull")
                        }

            }


    }
}
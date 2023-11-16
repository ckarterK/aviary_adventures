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
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class updateProfilePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile_page)



        var saveBtn=findViewById<Button>(R.id.updateProfile_saveBtn)
        var discardBtn=findViewById<Button>(R.id.updateProfile_discard)

        // Get a reference to the Firebase Database
        val databaseRef: DatabaseReference = FirebaseDatabase.getInstance().reference
        val uid = User.staticUser?.getUid().toString()
        val userNodePath = "Users/$uid"
        val user = FirebaseAuth.getInstance().currentUser

        // Button click listener to go back to the settings page
        discardBtn.setOnClickListener {
            val intent = Intent(this, settingsPage::class.java)
            startActivity(intent)
        }


// Button click listener to update user profile data
        saveBtn.setOnClickListener {
            var fullname=findViewById<EditText>(R.id.updateProfile_fullName).text.toString()
            var password=findViewById<EditText>(R.id.updateProfile_password).text.toString()

            // Update the full name if it's not empty
            if (!fullname.isEmpty())
            {
                val updatedData = mapOf("$userNodePath/name" to fullname)

                databaseRef.updateChildren(updatedData)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            "new fullname saved",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        Log.d("not saved ","${e}")
                    }

            }
            // Update the password if it's not empty
            if(!password.isEmpty())
            {
                if(password.length<6){
                    findViewById<EditText>(R.id.updateProfile_password).error = "password must be 6 characters long"
                }else{
                    user?.updatePassword(password)
                        ?.addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Password updated successfully
                                Toast.makeText(
                                    this,
                                    "new password saved",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // If updating the password fails, handle the error
                                val exception = task.exception
                                Log.d("not saved ","${exception}")
                            }
                        }
                }
            }
            if(password.isEmpty()&& fullname.isEmpty()){
                Toast.makeText(
                    this,
                    "you have not inputted any details",
                    Toast.LENGTH_SHORT
                ).show()
            }

            }
        }
    }
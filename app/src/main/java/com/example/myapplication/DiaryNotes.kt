package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.example.myapplication.Models.User
import com.example.myapplication.Models.diaryNotes
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date

class DiaryNotes : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_notes)

        setupNavigator(R.id.profile_diaryNote,diaryNotesList::class.java)
        setupNavigator(R.id.settings_diaryNote,settingsPage::class.java)
        setupNavigator(R.id.map_diaryNote,MapsActivity::class.java)
        setupNavigator(R.id.recrdobservation_diaryNote,createObservation::class.java)
        setupNavigator(R.id.observationList_diaryNote,ObservationListPage::class.java)

        var add_button=findViewById<Button>(R.id.add_diaryNotes)
        var cancel_button=findViewById<Button>(R.id.cancel_diaryNotes)

        cancel_button.setOnClickListener {
            val intent = Intent(this, diaryNotesList::class.java)
            startActivity(intent)
        }
        add_button.setOnClickListener {
            var subject= findViewById<EditText>(R.id.subject_diaryNotes).text.toString()
            var location= findViewById<EditText>(R.id.location_diaryNotes).text.toString()
            var description= findViewById<EditText>(R.id.description_diaryNotes).text.toString()

            if(verification(subject,location,description)){

                var diaryNotes= diaryNotes(subject,location,getCurrentDateTime(),description)
                try {

                    val database = FirebaseDatabase.getInstance()

                    // Create a DatabaseReference to the user's data
                    val databaseReference: DatabaseReference =
                        database.getReference("Users").child(User.staticUser.getUid())
                            .child("diary notes").child("diaryNote ${getCurrentDateTime()}")

                    // Save the user's data to the database
                    databaseReference.setValue(diaryNotes.creatediaryNotes())
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                                val intent = Intent(this, diaryNotesList::class.java)
                                startActivity(intent)
                            }
                            else
                            {
                                // Error saving data to the database
                                val dbException = dbTask.exception
                                // Handle the error (e.g., show a message to the user)
                            }
                        }
                }catch (e: Exception){
                    Toast.makeText(this, "take a picture", Toast.LENGTH_SHORT).show()

                }
            }else{
                Toast.makeText(this, "take a picture", Toast.LENGTH_SHORT).show()
            }
        }

    }




    fun  verification(subject:String,location:String,description:String): Boolean {

        if(location.isEmpty()){

            findViewById<EditText>(R.id.subject_diaryNotes).error=" please add a subject"
            return false
        }
        if(location.isEmpty()){
            findViewById<EditText>(R.id.location_diaryNotes).error=" please add a location"
            return false
        }
        if(description.isEmpty()){
            findViewById<EditText>(R.id.description_diaryNotes).error=" please add a description"
            return false
        }
        return true

    }
    fun getCurrentDateTime(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Define your desired date and time format
        return dateFormat.format(currentDate)
    }
    private fun setupNavigator(settings_Maps: Int, page: Class<*>) {
        val button = findViewById<LinearLayout>(settings_Maps)
        button.setOnClickListener {
            val intent = Intent(this, page)
            startActivity(intent)
        }
    }

}
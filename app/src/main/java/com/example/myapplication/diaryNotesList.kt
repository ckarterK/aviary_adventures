package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.User
import com.example.myapplication.Models.diaryNotes
import com.example.myapplication.RecyclerViewModels.DiaryNotesList
import com.example.myapplication.RecyclerViewModels.diaryNotesAdapter
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class diaryNotesList : AppCompatActivity() {

    private lateinit var newRecyclerView: RecyclerView
    private var newDiaryNotes =ArrayList<DiaryNotesList>()
    private var diaryNoteList = ArrayList<diaryNotes>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_notes_list)

        setupNavigator(R.id.profile_diaryNotesList, diaryNotesList::class.java)
        setupNavigator(R.id.settings_diaryNotesList ,settingsPage::class.java)
        setupNavigator(R.id.map_diaryNotesList,MapsActivity::class.java)
        setupNavigator(R.id.recrdobservation_diaryNotesList,createObservation::class.java)
        setupNavigator(R.id.observationList_diaryNotesList,ObservationListPage::class.java)

        newRecyclerView= findViewById(R.id.diaryNotes_RecyclerView)
        newRecyclerView.layoutManager= LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)

        val databaseRef = FirebaseDatabase.getInstance().reference
        val uid = User.staticUser?.getUid().toString()
        val categoryPath = "Users/$uid/diary notes"

        var newDiaryNote= findViewById<Button>(R.id.addDiaryNote)
        databaseRef.child(categoryPath).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    // Loop through the child nodes (each represents an observation)
                    val diarySubject = childSnapshot.child("subject").value.toString()
                    val diaryLocation = childSnapshot.child("location").value.toString()
                    val diaryDescription = childSnapshot.child("description").value.toString()
                    var diaryDate = childSnapshot.child("date").value.toString()

                    val dateFormat = SimpleDateFormat("yyyy-MM-dd") // Define your desired date format

                    val Date: Date = dateFormat.parse(diaryDate)

                    val formattedDate: String = dateFormat.format(Date)

                   var diaryNotes=diaryNotes(diarySubject,diaryLocation,formattedDate,diaryDescription)

                    diaryNoteList.add(diaryNotes)

                }
                getUserData()
                // Now you have a list of observations to work with
                // You can process or display them as needed
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur while retrieving data
                // For example, you can log an error message
            }
        })

        newDiaryNote.setOnClickListener {
            val intent = Intent(this, DiaryNotes::class.java)
            startActivity(intent)
        }
    }
    private fun getUserData(){
        for(item in diaryNoteList){
            val diaryNote= DiaryNotesList(
                "Date:${item.getdate()}",
                "Subject:${item.getSubject()}",
                "Location:${item.getlocation()}",
                "Description:\n ${item.getdescription()}")
            newDiaryNotes.add(diaryNote)
        }
        newRecyclerView.adapter= diaryNotesAdapter(newDiaryNotes)
    }

    private fun setupNavigator(settings_Maps: Int, page: Class<*>) {
        val button = findViewById<LinearLayout>(settings_Maps)
        button.setOnClickListener {
            val intent = Intent(this, page)
            startActivity(intent)
        }
    }

}
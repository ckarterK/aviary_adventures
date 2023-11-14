package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.User
import com.example.myapplication.Models.diaryNotes
import com.example.myapplication.Observations_RecyclerView.Myadapter
import com.example.myapplication.diaryNotes_RecyclerView.DiaryNotesList
import com.example.myapplication.diaryNotes_RecyclerView.diaryNotesAdapter
import com.example.myapplication.diaryNotes_RecyclerView.noDiaryNotesAdapter
import com.example.myapplication.diaryNotes_RecyclerView.noDiaryNotesList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class diaryNotesList : AppCompatActivity() {

    private lateinit var newRecyclerView: RecyclerView
    private var newDiaryNotes =ArrayList<DiaryNotesList>()
    private var noDiaryNotes =ArrayList<noDiaryNotesList>()
    private var diaryNoteList = ArrayList<diaryNotes>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diary_notes_list)

        setupNavigator(R.id.profile_diaryNotesList, diaryNotesList::class.java)
        setupNavigator(R.id.settings_diaryNotesList ,settingsPage::class.java)
        setupNavigator(R.id.map_diaryNotesList,MapsActivity::class.java)
        setupNavigator(R.id.recrdobservation_diaryNotesList,createObservation::class.java)
        setupNavigator(R.id.observationList_diaryNotesList,ObservationListPage::class.java)
        var newDiaryNote= findViewById<Button>(R.id.addDiaryNote)

        populateRecyclerView()

        var filterBtn= findViewById<Button>(R.id.filterBy_DiaryNotes)

        filterBtn.setOnClickListener {
            val startDateEditText = findViewById<EditText>(R.id.startdate)
            val endDateEditText = findViewById<EditText>(R.id.enddate)

            val startDate = startDateEditText.text.toString()
            val endDate = endDateEditText.text.toString()

            if (!startDate.isEmpty() && !endDate.isEmpty()) {
                // Check if both start and end dates are valid
                if (isValidDate(startDate) && isValidDate(endDate)) {
                    populateRecyclerView(startDate, endDate)
                } else {
                    // Notify the user that the dates are not in the correct format
                    startDateEditText.error = "Invalid date format yyyy-mm-dd"
                    endDateEditText.error = "Invalid date format yyyy-mm-dd"
                }
            } else if (startDate.isEmpty()) {
                startDateEditText.error = "Select a date"
            } else if (endDate.isEmpty()) {
                endDateEditText.error = "Select a date"
            } else {
                // Handle other cases if needed
            }
        }

        newDiaryNote.setOnClickListener {
            val intent = Intent(this, DiaryNotes::class.java)
            startActivity(intent)
        }
    }
    fun isValidDate(dateStr: String, format: String = "yyyy-MM-dd"): Boolean {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        sdf.isLenient = false

        return try {
            // Try to parse the date string
            sdf.parse(dateStr)
            true
        } catch (e: Exception) {
            // If an exception occurs, the date is not in the correct format
            false
        }
    }

    private fun populateRecyclerView(startDate:String,endDate:String){
        newRecyclerView= findViewById(R.id.diaryNotes_RecyclerView)
        newRecyclerView.layoutManager= LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)

        val databaseRef = FirebaseDatabase.getInstance().reference
        val uid = User.staticUser?.getUid().toString()
        val categoryPath = "Users/$uid/diary notes"


        databaseRef.child(categoryPath).addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                diaryNoteList.clear()

                if (dataSnapshot.exists())
                {
                    for (childSnapshot in dataSnapshot.children)
                    {
                        // Loop through the child nodes (each represents an observation)
                        val diarySubject = childSnapshot.child("subject").value.toString()
                        val diaryLocation = childSnapshot.child("location").value.toString()
                        val diaryDescription = childSnapshot.child("description").value.toString()
                        var diaryDate = childSnapshot.child("date").value.toString()

                        val dateFormat =
                            SimpleDateFormat("yyyy-MM-dd") // Define your desired date format

                        val Date: Date = dateFormat.parse(diaryDate)
                        val filterStart: Date = dateFormat.parse(startDate)
                        val filterEnd: Date = dateFormat.parse(endDate)
                        val formattedDate: String = dateFormat.format(Date)

                        if(Date in filterStart..filterEnd) {
                            var diaryNotes =
                                diaryNotes(
                                    diarySubject,
                                    diaryLocation,
                                    formattedDate,
                                    diaryDescription
                                )

                            diaryNoteList.add(diaryNotes)
                        }

                    }

                    if(diaryNoteList.isEmpty()){
                        Toast.makeText(
                            this@diaryNotesList,
                            "no results found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }else{
                        getUserData()
                    }

                    // Now you have a list of observations to work with
                    // You can process or display them as needed
                }else{
                    emptyRecyclerView()
                }
            }
            override fun onCancelled(databaseError: DatabaseError)
            {

            }
        })
    }
    private fun populateRecyclerView(){
        newRecyclerView= findViewById(R.id.diaryNotes_RecyclerView)
        newRecyclerView.layoutManager= LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)

        val databaseRef = FirebaseDatabase.getInstance().reference
        val uid = User.staticUser?.getUid().toString()
        val categoryPath = "Users/$uid/diary notes"


        databaseRef.child(categoryPath).addListenerForSingleValueEvent(object : ValueEventListener
        {
            override fun onDataChange(dataSnapshot: DataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    for (childSnapshot in dataSnapshot.children)
                    {
                        // Loop through the child nodes (each represents an observation)
                        val diarySubject = childSnapshot.child("subject").value.toString()
                        val diaryLocation = childSnapshot.child("location").value.toString()
                        val diaryDescription = childSnapshot.child("description").value.toString()
                        var diaryDate = childSnapshot.child("date").value.toString()

                        val dateFormat =
                            SimpleDateFormat("yyyy-MM-dd") // Define your desired date format

                        val Date: Date = dateFormat.parse(diaryDate)

                        val formattedDate: String = dateFormat.format(Date)

                        var diaryNotes =
                            diaryNotes(diarySubject, diaryLocation, formattedDate, diaryDescription)

                        diaryNoteList.add(diaryNotes)

                    }
                    getUserData()
                    // Now you have a list of observations to work with
                    // You can process or display them as needed
                }else{
                    emptyRecyclerView()
                }
            }
            override fun onCancelled(databaseError: DatabaseError)
            {

            }
        })
    }
    private fun getUserData(){
        newDiaryNotes.clear()
        for(item in diaryNoteList){
            val diaryNote= DiaryNotesList(
                "${item.getdate()}",
                "Subject:\n${item.getSubject()}",
                "Location:\n${item.getlocation()}",
                "Description:\n ${item.getdescription()}")
            newDiaryNotes.add(diaryNote)
        }
        updateRecyclerView()
    }

    private fun emptyRecyclerView(){
        newDiaryNotes.clear()
       var noDiaryNote =noDiaryNotesList("you dont have any diary notes,add a diary note")
        noDiaryNotes.add(noDiaryNote)
        newRecyclerView.adapter=noDiaryNotesAdapter(noDiaryNotes)
        }

    private fun updateRecyclerView() {
        // Create or update the adapter with the new data
        val adapter = diaryNotesAdapter(newDiaryNotes)
        newRecyclerView.adapter = adapter

        // Notify the adapter that the data set has changed
        adapter.notifyDataSetChanged()
    }



    private fun setupNavigator(settings_Maps: Int, page: Class<*>) {
        val button = findViewById<LinearLayout>(settings_Maps)
        button.setOnClickListener {
            val intent = Intent(this, page)
            startActivity(intent)
        }
    }

}
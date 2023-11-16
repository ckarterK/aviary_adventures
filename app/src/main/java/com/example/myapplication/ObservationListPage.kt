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
import com.example.myapplication.Models.Observation
import com.example.myapplication.Models.User
import com.example.myapplication.Observations_RecyclerView.Myadapter
import com.example.myapplication.Observations_RecyclerView.observationList
import com.example.myapplication.diaryNotes_RecyclerView.noDiaryNotesAdapter
import com.example.myapplication.diaryNotes_RecyclerView.noDiaryNotesList
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ObservationListPage : AppCompatActivity() {

    private lateinit var newRecyclerView:RecyclerView
    private var newObservationList =ArrayList<observationList>()
    private lateinit var dateList:ArrayList<String>
    private var observationsList = ArrayList<Observation>()
    private var noDiaryNotes =ArrayList<noDiaryNotesList>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation_list_page)


        var filterBtn= findViewById<Button>(R.id.filterBy_DiaryNotes)

        filterBtn.setOnClickListener {
            val startDateEditText = findViewById<EditText>(R.id.startdate)
            val endDateEditText = findViewById<EditText>(R.id.endDate)

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

        setupNavigator(R.id.profile_ObservationList,diaryNotesList::class.java)
        setupNavigator(R.id.settings_ObservationList,settingsPage::class.java)
        setupNavigator(R.id.map_ObservationList,MapsActivity::class.java)
        setupNavigator(R.id.recrdobservation_ObservationList,createObservation::class.java)
        setupNavigator(R.id.observationList_ObservationList,ObservationListPage::class.java)
        populateRecyclerView()
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

        newRecyclerView= findViewById(R.id.ObservationList_RecyclerView)
        newRecyclerView.layoutManager=LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)

        val databaseRef = FirebaseDatabase.getInstance().reference
        val uid = User.staticUser?.getUid().toString()
        val categoryPath = "Users/$uid/observed List"

        //adapted from firebase
//    authour:firebase
//    link:https://firebase.google.com/docs/database/admin/retrieve-data
//    date:2023-11-15
        databaseRef.child(categoryPath).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                observationsList.clear() // Clear the list before populating it again
                val dateFormat =
                    SimpleDateFormat("yyyy-MM-dd") // Define your desired date format

                val filterStart: Date = dateFormat.parse(startDate)
                val filterEnd: Date = dateFormat.parse(endDate)


                for (childSnapshot in dataSnapshot.children) {

                    // Loop through the child nodes (each represents an observation)
                    val birdSpecies = childSnapshot.child("birdSpecies").value.toString()
                    val description = childSnapshot.child("description").value.toString()
                    val quantity = childSnapshot.child("quantity").getValue(Int::class.java)
                    val pictureID = childSnapshot.child("pictureID").value.toString()
                    var date = childSnapshot.child("date").value.toString()
                    var observationDate: Date = dateFormat.parse(date)

                    if (observationDate in filterStart..filterEnd) {
                        val observation = Observation()
                        observation.setBirdSpecies(birdSpecies)
                        observation.setDescription(description)
                        observation.setObservationDate(date)
                        observation.setQuantity(quantity ?: 0)
                        observation.setPictureID(pictureID)

                        observationsList.add(observation)
                    }
                }
                if(observationsList.isEmpty()){
                    Toast.makeText(
                        this@ObservationListPage,
                        "no results found",
                        Toast.LENGTH_SHORT
                    ).show()
                }else{
                    getUserData()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur while retrieving data
                // For example, you can log an error message
            }
        })


    }
    private fun populateRecyclerView() {
        newRecyclerView = findViewById(R.id.ObservationList_RecyclerView)
        newRecyclerView.layoutManager = LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)

        val databaseRef = FirebaseDatabase.getInstance().reference
        val uid = User.staticUser?.getUid().toString()
        val categoryPath = "Users/$uid/observed List"

        //adapted from firebase
//    authour:firebase
//    link:https://firebase.google.com/docs/database/admin/retrieve-data
//    date:2023-11-15
        databaseRef.child(categoryPath).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    observationsList.clear() // Clear the list before populating it again
                    for (childSnapshot in dataSnapshot.children) {
                        // Loop through the child nodes (each represents an observation)
                        val birdSpecies = childSnapshot.child("birdSpecies").value.toString()
                        val description = childSnapshot.child("description").value.toString()
                        val quantity = childSnapshot.child("quantity").getValue(Int::class.java)
                        val pictureID = childSnapshot.child("pictureID").value.toString()
                        var date = childSnapshot.child("date").value.toString()
                        var lat= childSnapshot.child("latitude").value.toString().toDouble()
                        var long=childSnapshot.child("longitude").value.toString().toDouble()
                        var latlng= LatLng(lat,long)
                        val dateFormat =
                            SimpleDateFormat("yyyy-MM-dd") // Define your desired date format

                        val Date: Date = dateFormat.parse(date)

                        val formattedDate: String = dateFormat.format(Date)

                        val observation = Observation()
                        observation.setBirdSpecies(birdSpecies)
                        observation.setDescription(description)
                        observation.setObservationDate(formattedDate)
                        observation.setQuantity(quantity ?: 0)
                        observation.setPictureID(pictureID)
                        observation.setLocation(latlng.toString())

                        observationsList.add(observation)
                    }
                    getUserData()
                } else {
                    emptyRecyclerView()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur while retrieving data
                // For example, you can log an error message
            }


        })
    }

    private fun getUserData() {
        newObservationList.clear() // Clear the list before populating it again
        for (item in observationsList) {
            val observation = observationList(
                observationPicture="${item.getPictureID()}",
                observationLocation="observation location:\n${item.getLocations()}",
                observedBirds = "observed birds:\n${item.getQuantity().toString()}",
                observedSpecies = "observed species:\n ${item.getBirdSpecies()}",
                date = "${item.getObservationDate()}"
            )
            newObservationList.add(observation)
        }
        updateRecyclerView()
    }
    private fun emptyRecyclerView(){
        newObservationList.clear()
        var noDiaryNote = noDiaryNotesList("you don't have any observations,add a observation")
        noDiaryNotes.add(noDiaryNote)
        newRecyclerView.adapter= noDiaryNotesAdapter(noDiaryNotes)
    }

    private fun updateRecyclerView() {
        // Create or update the adapter with the new data
        val adapter = Myadapter(newObservationList)
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
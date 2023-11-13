package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.Observation
import com.example.myapplication.Models.User
import com.example.myapplication.Observations_RecyclerView.Myadapter
import com.example.myapplication.Observations_RecyclerView.observationList
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date


class ObservationListPage : AppCompatActivity() {

    private lateinit var newRecyclerView:RecyclerView
    private var newObservationList =ArrayList<observationList>()
    private lateinit var dateList:ArrayList<String>
    private var observationsList = ArrayList<Observation>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation_list_page)


        var filterBtn= findViewById<Button>(R.id.filterBy_obervationListPage)

        filterBtn.setOnClickListener {
            var startDate=findViewById<EditText>(R.id.startdate).text.toString()
            var endDate=findViewById<EditText>(R.id.endDate).text.toString()

            if(!startDate.isEmpty() && !endDate.isEmpty()){

                populateRecyclerView(startDate, endDate)

            }else if(startDate.isEmpty()){

                findViewById<EditText>(R.id.startdate).error="select a date"

            }else if(endDate.isEmpty()){

                findViewById<EditText>(R.id.endDate).error="select a date"
            }else{

            }

        }

        setupNavigator(R.id.profile_ObservationList,diaryNotesList::class.java)
        setupNavigator(R.id.settings_ObservationList,settingsPage::class.java)
        setupNavigator(R.id.map_ObservationList,MapsActivity::class.java)
        setupNavigator(R.id.recrdobservation_ObservationList,createObservation::class.java)
        setupNavigator(R.id.observationList_ObservationList,ObservationListPage::class.java)
        populateRecyclerView()
    }
    private fun populateRecyclerView(startDate:String,endDate:String){

        newRecyclerView= findViewById(R.id.ObservationList_RecyclerView)
        newRecyclerView.layoutManager=LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)

        val databaseRef = FirebaseDatabase.getInstance().reference
        val uid = User.staticUser?.getUid().toString()
        val categoryPath = "Users/$uid/observed List"

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
                getUserData()
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

        databaseRef.child(categoryPath).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                observationsList.clear() // Clear the list before populating it again
                for (childSnapshot in dataSnapshot.children) {
                    // Loop through the child nodes (each represents an observation)
                    val birdSpecies = childSnapshot.child("birdSpecies").value.toString()
                    val description = childSnapshot.child("description").value.toString()
                    val quantity = childSnapshot.child("quantity").getValue(Int::class.java)
                    val pictureID = childSnapshot.child("pictureID").value.toString()
                    var date = childSnapshot.child("date").value.toString()
                    val observation = Observation()
                    observation.setBirdSpecies(birdSpecies)
                    observation.setDescription(description)
                    observation.setObservationDate(date)
                    observation.setQuantity(quantity ?: 0)
                    observation.setPictureID(pictureID)

                    observationsList.add(observation)
                }
                getUserData()
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
                observedBirds = "observed birds: ${item.getQuantity().toString()}",
                observedSpecies = "observed species: ${item.getBirdSpecies()}",
                date = "${item.getObservationDate()}"
            )
            newObservationList.add(observation)
        }
        updateRecyclerView()
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
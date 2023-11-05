package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.Models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class ObservationListPage : AppCompatActivity() {

    private lateinit var newRecyclerView:RecyclerView
    private var newObservationList =ArrayList<observationList>()
    private lateinit var dateList:ArrayList<String>
    private lateinit var obseredSpeciesList:ArrayList<String>
    private lateinit var observerBirdsList:ArrayList<String>
    private var observationsList = ArrayList<Observation>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observation_list_page)


        setupNavigator(R.id.profile_ObservationList,settingsPage::class.java)
        setupNavigator(R.id.settings_ObservationList,settingsPage::class.java)
        setupNavigator(R.id.map_ObservationList,MapsActivity::class.java)
        setupNavigator(R.id.recrdobservation_ObservationList,createObservation::class.java)
        setupNavigator(R.id.observationList_ObservationList,ObservationListPage::class.java)

        newRecyclerView= findViewById(R.id.ObservationList_RecyclerView)
        newRecyclerView.layoutManager=LinearLayoutManager(this)
        newRecyclerView.setHasFixedSize(true)

        val databaseRef = FirebaseDatabase.getInstance().reference
        val uid = User.staticUser?.getUid().toString()
        val categoryPath = "Users/$uid/observed List"

        databaseRef.child(categoryPath).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (childSnapshot in dataSnapshot.children) {
                    // Loop through the child nodes (each represents an observation)
                    val birdSpecies = childSnapshot.child("birdSpecies").value.toString()
                    val description = childSnapshot.child("description").value.toString()
                    val quantity = childSnapshot.child("quantity").getValue(Int::class.java)
                    val pictureID = childSnapshot.child("pictureID").value.toString()

                    // Create an Observation object and add it to the list
                    val observation = Observation()
                    observation.setBirdSpecies(birdSpecies)
                    observation.setDescription(description)
                    observation.setQuantity(quantity ?: 0) // Use a default value if quantity is null
                    observation.setPictureID(pictureID)

                    observationsList.add(observation)
                    Log.d("datasnapshit","${birdSpecies}")
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


    }
    private fun getUserData(){
        for(item in observationsList){
            val observation=observationList(observedBirds = "observed birds: ${item.getQuantity().toString()}", observedSpecies = "observed species: ${item.getBirdSpecies()}", date = "22 March 2023")
            newObservationList.add(observation)
        }
        newRecyclerView.adapter=Myadapter(newObservationList)
    }

    private fun setupNavigator(settings_Maps: Int, page: Class<*>) {
        val button = findViewById<LinearLayout>(settings_Maps)
        button.setOnClickListener {
            val intent = Intent(this, page)
            startActivity(intent)
        }
    }
}
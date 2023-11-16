package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.myapplication.Models.User
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class settingsPage : AppCompatActivity() {
    val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_page)

        var name= findViewById<TextView>(R.id.settings_name)
        name.text=User.staticUser.getName()

        var email=findViewById<TextView>(R.id.settings_Email)
        email.text=User.staticUser.getEmail()

        hasMetricUnitPreference()
        hasRadiusPreference()

        var editProfileBtn=findViewById<Button>(R.id.settings_EditProfile)

        // Find and set up buttons and their click listeners
        setupMeasurementBtn(R.id.settings_kilometersBtn, User.Measurement.KILOMETERS)
        setupMeasurementBtn(R.id.settings_milesBtn, User.Measurement.MILES)
        setupRadiusBtn(R.id.Radius5_Settings,User.Radius.RADIUS_5)
        setupRadiusBtn(R.id.Radius10_Settings,User.Radius.RADIUS_10)
        setupRadiusBtn(R.id.Radius15_Settings,User.Radius.RADIUS_15)
        setupRadiusBtn(R.id.Radius30_Settings,User.Radius.RADIUS_30)

        // Set up navigation buttons
        setupNavigator(R.id.profile_Settings,diaryNotesList::class.java)
        setupNavigator(R.id.settings_Settings,settingsPage::class.java)
        setupNavigator(R.id.map_Settings,MapsActivity::class.java)
        setupNavigator(R.id.recordObservation_Settings,createObservation::class.java)
        setupNavigator(R.id.observationList_Settings,ObservationListPage::class.java)

        editProfileBtn.setOnClickListener {

            val intent = Intent(this, updateProfilePage::class.java)
            startActivity(intent)
        }


    }
    // Check and set metric unit preferences
    private fun hasMetricUnitPreference(){

        if(User.staticUser.getpreferedMeasurement().isEmpty()) {
            setupMeasurementBtn(R.id.settings_kilometersBtn, User.Measurement.KILOMETERS)
            setupMeasurementBtn(R.id.settings_milesBtn, User.Measurement.MILES)

        }
        else{

            if (User.staticUser.getpreferedMeasurement() == User.Measurement.KILOMETERS){
                chosenButtonColor(R.id.settings_kilometersBtn)


            }
            else if (User.staticUser.getpreferedMeasurement() == User.Measurement.MILES){
               chosenButtonColor(R.id.settings_milesBtn)
            }

        }
    }
    // Check and set radius preferences
    private fun hasRadiusPreference(){
        if(User.staticUser.getPreferedRadius()==0){
            setupRadiusBtn(R.id.Radius5_Settings, User.Radius.RADIUS_5)
            setupRadiusBtn(R.id.Radius10_Settings, User.Radius.RADIUS_10)
            setupRadiusBtn(R.id.Radius15_Settings, User.Radius.RADIUS_15)
            setupRadiusBtn(R.id.Radius30_Settings, User.Radius.RADIUS_30)
        }else{
            // Set the chosen radius button color
            if (User.staticUser.getPreferedRadius() == User.Radius.RADIUS_5){
               chosenButtonColor(R.id.Radius5_Settings)
            }

            if (User.staticUser.getPreferedRadius() == User.Radius.RADIUS_10){
                chosenButtonColor(R.id.Radius10_Settings)
            }

            if (User.staticUser.getPreferedRadius() == User.Radius.RADIUS_15){
                chosenButtonColor(R.id.Radius15_Settings)
            }

            if (User.staticUser.getPreferedRadius() == User.Radius.RADIUS_30){
                chosenButtonColor(R.id.Radius30_Settings)
            }

        }

    }
    // Set up click listener for metric unit buttons
    private fun setupMeasurementBtn(buttonId: Int, measurement: String) {
        val button = findViewById<Button>(buttonId)
        button.setOnClickListener {
            User.staticUser.setpreferedMeasurement(measurement)
            // Create a DatabaseReference to the user's data
            val databaseReference: DatabaseReference = database.getReference("Users").child(User.staticUser.getUid())

            // Save the user's data to the database
            databaseReference.updateChildren(User.staticUser.preferedMeasurementtoMap()).addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    if(measurement==User.Measurement.KILOMETERS){
                       chosenButtonColor(R.id.settings_kilometersBtn)
                        notChosenButtonColor(R.id.settings_milesBtn)
                    }else{
                        chosenButtonColor(R.id.settings_milesBtn)
                        notChosenButtonColor(R.id.settings_kilometersBtn)
                    }
                    Toast.makeText(
                        this@settingsPage,
                        "metrics has been changed",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {

                }
            }
        }
    }

    private fun setupRadiusBtn(buttonId: Int, radius: Int) {
        val button = findViewById<Button>(buttonId)
        button.setOnClickListener {
            User.staticUser.setPreferedRadius(radius)

            // Create a DatabaseReference to the user's data
            val databaseReference: DatabaseReference = database.getReference("Users").child(User.staticUser.getUid())

            // Save the user's data to the database
            databaseReference.updateChildren(User.staticUser.PreferedRadiustoMap()).addOnCompleteListener { dbTask ->
                if (dbTask.isSuccessful) {
                    if(radius==User.Radius.RADIUS_5){
                        chosenButtonColor(R.id.Radius5_Settings)
                       notChosenButtonColor(R.id.Radius10_Settings,R.id.Radius15_Settings,R.id.Radius30_Settings)
                    }
                    if(radius==User.Radius.RADIUS_10){
                        chosenButtonColor(R.id.Radius10_Settings)
                        notChosenButtonColor(R.id.Radius5_Settings,R.id.Radius15_Settings,R.id.Radius30_Settings)
                    }
                    if(radius==User.Radius.RADIUS_15){
                        chosenButtonColor(R.id.Radius15_Settings)
                        notChosenButtonColor(R.id.Radius5_Settings,R.id.Radius10_Settings,R.id.Radius30_Settings)
                    }
                    if(radius==User.Radius.RADIUS_30){
                        chosenButtonColor(R.id.Radius30_Settings)
                        notChosenButtonColor(R.id.Radius5_Settings,R.id.Radius10_Settings,R.id.Radius15_Settings)
                    }

                    Toast.makeText(
                        this@settingsPage,
                        "radius has been changed",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Error saving data to the database
                    val dbException = dbTask.exception
                    // Handle the error (e.g., show a message to the user)
                }
            }
        }
    }

    private fun setupNavigator(settings_Maps: Int, page: Class<*>) {
        val button = findViewById<LinearLayout>(settings_Maps)
        button.setOnClickListener {

            val intent = Intent(this, page)
            startActivity(intent)

        }
    }

    private fun chosenButtonColor(Button: Int){
        findViewById<Button>(Button).backgroundTintList = ContextCompat.getColorStateList(this, R.color.white)
    }
    private fun notChosenButtonColor(Button: Int){
        findViewById<Button>(Button).backgroundTintList = ContextCompat.getColorStateList(this,R.color.grey)
    }
    private fun notChosenButtonColor(Button1: Int,Button2: Int,Button3: Int){
        findViewById<Button>(Button1).backgroundTintList = ContextCompat.getColorStateList(this, R.color.grey)
        findViewById<Button>(Button2).backgroundTintList = ContextCompat.getColorStateList(this,R.color.grey)
        findViewById<Button>(Button3).backgroundTintList = ContextCompat.getColorStateList(this,R.color.grey)

    }
}
package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.myapplication.Models.Observation
import com.example.myapplication.Models.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class createObservation : AppCompatActivity() {

    private var counter:Int = 0
    private var PhotoUrl:String=""
    private lateinit var imageBitmap: Bitmap
    private  val CAMERA_REQUEST_CODE = 1001
    private lateinit var birdSpeciesET:Spinner
    private lateinit var quantityET:TextView
    private lateinit var birdDescriptionET:EditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_observation)

        birdSpeciesET= findViewById<Spinner>(R.id.createObservation_BirdDropDown)


        val spinner_items = mutableListOf(
            "select a bird species ","American Robin", "Bald Eagle", "Blue Jay", "Cardinal", "Chickadee",
            "European Starling", "House Sparrow", "Mallard Duck", "Northern Mockingbird", "Peregrine Falcon",
            "Red-tailed Hawk", "American Goldfinch", "Ruby-throated Hummingbird", "Swan", "Woodpecker",
            "Yellow Warbler", "Osprey", "Cedar Waxwing", "Sandhill Crane", "Northern Harrier"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            spinner_items
        )

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        birdSpeciesET.adapter = adapter


        quantityET=findViewById<TextView>(R.id.createObservation_Qantity)
        birdDescriptionET= findViewById<EditText>(R.id.createObservation_Description)
        var addButton= findViewById<ImageView>(R.id.createObservation_addQuantity)
        var minusButton= findViewById<ImageView>(R.id.createObservation_minusQuantity)
        var addSpeciesButton=findViewById<Button>(R.id.createObservation_addSpecies)
        var recordButton=findViewById<Button>(R.id.createObservation_Record)
        val takePictureButton = findViewById<Button>(R.id.ButtonPicture)


        setupNavigator(R.id.profile_CreateObservation,diaryNotesList::class.java)
        setupNavigator(R.id.settings_CreateObservation ,settingsPage::class.java)
        setupNavigator(R.id.map_CreateObservation,MapsActivity::class.java)
        setupNavigator(R.id.recrdobservation_CreateObservation,createObservation::class.java)
        setupNavigator(R.id.observationList_CreateObservation,ObservationListPage::class.java)

        // Initialize fusedLocationClient for obtaining device location
        fusedLocationClient= LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    User.staticUser.setLocation(it.latitude, it.longitude)
                }
            }
        }

        // Function to set up quantity buttons and click listeners
        addButton.setOnClickListener {

            counter++
            var Quantity=findViewById<TextView>(R.id.createObservation_Qantity)
            Quantity.text
            Quantity.text="${counter}"


        }
        // Function to set up quantity buttons and click listeners
        minusButton.setOnClickListener {

            if(counter>0){

                counter--
                var Quantity=findViewById<TextView>(R.id.createObservation_Qantity)
                Quantity.text
                Quantity.text="${counter}"

                if(counter==0){
                    var Quantity=findViewById<TextView>(R.id.createObservation_Qantity)
                    Quantity.text
                    Quantity.text="Quantity"
                }
            }
            else{
                Toast.makeText(this, "add a number", Toast.LENGTH_SHORT).show()
            }
        }

        addSpeciesButton.setOnClickListener {


            var observation = Observation()

            if(validateFields(observation)) {
            try {


                uploadImageToFirebaseStorage(imageBitmap)
                User.staticUser.addObservation(observation)
                val database = FirebaseDatabase.getInstance()

                //adapted from firebase
//    authour:firebase
//    link:https://firebase.google.com/docs/database/admin/save-data
//    date:2023-11-15
                // Create a DatabaseReference to the user's data
                val databaseReference: DatabaseReference =
                    database.getReference("Users").child(User.staticUser.getUid())
                        .child("observed List").child("observation ${getCurrentDateTime()}")

                // Save the user's data to the database
                databaseReference.setValue(observation.toMap())
                    .addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            Toast.makeText(this, "observation recorded", Toast.LENGTH_SHORT).show()
                            counter=0
                            birdDescriptionET.setText("")
                            quantityET.setText("Quantity")
                            birdSpeciesET.setSelection(0)
                            takePictureButton.isEnabled=true
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
            }

        }

        takePictureButton.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            takePictureButton.isEnabled=false
        }


        recordButton.setOnClickListener {


            var observation = Observation()

            if(validateFields(observation)) {
                try {

                User.staticUser.addObservation(observation)
                val database = FirebaseDatabase.getInstance()


                // Create a DatabaseReference to the user's data
                val databaseReference: DatabaseReference =
                    database.getReference("Users").child(User.staticUser.getUid())
                        .child("observed List").child("observation ${observation.getObservationDate()}")
                    //adapted from firebase
//    authour:firebase
//    link:https://firebase.google.com/docs/database/admin/save-data
//    date:2023-11-15
                databaseReference.setValue(observation.toMap())
                    .addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
                            val intent = Intent(this, createObservation::class.java)
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
            }

        }
    }
    fun getCurrentDateTime(): String {
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Define your desired date and time format
        return dateFormat.format(currentDate)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.extras?.get("data")?.let { capturedImage ->
                imageBitmap = capturedImage as Bitmap
                // Save the image in memory or upload it to Firebase Storage here
            }
        }
    }
    // Function to upload image to Firebase Storage
    fun uploadImageToFirebaseStorage(imageBitmap: Bitmap) {
        val uid = User.staticUser?.getUid()
        PhotoUrl="${uid}_${getCurrentDateTime()}.jpg"
        uid?.let {
            val storageReference = FirebaseStorage.getInstance().reference.child("images").child(PhotoUrl)
            val baos = ByteArrayOutputStream()
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val imageData = baos.toByteArray()

            val uploadTask = storageReference.putBytes(imageData)
            uploadTask.addOnSuccessListener {
                // Image upload successful
                Toast.makeText(
                    this@createObservation,
                    "Image uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                Toast.makeText(
                    this@createObservation,
                    "Image not uploaded successfully",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateFields(observation: Observation): Boolean {

        try{
            uploadImageToFirebaseStorage(imageBitmap)
            val spinnerIndex = birdSpeciesET.selectedItemPosition
            var birdSpecies= birdSpeciesET.selectedItem.toString().trim()
            val birdDescription = birdDescriptionET.text.toString().trim()


            var isValid = true

            if (spinnerIndex==0) {
                isValid = false
                Toast.makeText(
                    this@createObservation,
                    "select a bird species",
                    Toast.LENGTH_SHORT
                ).show()
            }

            if (counter==0) {
                quantityET.error = "Quantity (Required)"
                isValid = false
            }

            if (birdDescription.isEmpty()) {
                birdDescriptionET.error = "bird Description"
                isValid = false
            }
            if(isValid){

                observation.setBirdSpecies(birdSpecies)
                observation.setDescription(birdDescription)
                observation.setQuantity(counter)
                observation.setPictureID(PhotoUrl)
                observation.setObservationDate(getCurrentDateTime())

            }
            return isValid
        } catch (e: Exception) {
                Toast.makeText(
                    this@createObservation,
                    "please take a picture",
                    Toast.LENGTH_SHORT
                ).show()
            return false
        }

    }
    private fun setupNavigator(settings_Maps: Int, page: Class<*>) {
        val button = findViewById<LinearLayout>(settings_Maps)
        button.setOnClickListener {
            val intent = Intent(this, page)
            startActivity(intent)
        }
    }
}
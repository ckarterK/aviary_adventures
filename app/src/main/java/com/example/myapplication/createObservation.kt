package com.example.myapplication

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class createObservation : AppCompatActivity() {

    private var counter:Int = 0
    private var PhotoUrl:String=""
    private lateinit var imageBitmap: Bitmap
    private  val CAMERA_REQUEST_CODE = 1001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_observation)
        val spinner_items = mutableListOf<String>()

        spinner_items.add("akim")
        spinner_items.add("car")
        spinner_items.add("van")
        spinner_items.add("stan")

        val spinner = findViewById<Spinner>(R.id.createObservation_BirdDropDown)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            spinner_items
        )

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        spinner.adapter = adapter

        var addButton= findViewById<ImageView>(R.id.createObservation_addQuantity)
        var minusButton= findViewById<ImageView>(R.id.createObservation_minusQuantity)
        var addSpeciesButton=findViewById<Button>(R.id.createObservation_addSpecies)
        val takePictureButton = findViewById<Button>(R.id.ButtonPicture)

        addButton.setOnClickListener {

            counter++
            var Quantity=findViewById<TextView>(R.id.createObservation_Qantity)
            Quantity.text
            Quantity.text="${counter}"


        }


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

                uploadImageToFirebaseStorage(imageBitmap)
                User.staticUser.addObservation(observation)
                val database = FirebaseDatabase.getInstance()


                // Create a DatabaseReference to the user's data
                val databaseReference: DatabaseReference =
                    database.getReference("Users").child(User.staticUser.getUid())
                        .child("observed List").child("observation ${getCurrentDateTime()}")

                // Save the user's data to the database
                databaseReference.setValue(observation.toMap())
                    .addOnCompleteListener { dbTask ->
                        if (dbTask.isSuccessful) {
                            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()

                        }
                        else
                        {
                            // Error saving data to the database
                            val dbException = dbTask.exception
                            // Handle the error (e.g., show a message to the user)
                        }
                    }
            }

        }

        takePictureButton.setOnClickListener {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)

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

    private fun validateFields(observation:Observation): Boolean {
        var birdSpeciesET = findViewById<Spinner>(R.id.createObservation_BirdDropDown)
        var quantityET=findViewById<TextView>(R.id.createObservation_Qantity)
        var birdDescriptionET= findViewById<EditText>(R.id.createObservation_Description)


        val birdSpecies = birdSpeciesET.selectedItem.toString().toString().trim()
        val birdDescription = birdDescriptionET.text.toString().trim()


        var isValid = true

        if (birdSpecies.isEmpty()) {
            isValid = false
            Toast.makeText(
                this@createObservation,
                "select a bird",
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
        if(PhotoUrl.isEmpty()){
            Toast.makeText(
                this@createObservation,
                "Itake a picture of the bird",
                Toast.LENGTH_SHORT
            ).show()
            isValid= false

        }
        if(isValid){

            observation.setBirdSpecies(birdSpecies)
            observation.setDescription(birdDescription)
            observation.setQuantity(counter)
            observation.setPictureID(PhotoUrl)
        }
        return isValid
    }
}
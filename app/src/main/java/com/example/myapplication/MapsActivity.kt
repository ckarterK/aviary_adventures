package com.example.myapplication

import PolyLineData
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.Models.Observation
import com.example.myapplication.Models.User
import com.example.myapplication.Models.diaryNotes

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.GoogleMap.OnPolylineClickListener
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.PendingResult
import com.google.maps.internal.PolylineEncoding
import com.google.maps.model.DirectionsResult
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnPolylineClickListener{

    private var alreadyZoomed: Boolean=true
    private var observationsList = ArrayList<Observation>()
    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mGeoApiContext: GeoApiContext
    private var mPolyLinesData: ArrayList<PolyLineData> = ArrayList()
    private var mSelectedMarker: Marker? =null
    private lateinit var endlocation: LatLng
    private lateinit var locationUpdateHandler: Handler
    private val locationUpdateInterval = 5000L


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        setupNavigator(R.id.profile_Maps,diaryNotesList::class.java)
        setupNavigator(R.id.settings_Maps,settingsPage::class.java)
        setupNavigator(R.id.map_Maps,MapsActivity::class.java)
        setupNavigator(R.id.recrdobservation_Maps,createObservation::class.java)
        setupNavigator(R.id.observationList_Maps,ObservationListPage::class.java)
        val RadiusOptions = mutableListOf<String>()

        if (User.staticUser.getpreferedMeasurement()==User.Measurement.KILOMETERS){
            RadiusOptions.add("select a range in Kilometers")
            RadiusOptions.add("1")
            RadiusOptions.add("5")
            RadiusOptions.add("10")
            RadiusOptions.add("15")
            RadiusOptions.add("30")
        }else{
            RadiusOptions.add("select a range Miles")
            RadiusOptions.add("1")
            RadiusOptions.add("5")
            RadiusOptions.add("10")
            RadiusOptions.add("15")
            RadiusOptions.add("30")
        }


        val RadiuSpinner = findViewById<Spinner>(R.id.RadiuSpinner_Maps)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            RadiusOptions
        )

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // Apply the adapter to the spinner
        RadiuSpinner.adapter = adapter

        var filterByBtn= findViewById<Button>(R.id.FilterByBtn_Maps)
        filterByBtn.setOnClickListener {

            try {
                val radius = RadiuSpinner.selectedItem.toString().toInt()
                Log.d("spinner","${User.staticUser.getLocation().lat}  ${User.staticUser.getLocation().lng} ${radius}")

                fetchDataAndAddMarkers(radius)
            } catch (e: NumberFormatException) {
                Toast.makeText(this, "select a range", Toast.LENGTH_SHORT).show()
            }


        }



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)



    }
    private fun scheduleLocationUpdate() {
        locationUpdateHandler = Handler(Looper.getMainLooper())
        locationUpdateHandler.postDelayed({
            // Update the user's location
            showUserLocation()

            // Schedule the next location update
            scheduleLocationUpdate()
        }, locationUpdateInterval)
    }

    private fun stopLocationUpdates() {
        locationUpdateHandler.removeCallbacksAndMessages(null)
    }

    // ... (existing code)

    override fun onDestroy() {
        super.onDestroy()
        // Stop location updates when the activity is destroyed
        stopLocationUpdates()
    }

    /**
     * Manipulates the map once available.
     */
    override fun onMapReady(googleMap: GoogleMap) {


        mMap = googleMap
        mMap.setOnPolylineClickListener(this)

        var permissionGranted=false

        while(!permissionGranted) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mMap.isMyLocationEnabled = true
                permissionGranted=true
                // Show the user's location
                scheduleLocationUpdate()

            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
                permissionGranted=false
            }
        }
        mMap.setOnMarkerClickListener { marker ->

            marker.showInfoWindow()
            mSelectedMarker=marker
            calculateDirections(marker)
            true // Return true to consume the event
        }

    }



//adapted from youtube
//    authour:CodingWithMitch
//    link to Author:
//    link:https://www.youtube.com/watch?v=f47L1SL5S0o
//    date:20 Sept 2018

    private fun fetchDataAndAddMarkers(radius: Int) {
        val referenceLocation = Location("referenceLocation")
        val userLocation = User.staticUser.getLocation()

        if (userLocation != null) {
            referenceLocation.latitude = userLocation.lat
            referenceLocation.longitude = userLocation.lng

            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.ebird.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(EBirdApiService::class.java)

            var latlng=LatLng(User.staticUser.getLocation().lat,User.staticUser.getLocation().lng)
            var countryCode=getCountryCode(latlng.latitude,latlng.longitude)
            val retrofitData = retrofit.getRecentHotspots("${countryCode}")
            Log.d("countrycode"," apppl: ${countryCode}")
            retrofitData.enqueue(object : Callback<List<HotspotsItem>?> {
                override fun onResponse(
                    call: Call<List<HotspotsItem>?>,
                    response: Response<List<HotspotsItem>?>
                ) {
                    val hotspotList = response.body() ?: emptyList()

                    val markerOptionsList = ArrayList<MarkerOptions>()

                    for (hotspot in hotspotList) {
                        val location = Location("hotspotLocation")
                        location.latitude = hotspot.lat
                        location.longitude = hotspot.lng

                        if (User.staticUser.getpreferedMeasurement() == User.Measurement.KILOMETERS) {

                            val distanceInKilometers = referenceLocation.distanceTo(location) / 1000

                            if (distanceInKilometers <= radius) {
                                val hotspotLatLng = LatLng(hotspot.lat, hotspot.lng)
                                markerOptionsList.add(
                                    MarkerOptions().position(hotspotLatLng)
                                        .title("Bird Observation")
                                )
                            }
                        } else {
                            val metersToMilesConversionFactor = 0.000621371
                            val distanceInMiles =
                                referenceLocation.distanceTo(location) * metersToMilesConversionFactor

                            val rangeInMiles = radius.toDouble() // Assuming radius is in miles
                            if (distanceInMiles <= rangeInMiles) {
                                val hotspotLatLng = LatLng(hotspot.lat, hotspot.lng)
                                markerOptionsList.add(
                                    MarkerOptions().position(hotspotLatLng)
                                        .title("Bird Observation")
                                )
                            }
                        }
                    }
                    // Add all markers to the map
                    mMap.clear()
                    for (markerOptions in markerOptionsList) {
                        mMap.addMarker(markerOptions)
                    }
                    userobservations()
                    // Adjust the camera to show all markers within the specified range
                    if (!markerOptionsList.isEmpty()) {
                        val builder = LatLngBounds.builder()
                        val userLatLng = LatLng(userLocation.lat,userLocation.lng)
                        builder.include(userLatLng)
                        for (markerOptions in markerOptionsList) {
                            builder.include(markerOptions.position)
                        }
                        val bounds = builder.build()
                        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 250)
                        mMap.moveCamera(cameraUpdate)
                    }
                }

                override fun onFailure(call: Call<List<HotspotsItem>?>, t: Throwable) {
                    // Handle the failure
                }
            })
        } else {
            Log.e("Error", "User's location is null.")
        }
    }

//    adapted from youtube
//    authour:CodingWithMitch
//    link to Author:https://www.youtube.com/@codingwithmitch
//    link:https://www.youtube.com/watch?v=f47L1SL5S0o
//    date:20 Sept 2018
    private fun fetchDataAndAddMarkers() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.ebird.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EBirdApiService::class.java)

    var latlng=LatLng(User.staticUser.getLocation().lat,User.staticUser.getLocation().lng)
    var countryCode=getCountryCode(latlng.latitude,latlng.longitude)
        val retrofitData = retrofit.getRecentHotspots("${countryCode}")
        Log.d("countrycode"," apppl: ${countryCode}")
        retrofitData.enqueue(object : Callback<List<HotspotsItem>?> {
            override fun onResponse(
                call: Call<List<HotspotsItem>?>,
                response: Response<List<HotspotsItem>?>
            ) {
                val HotspotList = response.body() ?: emptyList()

                for (hotspots in HotspotList) {
                    val location = LatLng(hotspots.lat, hotspots.lng)
                    mMap.addMarker(MarkerOptions().position(location).title("Bird Observation"))

                }
                userobservations()
            }

            override fun onFailure(call: Call<List<HotspotsItem>?>, t: Throwable) {
                // Handle the failure
            }
        })
    }

    private fun showUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    User.staticUser.setLocation(it.latitude, it.longitude)
                    Log.d("show", "${it.latitude}  ${it.longitude}")

                    if(alreadyZoomed){
                        if(User.staticUser.getPreferedRadius()<=0){
                            fetchDataAndAddMarkers()
                        }else{
                            Log.d("radius parameter","${User.staticUser.getPreferedRadius()}")
                            fetchDataAndAddMarkers(User.staticUser.getPreferedRadius())
                        }
                    }


                  if(alreadyZoomed){
                      val userLatLng = LatLng(it.latitude, it.longitude)
                      val cameraUpdate = CameraUpdateFactory.newLatLngZoom(userLatLng, 50f) // You can adjust the zoom level as needed (15f in this case)
                      mMap.moveCamera(cameraUpdate)
                      alreadyZoomed=false
                  }

                }
            }
        }
    }
    //adapted from youtube
//    authour:CodingWithMitch
//    link to Author:https://www.youtube.com/@codingwithmitch
//    link:https://www.youtube.com/watch?v=f47L1SL5S0o
//    date:20 Sept 2018
    private fun calculateDirections(marker: Marker) {

        val destination = com.google.maps.model.LatLng(
            marker.position.latitude,
            marker.position.longitude
        )
        mGeoApiContext= GeoApiContext.Builder()
            .apiKey("AIzaSyBA__k_CwwnYUvyQQq2IV511Ekl9g4d2Lk")
            .build()

        val directions = DirectionsApiRequest(mGeoApiContext)

        directions.alternatives(true)
        directions.origin(
            User.staticUser.getLocation()?.let {
                com.google.maps.model.LatLng(
                    it.lat,
                    it.lng
                )
            }
        )
        directions.destination(destination).setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {
                addPolylinesToMap(result)
            }

            override fun onFailure(e: Throwable) {
            }
        })
    }

    //adapted from youtube
//    authour:CodingWithMitch
//    link to Author:https://www.youtube.com/@codingwithmitch
//    link:https://www.youtube.com/watch?v=xl0GwkLNpNI
//    date:20 Sept 2018
    private fun addPolylinesToMap(result: DirectionsResult) {
        Handler(Looper.getMainLooper()).post {

            if(mPolyLinesData.size>0){
                for(polylineData in mPolyLinesData){
                    polylineData.getPolyline().remove()
                }
                mPolyLinesData.clear()
                mPolyLinesData= ArrayList<PolyLineData>()
            }

            for (route in result.routes) {
                val decodedPath = PolylineEncoding.decode(route.overviewPolyline.encodedPath)

                val newDecodedPath = ArrayList<LatLng>()

                for (latLng in decodedPath) {
                    newDecodedPath.add(LatLng(latLng.lat, latLng.lng))
                }

                val polyline = mMap.addPolyline(
                    PolylineOptions().addAll(newDecodedPath)
                        .color(Color.GRAY)
                )

                polyline.isClickable = true
                mPolyLinesData.add(PolyLineData(polyline,route.legs[0]))



            }
        }
    }
    //adapted from youtube
//    authour:CodingWithMitch
//    link to Author:https://www.youtube.com/@codingwithmitch
//    link:https://www.youtube.com/watch?v=3f09neIN89o&t=403s
//    date:20 Sept 2018
    override fun onPolylineClick(polyline: Polyline) {
        var index =0
        for (polylineData in mPolyLinesData) {

            index++
            if (polyline.id == polylineData.getPolyline().id ){
                polylineData.getPolyline().color = Color.BLUE
                polylineData.getPolyline().zIndex = 1.0f
                endlocation= LatLng(
                   polylineData.getLeg().endLocation.lat,
                   polylineData.getLeg().endLocation.lng)

                if(User.staticUser.getpreferedMeasurement()==User.Measurement.KILOMETERS){
                    // Convert the distance from meters to kilometers
                    val distanceInMeters = polylineData.getLeg().distance.inMeters
                    val distanceInKilometers = distanceInMeters / 1000.0
                    mSelectedMarker!!.title="Trip: $index"
                    mSelectedMarker!!.snippet="Duration: ${polylineData.getLeg().duration}, Distance: ${distanceInKilometers} km"
                    mSelectedMarker?.showInfoWindow()

                }else{
                    mSelectedMarker!!.title="Trip:$index"
                    mSelectedMarker!!.snippet="Duration: ${polylineData.getLeg().duration} ${polylineData.getLeg().distance}"
                    mSelectedMarker?.showInfoWindow()


                }

            } else {
                polylineData.getPolyline().color = Color.GRAY
                polylineData.getPolyline().zIndex = 0.0f
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
    private fun getCountryCode(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (addresses != null) {
            return if (addresses.isNotEmpty()) {
                addresses[0].countryCode ?: ""
            } else {
                "Unknown"
            }
        }else{
            return "Unknown"
        }
    }
    private fun userobservations() {

        val databaseRef = FirebaseDatabase.getInstance().reference
        val uid = User.staticUser?.getUid().toString()
        val categoryPath = "Users/$uid/observed List"

        databaseRef.child(categoryPath).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    observationsList.clear() // Clear the list before populating it again
                    for (childSnapshot in dataSnapshot.children) {
                        var date = childSnapshot.child("date").value.toString()
                        var lat= childSnapshot.child("latitude").value.toString().toDouble()
                        var long=childSnapshot.child("longitude").value.toString().toDouble()
                        var location= LatLng(lat,long)
                        val dateFormat = SimpleDateFormat("yyyy-MM-dd")

                        val markerColor = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)

                        val formateddate: Date = dateFormat.parse(date)
                        mMap.addMarker(MarkerOptions().position(location)
                            .title("your Bird Observation")
                            .snippet("Date:${dateFormat.format(formateddate)}\n").icon(markerColor))
                    }

                } else {

                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle any errors that occur while retrieving data
                // For example, you can log an error message
            }


        })
    }
}



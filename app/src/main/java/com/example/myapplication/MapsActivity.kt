package com.example.myapplication

import PolyLineData
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

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
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
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

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, OnPolylineClickListener{

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mGeoApiContext: GeoApiContext
    private var userLocation: LatLng? =null
    private var mPolyLinesData: ArrayList<PolyLineData> = ArrayList()
    private var mSelectedMarker: Marker? =null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient=LocationServices.getFusedLocationProviderClient(this)

    }

    /**
     * Manipulates the map once available.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnPolylineClickListener(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Enable the My Location layer
            mMap.isMyLocationEnabled = true

            // Show the user's location
            showUserLocation()
            // Fetch and add bird observation markers
            fetchDataAndAddMarkers()
        } else {
            // Request location permission from the user
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }

        mMap.setOnMarkerClickListener { marker ->

            mSelectedMarker=marker
            calculateDirections(marker)
            true // Return true to consume the event
        }

    }

    private fun fetchDataAndAddMarkers() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.ebird.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EBirdApiService::class.java)

        val retrofitData = retrofit.getRecentHotspots("US")
        retrofitData.enqueue(object : Callback<List<HotspotsItem>?> {
            override fun onResponse(
                call: Call<List<HotspotsItem>?>,
                response: Response<List<HotspotsItem>?>
            ) {
                val HotspotList = response.body() ?: emptyList()

                for (hotspots in HotspotList) {
                    val location = LatLng(hotspots.lat, hotspots.lng)
                    mMap.addMarker(MarkerOptions().position(location).title("Bird Observation"))
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(location))
                    Log.d("hotspot","${hotspots.lat} ${hotspots.lng}")
                }



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
                     userLocation = LatLng(it.latitude, it.longitude)

                }
            }
        }
    }

    private fun calculateDirections(marker: Marker) {
        Log.d("TAG", "calculateDirections: calculating directions.")

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
            userLocation?.let {
                com.google.maps.model.LatLng(
                    it.latitude,
                    it.longitude
                )
            }
        )

        Log.d("TAG", "calculateDirections: destination: $destination")

        directions.destination(destination).setCallback(object : PendingResult.Callback<DirectionsResult> {
            override fun onResult(result: DirectionsResult) {
                addPolylinesToMap(result)
            }

            override fun onFailure(e: Throwable) {
                Log.e("TAG", "calculateDirections: Failed to get directions: " + e.message)
            }
        })
    }
    private fun addPolylinesToMap(result: DirectionsResult) {
        Handler(Looper.getMainLooper()).post {
            Log.d("TAG", "run: result routes: " + result.routes.size)

            if(mPolyLinesData.size>0){
                for(polylineData in mPolyLinesData){
                    polylineData.getPolyline().remove()
                }
                mPolyLinesData.clear()
                mPolyLinesData= ArrayList<PolyLineData>()
            }

            for (route in result.routes) {
                Log.d("TAG", "run: leg: " + route.legs[0].toString())
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

    override fun onPolylineClick(polyline: Polyline) {
        var index =0
        for (polylineData in mPolyLinesData) {

            index++
            Log.d("TAG", "onPolylineClick: toString: " + polylineData.toString())
            if (polyline.id == polylineData.getPolyline().id ){
                polylineData.getPolyline().color = Color.BLUE
                polylineData.getPolyline().zIndex = 1.0f

               var endlocation= LatLng(
                   polylineData.getLeg().endLocation.lat,
                   polylineData.getLeg().endLocation.lng)

                val marker: Marker? = mMap.addMarker(
                    MarkerOptions()
                        .position(endlocation)
                        .title("Trip:$index")
                        .snippet("Duration: ${polylineData.getLeg().duration}")
                )
                marker?.showInfoWindow()
                mSelectedMarker?.isVisible=false

            } else {
                polylineData.getPolyline().color = Color.GRAY
                polylineData.getPolyline().zIndex = 0.0f
            }
        }

    }




}



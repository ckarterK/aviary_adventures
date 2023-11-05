package com.example.myapplication.Models

import com.example.myapplication.Observation
import com.google.maps.model.LatLng

class User {



    // Private instance variables
    private var email:String = ""
    private var password: String = ""
    private var name: String = ""
    private var preferedMeasurement: String=""
    private var PreferedRadius: Int=50
    private var Uid: String =""
    private var location: LatLng = LatLng(0.0, 0.0)
    private var observation: MutableList<Observation> = mutableListOf()


    // Getter and setter for username
    fun getEmail(): String {
        return email
    }
    fun createUserToMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["name"] = name
        result["email"] = email
        return result
    }
    fun PreferedRadiustoMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["PreferedRadius"]=PreferedRadius
        return result
    }
    fun preferedMeasurementtoMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["preferedMeasurement"]=preferedMeasurement
        return result
    }

    fun setEmail(email: String) {
        this.email = email
    }

    fun getUid(): String {
        return Uid
    }

    fun setUid(uid: String) {
        this.Uid = uid
    }

    // Getter and setter for password
    fun getPassword(): String {
        return password
    }

    fun setPassword(password: String) {
        this.password = password
    }

    // Getter and setter for name
    fun getName(): String {
        return name
    }

    fun setName(name: String) {
        this.name = name
    }

    // Getter and setter for measurement
    fun getPreferedRadius(): Int {
        return PreferedRadius
    }

    fun setPreferedRadius(Radius: Int) {
        this.PreferedRadius = Radius
    }
    fun getLocation(): LatLng {
        return location
    }

    fun setLocation(lat: Double, lng: Double) {
        this.location = LatLng(lat, lng)
    }

    fun getpreferedMeasurement(): String {
        return preferedMeasurement
    }

    fun setpreferedMeasurement(measurement: String) {
        this.preferedMeasurement = measurement
    }

    // Getter and setter for observation
    fun getObservation(): List<Observation> {
        return observation
    }

    fun addObservation(observation: Observation) {
        this.observation.add(observation)
    }

    companion object {
        // Create a static User object with predefined values
        val staticUser = User()
    }
    object Measurement {
        const val KILOMETERS = "Kilometers"
        const val MILES = "Miles"
    }

    object Radius {
        const val RADIUS_5 = 5
        const val RADIUS_10 = 10
        const val RADIUS_15 = 15
        const val RADIUS_30 = 30
    }
}

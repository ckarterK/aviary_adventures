package com.example.myapplication
class User {



    // Private instance variables
    private var email:String = ""
    private var password: String = ""
    private var name: String = ""
    private var measurement: String=""
    private var Uid: String =""
    private var observation: MutableList<Observation> = mutableListOf()

    // Getter and setter for username
    fun getEmail(): String {
        return email
    }
    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["name"] = name
        result["email"] = email
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
    fun getMeasurement(): String {
        return measurement
    }

    fun setMeasurement(measurement: String) {
        this.measurement = measurement
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
}

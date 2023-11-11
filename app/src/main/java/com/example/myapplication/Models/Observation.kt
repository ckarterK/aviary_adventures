package com.example.myapplication.Models

import com.example.myapplication.Models.User

class Observation {

    // Instance fields as mutable lists
    private lateinit var birdSpecies: String
    private lateinit var description: String
    private var quantity: Int=0
    private lateinit var locations: String
    private lateinit var pictureID: String
    private lateinit var observationDate: String

    fun toMap(): Map<String, Any> {
        val result = HashMap<String, Any>()

        result["birdSpecies"] = birdSpecies
        result["description"] = description
        result["quantity"] = quantity
        result["pictureID"] = pictureID
        result["latitude"]=User.staticUser.getLocation().lat
        result["longitude"]=User.staticUser.getLocation().lng
        result["date"]=observationDate


        return result
    }

    // Getter methods for birdSpecies
    fun getBirdSpecies(): String {
        return birdSpecies
    }

    // Setter methods for birdSpecies
    fun setBirdSpecies(species: String) {
        birdSpecies=species
    }

    // Getter methods for description
    fun getDescription(): String {
        return description
    }

    // Setter methods for description
    fun setDescription(description: String) {
        this.description=description
    }

    // Getter methods for quantity
    fun getQuantity(): Int {
        return quantity
    }

    // Setter methods for quantity
    fun setQuantity(qty: Int) {
        quantity=qty
    }

    // Getter methods for locations
    fun getLocations(): String {
        return locations
    }

    // Setter methods for locations
    fun setLocation(location: String) {
        this.locations=location
    }

    // Getter methods for pictureID
    fun getPictureID(): String{
        return pictureID
    }

    // Setter methods for pictureID
    fun setPictureID(picID: String) {
        pictureID=picID
    }

    // Getter methods for observationDate
    fun getObservationDate(): String {
        return observationDate
    }

    // Setter methods for observationDate
    fun setObservationDate(date: String) {
        observationDate=date


    }
}



package com.example.myapplication.Models

class diaryNotes(
        private var subject: String="",
        private var location: String="",
        private var date: String="",
        private var description: String="") {


     fun creatediaryNotes(): Map<String, Any> {
        val result = HashMap<String, Any>()
        result["subject"] = subject
        result["location"] = location
        result["description"] = description
        result["date"] = date
        return result
    }

    fun getSubject(): String {
        return subject
    }
    fun setSubject(subject: String) {
        this.subject = subject
    }

    fun getlocation(): String {
        return location
    }
    fun setlocation(location: String) {
        this.location = location
    }

    fun getdate(): String {
        return date
    }
    fun setdate(date: String) {
        this.date = date
    }

    fun setdescription(description: String) {
        this.description = description
    }
    fun getdescription(): String {
        return description
    }




}
package com.example.myapplication.Observations_RecyclerView

import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class Myadapter(private val observationList: ArrayList<observationList>) :
    RecyclerView.Adapter<Myadapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_observations, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = observationList[position]
        holder.date.text = currentItem.date
        holder.observerdSpecies.text = currentItem.observedSpecies
        holder.observeredBirds.text = currentItem.observedBirds
        holder.observationLocation.text = currentItem.observationLocation

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val imageRef = storageRef.child("images/${currentItem.observationPicture}")

        try {
            val localFile = File.createTempFile("${currentItem.observationPicture}", "jpg")

            imageRef.getFile(localFile)
                .addOnSuccessListener {
                var bitmap=BitmapFactory.decodeFile(localFile.absolutePath)
                    holder.observationPicture.setImageBitmap(bitmap)


                }
                .addOnFailureListener { exception ->
                    // Handle any errors that occurred during the image download
                    Log.e("MyAdapter", "Error downloading image: ${exception.message}")
                }
        } catch (e: Exception) {
            // Handle any exceptions that occurred during file creation
            Log.e("MyAdapter", "Error creating local file: ${e.message}")
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var date: TextView = itemView.findViewById(R.id.date)
        var observerdSpecies: TextView = itemView.findViewById(R.id.observerdSpecies)
        var observeredBirds: TextView = itemView.findViewById(R.id.observeredBirds)
        var observationLocation: TextView = itemView.findViewById(R.id.observationLocation)
        var observationPicture: ImageView = itemView.findViewById(R.id.picture)
    }

    override fun getItemCount(): Int {
        return observationList.size
    }
}

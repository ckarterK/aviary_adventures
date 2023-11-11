package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.RecyclerViewModels.observationList

class Myadapter(private val observationList:ArrayList<observationList>):
    RecyclerView.Adapter<Myadapter.MyViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView=LayoutInflater.from(parent.context).inflate(R.layout.list_observations,parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
       val currentItem = observationList[position]
        holder.date.text=currentItem.date
        holder.observerdSpecies.text=currentItem.observedSpecies
        holder.observeredBirds.text=currentItem.observedBirds
    }

    class MyViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        var date: TextView=itemView.findViewById(R.id.date)
        var observerdSpecies: TextView=itemView.findViewById(R.id.subject)
        var observeredBirds: TextView=itemView.findViewById(R.id.location)

    }

    override fun getItemCount(): Int {
      return observationList.size
    }

}

package com.example.myapplication.diaryNotes_RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class diaryNotesAdapter(private val diaryNotesList:ArrayList<DiaryNotesList>):
    RecyclerView.Adapter<diaryNotesAdapter.MyViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView=
            LayoutInflater.from(parent.context).inflate(R.layout.list_diarynotes,parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = diaryNotesList[position]
        holder.date.text=currentItem.date
        holder.subject.text=currentItem.subject
        holder.description.text=currentItem.description
        holder.location.text=currentItem.location
    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var date: TextView =itemView.findViewById(R.id.date)
        var subject: TextView =itemView.findViewById(R.id.subject)
        var description: TextView =itemView.findViewById(R.id.description)
        var location: TextView =itemView.findViewById(R.id.location)

    }

    override fun getItemCount(): Int {
        return diaryNotesList.size
    }
}
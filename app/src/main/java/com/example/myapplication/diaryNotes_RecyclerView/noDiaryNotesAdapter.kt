package com.example.myapplication.diaryNotes_RecyclerView

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

class noDiaryNotesAdapter(private val nodiaryNotesList:ArrayList<noDiaryNotesList>):
    RecyclerView.Adapter<noDiaryNotesAdapter.MyViewHolder>()  {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView=
            LayoutInflater.from(parent.context).inflate(R.layout.list_diarynotes,parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = nodiaryNotesList[position]
        holder.emptyText.text=currentItem.textMessage

    }

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var emptyText: TextView =itemView.findViewById(R.id.noDiaryEntries)


    }

    override fun getItemCount(): Int {
        return nodiaryNotesList.size
    }
}
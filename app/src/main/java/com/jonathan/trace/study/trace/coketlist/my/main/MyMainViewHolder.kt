package com.jonathan.trace.study.trace.coketlist.my.main

import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R

class MyMainViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    private val iv1: ImageView = itemView.findViewById(R.id.iv_recycler_1)
    private val iv2: ImageView = itemView.findViewById(R.id.iv_recycler_2)

    fun bind(data: ArrayList<Thumbnail>){
        //TODO("thumbnail not yet implemented")
        iv1.setImageResource(R.drawable.thumbnail_sample_white)
        iv1.setOnClickListener{
            Toast.makeText(itemView.context, "thumbnail id: ${data[0].id}", Toast.LENGTH_SHORT).show()
        }
        if(data.size == 2) {
            iv2.setImageResource(R.drawable.thumbnail_sample_white)
            iv2.setOnClickListener {
                Toast.makeText(itemView.context, "thumbnail id: ${data[1].id}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
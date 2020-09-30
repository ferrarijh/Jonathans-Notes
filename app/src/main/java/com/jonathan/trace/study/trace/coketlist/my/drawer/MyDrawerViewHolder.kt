package com.jonathan.trace.study.trace.coketlist.my.drawer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R

class MyDrawerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
    private val iv = itemView.findViewById<ImageView>(R.id.iv_drawer_item)
    private val tv = itemView.findViewById<TextView>(R.id.tv_drawer_item)

    fun bind(item: MyDrawerItem){
        iv.setImageResource(item.imageId)
        tv.setText(item.textId)
        itemView.setOnClickListener{
            Toast.makeText(itemView.context, "${tv.text}!", Toast.LENGTH_SHORT).show()
        }
    }
}
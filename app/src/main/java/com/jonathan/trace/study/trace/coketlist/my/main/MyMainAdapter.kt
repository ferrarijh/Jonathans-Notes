package com.jonathan.trace.study.trace.coketlist.my.main

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R

class MyMainAdapter (private var context: Context): RecyclerView.Adapter<MyMainViewHolder>(){
    //TODO
    var data = arrayListOf<ArrayList<Thumbnail>>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyMainViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.views_for_main_recycler, parent, false)
        return MyMainViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyMainViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size
}
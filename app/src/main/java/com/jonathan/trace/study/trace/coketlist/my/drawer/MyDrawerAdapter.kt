package com.jonathan.trace.study.trace.coketlist.my.drawer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.R

class MyDrawerAdapter(private val context: Context): RecyclerView.Adapter<MyDrawerViewHolder>(){
    var data = arrayListOf<MyDrawerItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDrawerViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.views_for_drawer_recycler, parent, false)
        return MyDrawerViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyDrawerViewHolder, position: Int) = holder.bind(data[position])

    override fun getItemCount(): Int = data.size

}
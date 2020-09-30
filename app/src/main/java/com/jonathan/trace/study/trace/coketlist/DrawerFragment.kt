package com.jonathan.trace.study.trace.coketlist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jonathan.trace.study.trace.coketlist.my.drawer.MyDrawerAdapter
import com.jonathan.trace.study.trace.coketlist.my.drawer.MyDrawerItem
import kotlinx.android.synthetic.main.fragment_drawer.*

class DrawerFragment: Fragment(){
    private lateinit var myDrawerAdapter: MyDrawerAdapter
    private val data = arrayListOf<MyDrawerItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_drawer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        myDrawerAdapter = MyDrawerAdapter(view.context)
        rv_drawer.adapter = myDrawerAdapter
        rv_drawer.layoutManager = LinearLayoutManager(view.context)
        loadData()
    }

    private fun loadData(){
        data.apply{
            add(MyDrawerItem(R.drawable.mylock, R.string.lock))
            add(MyDrawerItem(R.drawable.trash, R.string.trashcan))
            add(MyDrawerItem(R.drawable.setting, R.string.settings))
        }
        myDrawerAdapter.data = data
        myDrawerAdapter.notifyDataSetChanged()
    }
}
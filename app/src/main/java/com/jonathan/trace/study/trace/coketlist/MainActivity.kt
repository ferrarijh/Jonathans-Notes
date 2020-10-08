package com.jonathan.trace.study.trace.coketlist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.jonathan.trace.study.trace.coketlist.room.NoteViewModel
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController
    private lateinit var mViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /*
        iv_hamburger.setOnClickListener {
            layout_drawer.openDrawer(GravityCompat.START)
        }

         */

        mViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        setFAB()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_nav_host) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setFAB(){
        fab_add.setOnClickListener{
            appBar.setExpanded(true)
            val action = HomeFragmentDirections.actionHomeFragmentToEditNoteFragment()
            findNavController(R.id.fragment_nav_host).navigate(action)
        }
    }


    override fun onBackPressed() {
        if (layout_drawer.isDrawerOpen(GravityCompat.START))
            layout_drawer.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

}
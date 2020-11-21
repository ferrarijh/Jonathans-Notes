package com.jonathan.trace.study.trace.coketlist

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_drawer.*

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_nav_host) as NavHostFragment
        navController = navHostFragment.navController

        setDrawerItems()
    }

    override fun onBackPressed() {
        if (layout_drawer.isDrawerOpen(GravityCompat.START))
            layout_drawer.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }

    private fun setDrawerItems(){
        ll_item_private.setOnClickListener{
            fab_add.hide()
            navController.navigate(HomeFragmentDirections.actionHomeFragmentToPrivateFragment())
        }
        ll_item_trash.setOnClickListener{
            fab_add.hide()
            navController.navigate(HomeFragmentDirections.actionHomeFragmentToTrashCanFragment())
        }
    }
}
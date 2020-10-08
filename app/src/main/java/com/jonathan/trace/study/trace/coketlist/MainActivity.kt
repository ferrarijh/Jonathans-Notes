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
    private lateinit var sortDialog: AlertDialog
    private lateinit var mViewModel: NoteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        iv_hamburger.setOnClickListener {
            layout_drawer.openDrawer(GravityCompat.START)
        }

        mViewModel = ViewModelProvider(this).get(NoteViewModel::class.java)
        setFAB()
        mSetSupportActionBar()
        setDialog()

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment_nav_host) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setDialog() {
        val sortTitle = getString(R.string.sort_by_title)
        val sortBody = getString(R.string.sort_by_body)
        val sortCreated = getString(R.string.sort_by_time_created)
        val sortModified = getString(R.string.sort_by_time_modified)

        val sortSel = arrayOf(sortModified, sortCreated, sortTitle, sortBody)
        val builderSort = AlertDialog.Builder(this)
        builderSort.setItems(sortSel) { _, i ->
            when (sortSel[i]) {
                sortModified -> mViewModel.sortState.value = NoteViewModel.SortState.MODIFIED
                sortCreated -> mViewModel.sortState.value = NoteViewModel.SortState.CREATED
                sortTitle -> mViewModel.sortState.value = NoteViewModel.SortState.TITLE
                sortBody -> mViewModel.sortState.value = NoteViewModel.SortState.BODY
            }
        }
        sortDialog = builderSort.create()
    }

    private fun setFAB(){
        fab_add.setOnClickListener{
            appBar.setExpanded(true)
            val action = HomeFragmentDirections.actionHomeFragmentToEditNoteFragment()
            findNavController(R.id.fragment_nav_host).navigate(action)
        }
    }

    private fun mSetSupportActionBar(){
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowTitleEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_actions, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onBackPressed() {
        if (layout_drawer.isDrawerOpen(GravityCompat.START))
            layout_drawer.closeDrawer(GravityCompat.START)
        else
            super.onBackPressed()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        when(item.itemId){
            R.id.action_search -> {
                toast.setText("selected search item!")
                val action = HomeFragmentDirections.actionHomeFragmentToSearchFragment()
                navController.navigate(action)
            }
            R.id.action_sort -> {
                toast.setText("selected sort item!")
                sortDialog.show()
            }
            else -> toast.setText("not working :(")
        }
        toast.show()

        return super.onOptionsItemSelected(item)
    }
}
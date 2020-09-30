package com.jonathan.trace.study.trace.coketlist

import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.jonathan.trace.study.trace.coketlist.my.main.MyMainAdapter
import com.jonathan.trace.study.trace.coketlist.my.main.Thumbnail
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    lateinit var myMainAdapter: MyMainAdapter
    private val data = arrayListOf<ArrayList<Thumbnail>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //inflate toolbar with right side action icons
        //toolbar.inflateMenu(R.menu.menu_actions)
        setSupportActionBar(toolbar)

        //add hamburger button
        val ivHamburger = toolbar.findViewById<ImageView>(R.id.iv_hamburger)
        ivHamburger.setOnClickListener {
            layout_drawer.openDrawer(Gravity.LEFT)
        }

        supportActionBar!!.setDisplayShowTitleEnabled(false)

        myMainAdapter = MyMainAdapter(this)
        rv_main.adapter = myMainAdapter
        loadData(20)

        //show/hide fab on scroll
        rv_main.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy>0)
                    fab.hide()
                else
                    fab.show()

                super.onScrolled(recyclerView, dx, dy)
            }
        })

        fab.setOnClickListener{
            startActivity(Intent(this, EditActivity::class.java))
        }
    }

    private fun loadData(n: Int){

        for(i in 0 until n) {
            data.add(arrayListOf<Thumbnail>())
            data[data.lastIndex].add(Thumbnail(i*2))
            data[data.lastIndex].add(Thumbnail(i*2 + 1))
        }
        myMainAdapter.data = data
        myMainAdapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        if (layout_drawer.isDrawerOpen(Gravity.LEFT))
            layout_drawer.closeDrawer(Gravity.LEFT)
        else
            super.onBackPressed()
    }

    //inflate menu with custom menu resource
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_actions, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val toast = Toast.makeText(this, "", Toast.LENGTH_SHORT)
        when(item.itemId){
            R.id.action_search -> toast.setText("selected search item!")
            R.id.action_sort -> toast.setText("selected sort item!")
            else -> toast.setText("not working :(")
        }
        toast.show()

        return super.onOptionsItemSelected(item)
    }
}
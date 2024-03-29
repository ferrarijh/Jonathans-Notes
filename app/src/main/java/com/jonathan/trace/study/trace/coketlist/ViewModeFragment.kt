package com.jonathan.trace.study.trace.coketlist

import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_view_only.*

class ViewModeFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_view_only, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setCanvas()
        setDrawer()
        setAppBar()
    }

    private fun setCanvas(){
        val note = ViewModeFragmentArgs.fromBundle(requireArguments()).note
        tv_note_title.text = note.title
        tv_note_body.text = note.body
        tv_note_body.movementMethod = ScrollingMovementMethod()
        cv_note_view_only.setCardBackgroundColor(Color.parseColor(note.color))
    }

    private fun setAppBar(){
        requireActivity().findViewById<ImageView>(R.id.iv_hamburger).setImageResource(android.R.color.transparent)
        val toolBar = requireActivity().findViewById<Toolbar>(R.id.toolbar)
        toolBar.setNavigationOnClickListener{
            findNavController().navigateUp()
        }
    }

    private fun setDrawer(){
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }
}
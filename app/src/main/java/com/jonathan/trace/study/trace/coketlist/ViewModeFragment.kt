package com.jonathan.trace.study.trace.coketlist

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_view_only.*

class ViewModeFragment : Fragment(){

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_view_only, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val note = ViewModeFragmentArgs.fromBundle(requireArguments()).note
        tv_note_title.text = note.title
        tv_note_body.text = note.body
        tv_note_body.movementMethod = ScrollingMovementMethod()
        setOnBackPressed()
        setDrawer()
    }

    private fun setDrawer(){
        val drawer = requireActivity().findViewById<DrawerLayout>(R.id.layout_drawer)
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    private fun setOnBackPressed(){
        requireActivity().onBackPressedDispatcher.addCallback(this){
            val action = ViewModeFragmentDirections.actionViewModeFragmentToTrashCanFragment()
            findNavController().navigate(action)
        }
    }
}
package com.jonathan.trace.study.trace.coketlist.dialog.fragment

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.jonathan.trace.study.trace.coketlist.R
import kotlinx.android.synthetic.main.fragment_image_view.*
import java.io.File

class ImageViewFragment: DialogFragment(){

    override fun onStart() {
        super.onStart()

        val mp = ViewGroup.LayoutParams.MATCH_PARENT
        val density = resources.displayMetrics.density
        val h = resources.displayMetrics.heightPixels * 2/3
        dialog?.window?.setLayout(mp, h)
        dialog?.window?.setGravity(Gravity.TOP)

        Log.d("", "h: $h")
        Log.d("","density: $density, height: ${resources.displayMetrics.heightPixels}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_image_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val uri = with(requireArguments().getString("fullDir")){
            Uri.fromFile(File(this!!))
        }
        tiv_big_screen.setImageURI(uri)
    }
}
package com.jonathan.trace.study.trace.coketlist.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.dialog.*

class MyDialog(
    context: Context,
    private val layoutId: Int,
    val title: String,
    private val pClickListener: View.OnClickListener,
): Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layoutId)

        val lp = window?.attributes
        lp?.apply{
            flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
            dimAmount = 0.5f
            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.MATCH_PARENT
            gravity = Gravity.BOTTOM
        }

        window?.apply {
            setBackgroundDrawableResource(android.R.color.transparent)
            val newX = context.resources.displayMetrics.widthPixels
            val newY = context.resources.displayMetrics.heightPixels
            setLayout((newX*0.9).toInt(), (newY*0.9).toInt())
            Log.d("", "layout: $newX, $newY")
        }

        tv_dialog_title.text = title

        btn_positive.setOnClickListener(pClickListener)
        btn_negative.setOnClickListener{
            this.dismiss()
        }
    }
}
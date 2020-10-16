package com.jonathan.trace.study.trace.coketlist.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import com.jonathan.trace.study.trace.coketlist.R
import kotlinx.android.synthetic.main.dialog_delete_multi.*

class DeleteMultiDialog(
    context: Context,
    private val pClickListener: View.OnClickListener
): Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_delete_multi)

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
        }

        btn_delete_multi_positive.setOnClickListener(pClickListener)
        btn_delete_multi_negative.setOnClickListener{
            this.dismiss()
        }
    }
}
package com.jonathan.trace.study.trace.coketlist

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import kotlinx.android.synthetic.main.dialog.*

class MyDialog(
    context: Context,
    private val pClickListener: View.OnClickListener,
): Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
        layoutParams.dimAmount = 0.8f
        window?.attributes = layoutParams

        window?.setBackgroundDrawableResource(R.drawable.dialog_background)

        setContentView(R.layout.dialog)
        btn_positive.setOnClickListener(pClickListener)
        btn_negative.setOnClickListener{
            this.dismiss()
        }
    }
}
package com.jonathan.trace.study.trace.coketlist.gesturelistener

import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent

class MyGestureListener: GestureDetector.OnGestureListener {
    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onShowPress(e: MotionEvent?) {
    }

    override fun onSingleTapUp(e: MotionEvent?): Boolean {
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return true
    }

    override fun onLongPress(e: MotionEvent?) {
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d("", "e1: $e1")
        Log.d("", "e2: $e2")
        Log.d("", "velocityX: $velocityX")
        Log.d("", "velocityY: $velocityY")
        return true
    }
}
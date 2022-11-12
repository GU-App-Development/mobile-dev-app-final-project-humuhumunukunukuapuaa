package com.zybooks.reeftriggerfish.uitel

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs

private const val SWIPE_THRESHOLD = 100
private const val SWIPE_VELOCITY_THRESHOLD = 100

open class OnSwipeListener(context : Context?) : View.OnTouchListener
{
    private var gestureDetector : GestureDetector

    // *************** MOTION DETECTION ***************
    override fun onTouch(p0: View?, motionEvent: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(motionEvent)
    }

    // *************** MOTION TRACKING ***************
    inner class GestureListener : SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?) : Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false

            val yDiff = e2.y - e1.y
            val xDiff = e2.x - e1.x

            if( abs(xDiff) > abs(yDiff) ) {
                //  *************** LEFT OR RIGHT MOVEMENT ***************
                if( abs(xDiff) > SWIPE_THRESHOLD && abs(velocityX) > SWIPE_VELOCITY_THRESHOLD){
                    if( (xDiff > 0) ){
                        onSwipeRight()
                    } else {
                        onSwipeLeft()
                    }
                    result = true
                }
            }  else if ( abs(yDiff) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD){
                //  *************** UP OR DOWN MOVEMENT ***************
                if( (yDiff < 0) ){
                    onSwipeTop()
                } else {
                    onSwipeBottom()
                }
                result = true
            }
            return result
        }
    }

    // These get overridden in MainActivity.onCreate()
    open fun onSwipeLeft() {}
    open fun onSwipeRight() {}
    open fun onSwipeBottom() {}
    open fun onSwipeTop() {}

    init {
        gestureDetector = GestureDetector(context, GestureListener())
    }
}
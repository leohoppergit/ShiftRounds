package de.nulide.shiftcal.ui.helper

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout

open class TouchInterceptedLinearLayout(context: Context, attrs: AttributeSet) :
    LinearLayout(context, attrs) {

    var interceptedTouchListener: ((MotionEvent) -> Unit)? = null

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        onTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        interceptedTouchListener?.invoke(event)
        if (event.action == MotionEvent.ACTION_UP) {
            performClick()
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}

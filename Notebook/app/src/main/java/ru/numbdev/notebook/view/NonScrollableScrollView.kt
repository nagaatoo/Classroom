package ru.numbdev.notebook.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ScrollView

class NonScrollableScrollView(context: Context, attrs: AttributeSet?) : ScrollView(context, attrs) {

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }
}

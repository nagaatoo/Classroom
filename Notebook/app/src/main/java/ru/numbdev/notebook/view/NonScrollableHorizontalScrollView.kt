package ru.numbdev.notebook.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView
import android.widget.ScrollView

class NonScrollableHorizontalScrollView(context: Context, attrs: AttributeSet?) : HorizontalScrollView(context, attrs) {

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return false
    }
}

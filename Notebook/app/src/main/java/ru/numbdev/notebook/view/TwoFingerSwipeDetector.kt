package ru.numbdev.notebook.view

import android.view.MotionEvent
import kotlin.math.abs

class SingleFingerSwipeDetector(
    private val onSwipeLeft: () -> Unit,
    private val onSwipeRight: () -> Unit
) {

    private var startX = 0f
    private var startY = 0f
    private var isInBottomRightZone = false

    companion object {
        private const val MIN_SWIPE_DISTANCE = 80
        private const val ZONE_SIZE = 150 // Размер зоны в нижнем правом углу
        private const val BOTTOM_MARGIN = 200 // Отступ снизу для кнопок
    }

    fun onTouchEvent(event: MotionEvent, screenWidth: Int, screenHeight: Int): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                
                // Проверяем, что касание в нижнем правом углу
                val rightEdge = screenWidth - ZONE_SIZE
                val bottomEdge = screenHeight - ZONE_SIZE - BOTTOM_MARGIN
                
                isInBottomRightZone = startX > rightEdge && startY > bottomEdge
            }

            MotionEvent.ACTION_UP -> {
                if (isInBottomRightZone) {
                    val endX = event.x
                    val endY = event.y
                    
                    val deltaX = endX - startX
                    val deltaY = endY - startY
                    
                    // Проверяем горизонтальный свайп
                    if (abs(deltaX) > MIN_SWIPE_DISTANCE && abs(deltaX) > abs(deltaY)) {
                        if (deltaX > 0) {
                            onSwipeRight()
                            return true
                        } else {
                            onSwipeLeft()
                            return true
                        }
                    }
                }
                isInBottomRightZone = false
            }

            MotionEvent.ACTION_CANCEL -> {
                isInBottomRightZone = false
            }
        }
        
        return false
    }
} 
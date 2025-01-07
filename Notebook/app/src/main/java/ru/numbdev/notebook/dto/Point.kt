package ru.numbdev.notebook.dto

data class Point(
    val x: Float,
    val y: Float,
    val timestamp: Long,
    val order: LineOrder
)

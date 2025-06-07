package ru.numbdev.notebook.dto

data class LineBlock(
    val id: String,
    val pageNumber: Number,
    val type: ToolType,
    val point: Point
)

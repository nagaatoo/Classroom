package ru.numbdev.notebook.dto

data class Line(
    val id: String,
    val role: Role,
    val type: ToolType,
    val points: MutableList<Point>
)

package ru.numbdev.notebook.dto

import android.graphics.Paint
import android.graphics.Path

data class ServiceLine(
    var line: Line,
    var path: Path,
    var drawPaint: Paint,
    var isFinished: Boolean // TODO
)

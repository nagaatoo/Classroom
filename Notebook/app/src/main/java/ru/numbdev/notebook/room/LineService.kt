package ru.numbdev.notebook.room

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import ru.numbdev.notebook.dto.Line
import ru.numbdev.notebook.dto.LineBlock
import ru.numbdev.notebook.dto.LineOrder
import ru.numbdev.notebook.dto.Point
import ru.numbdev.notebook.dto.Role
import ru.numbdev.notebook.dto.ServiceLine
import ru.numbdev.notebook.dto.ToolType
import java.util.UUID

class LineService {
    companion object {

        private var currentTool: ToolType = ToolType.PEN

        // Current line witch drawing
        private var currentLine: ServiceLine? = null

        // Store circles to draw each time the user touches down
        private val teacherPoints: MutableList<ServiceLine> = mutableListOf()
        private val teammatePoints: MutableList<ServiceLine> = mutableListOf()
        private val myPoints: MutableList<ServiceLine> = mutableListOf()

        fun isInit(): Boolean {
            return RoomStateParams.role != null
        }

        /**
         * Полная реинициализация LineService при смене страницы
         */
        fun reinitialize() {
            println("Reinitializing LineService")

            // Очищаем текущую линию
            currentLine = null

            // Очищаем все точки
            teacherPoints.clear()
            teammatePoints.clear()
            myPoints.clear()

            println("LineService reinitialized")
        }

        fun changeTool() {
            if (currentTool == ToolType.PEN) {
                currentTool = ToolType.ERASER
            } else {
                currentTool = ToolType.PEN
            }
        }

        fun drawLines(canvas: Canvas) {
            drawLine(canvas, teammatePoints)
            drawLine(canvas, myPoints)
            drawLine(canvas, teacherPoints)
        }

        private fun drawLine(canvas: Canvas, line: MutableList<ServiceLine>) {
            line.forEachIndexed { _, serviceLine ->
                canvas.drawPath(serviceLine.path, serviceLine.drawPaint)
            }
        }

        fun cleanMy() {
            myPoints.clear()
        }

        fun doCleanLines(linesForDelete: Map<String, Line>?) {
            if (linesForDelete == null) {
                return
            }

            if (linesForDelete.values.firstOrNull()?.userIdOwner == RoomStateParams.userId) {
                return
            }

            val lines = mutableListOf<ServiceLine>()
            for (i in 0..<teammatePoints.size) {
                val target = teammatePoints[i]

                if (linesForDelete.containsKey(target.line.id)) {
                    lines.add(target)
                }
            }

            lines.forEach { teammatePoints.remove(it) }
        }

        fun doTeacherCleanLines(linesForDelete: Map<String, Line>?) {
            if (linesForDelete == null) {
                return
            }

            val lines = mutableListOf<ServiceLine>()
            for (i in 0..<teacherPoints.size) {
                val target = teacherPoints[i]
                if (linesForDelete.containsKey(target.line.id)) {
                    lines.add(target)
                }
            }

            lines.forEach { teacherPoints.remove(it) }
        }

        fun doInitState(lines: Map<String, Line>?) {
            printState(lines, true)
        }

        fun doPrintState(lines: Map<String, Line>?) {
            printState(lines, false)
        }

        private fun printState(lines: Map<String, Line>?, isInit: Boolean) {
            if (lines == null) {
                return
            }

            lines
                .entries
                .forEach { es ->
                    val line = es.value
                    if (!isInit && RoomStateParams.userId == line.userIdOwner) {
                        println("Skip $isInit and user ${RoomStateParams.userId}")
                        return;
                    }

                    when (line.role) {
                        Role.TEACHER -> {
                            println("Print line for teacher")
                            addAndPrint(line, getPen(line.type, line.role), teacherPoints)
                        }

                        Role.STUDENT -> {
                            addAndPrint(line, getPen(line.type, line.role), teammatePoints)
                        }
                    }
                }
        }

        private fun addAndPrint(line: Line, pen: Paint, targetPoints: MutableList<ServiceLine>) {
            val serviceLine: ServiceLine
            val lastOrNull = targetPoints.firstOrNull { it.line.id == line.id }
            if (lastOrNull == null) {
                serviceLine = ServiceLine(
                    line,
                    Path(),
                    pen,
                    false
                )

                targetPoints.add(serviceLine)
            } else {
                serviceLine = targetPoints.last()
                serviceLine.line.points.addAll(line.points)
            }

            line.points.forEachIndexed { _, point ->
                val touchX = point.x
                val touchY = point.y

                when (point.order) {
                    LineOrder.FIRST -> serviceLine.path.moveTo(touchX, touchY)
                    LineOrder.LAST -> {
                        serviceLine.path.lineTo(touchX, touchY)
                        serviceLine.isFinished = true
                    }

                    LineOrder.MIDDLE -> serviceLine.path.lineTo(touchX, touchY)
                }
            }
        }

        fun createNewLine(touchX: Float, touchY: Float): LineBlock {
            currentLine = ServiceLine(
                createLine(UUID.randomUUID(), touchX, touchY, LineOrder.FIRST),
                Path(),
                getPen(),
                false
            )
            currentLine?.path?.moveTo(touchX, touchY)
            myPoints.add(currentLine!!)

            return createBlock(touchX, touchY, LineOrder.FIRST)
        }

        private fun createLine(
            currentLineId: UUID,
            touchX: Float,
            touchY: Float,
            order: LineOrder
        ): Line {
            return Line(
                currentLineId.toString(),
                RoomStateParams.userId,
                Role.STUDENT,
                currentTool,
                mutableListOf(fillDrawPoint(touchX, touchY, order))
            )
        }

        fun moveLine(touchX: Float, touchY: Float): LineBlock {
            currentLine?.path?.lineTo(touchX, touchY)

            return createBlock(touchX, touchY, LineOrder.MIDDLE)
        }

        fun endLine(touchX: Float, touchY: Float): LineBlock {
            val block = createBlock(touchX, touchY, LineOrder.LAST)
            currentLine = null

            return block
        }

        private fun createBlock(touchX: Float, touchY: Float, order: LineOrder): LineBlock {
            return LineBlock(
                currentLine?.line!!.id,
                RoomStateParams.currentPage,
                currentLine?.line!!.type,
                Point(
                    x = touchX,
                    y = touchY,
                    timestamp = System.currentTimeMillis(),
                    order
                )
            )
        }

        private fun getPen(): Paint {
            return getPen(currentTool, RoomStateParams.role)
        }

        private fun getPen(type: ToolType, role: Role?): Paint {
            if (type == ToolType.ERASER) {
                return createEraserPaint()
            }

            if (role == Role.TEACHER) {
                return createTeacherPenPaint()
            }

            return createPenPaint()
        }

        private fun fillDrawPoint(touchX: Float, touchY: Float, order: LineOrder): Point {
            return Point(
                touchX,
                touchY,
                System.currentTimeMillis(),
                order
            )
        }

        private fun createPenPaint(): Paint {
            val drawPaint = Paint()
            drawPaint.style = Paint.Style.FILL;
            drawPaint.setColor(Color.BLACK);
            drawPaint.isAntiAlias = true;
            drawPaint.strokeWidth = 5F;
            drawPaint.style = Paint.Style.STROKE;
            drawPaint.strokeJoin = Paint.Join.ROUND;
            drawPaint.strokeCap = Paint.Cap.ROUND;

            return drawPaint
        }

        private fun createTeacherPenPaint(): Paint {
            val drawPaint = Paint()
            drawPaint.style = Paint.Style.FILL;
            drawPaint.setColor(Color.RED);
            drawPaint.isAntiAlias = true;
            drawPaint.strokeWidth = 5F;
            drawPaint.style = Paint.Style.STROKE;
            drawPaint.strokeJoin = Paint.Join.ROUND;
            drawPaint.strokeCap = Paint.Cap.ROUND;

            return drawPaint
        }

        private fun createEraserPaint(): Paint {
            val drawPaint = Paint()
            drawPaint.style = Paint.Style.FILL;
            drawPaint.setColor(Color.WHITE);
            drawPaint.isAntiAlias = true;
            drawPaint.strokeWidth = 5F;
            drawPaint.style = Paint.Style.STROKE;
            drawPaint.strokeJoin = Paint.Join.ROUND;
            drawPaint.strokeCap = Paint.Cap.ROUND;

            return drawPaint
        }
    }
}
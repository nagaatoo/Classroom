package ru.numbdev.notebook.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.numbdev.notebook.client.RoomClient
import ru.numbdev.notebook.dto.Line
import ru.numbdev.notebook.dto.LineBlock
import ru.numbdev.notebook.dto.CommandToRoom
import ru.numbdev.notebook.dto.LineOrder
import ru.numbdev.notebook.dto.Point
import ru.numbdev.notebook.dto.Role
import ru.numbdev.notebook.dto.ServiceLine
import ru.numbdev.notebook.dto.ToolType
import ru.numbdev.notebook.dto.command.CleanCommand
import ru.numbdev.notebook.dto.command.Command
import ru.numbdev.notebook.dto.command.PrintCommand
import java.util.UUID


class PaintView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val gson = Gson();

    // Tool type
    private var toolType = ToolType.PEN;

    // Solution for clear
    private var isClear = false

    // Current line witch drawing
    private var currentLine: ServiceLine? = null

    // Store circles to draw each time the user touches down
    private val teacherPoints: MutableList<ServiceLine> = mutableListOf()
    private val teammatePoints: MutableList<ServiceLine> = mutableListOf()
    private val myPoints: MutableList<ServiceLine> = mutableListOf()

    init {
        isFocusable = true;
        setFocusableInTouchMode(true);
        setup()
    }

    fun changeTool() {
        if (toolType == ToolType.PEN) {
            setupEraserPaint()
            println("Changed to eraser")
        } else {
            setupPenPaint()
            println("Changed to pen")
        }
    }

    fun clean() {
        cleanState()
        sendCleanToSession()
    }

    private fun cleanState() {
        println("Clean")
        teacherPoints.clear()
        myPoints.clear()
        teammatePoints.clear()
        isClear = true
        invalidate()
    }

    private fun printState(lines: Map<String, Line>) {
        lines
            .entries
            .forEach { es ->
                val line = es.value

                when (line.role) {
                    Role.TEACHER -> {
                        val pen = if (line.type == ToolType.PEN) createTeacherPenPaint() else createEraserPaint()
                        addAndPrint(line, pen, teacherPoints)
                    }

                    Role.STUDENT -> {
                        val pen = if (line.type == ToolType.PEN) createPenPaint() else createEraserPaint()
                        addAndPrint(line, pen, teammatePoints)
                    }
                }
            }

        postInvalidate()
    }

    private fun addAndPrint(line: Line, pen: Paint, targetPoints: MutableList<ServiceLine>) {
        val serviceLine : ServiceLine
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
//                    serviceLine.isFinished = true
                }
                LineOrder.MIDDLE -> serviceLine.path.lineTo(touchX, touchY)
            }
        }
    }

    private fun setup() {
        GlobalScope.launch {
            RoomClient.getSession()?.incoming?.consumeEach {
                val currentCommand =
                    gson.fromJson(it.data.toString(Charsets.UTF_8), CommandToRoom::class.java)
                when (currentCommand.command) {
                    Command.CLEAN -> cleanState()
                    Command.PRINT -> printState(currentCommand.lines)
                    Command.TEACHER_CLEAN -> cleanState()
                    else -> {}
                }
                println("Incoming finished")
            }
        }
    }

    private fun setupPenPaint() {
        toolType = ToolType.PEN
    }

    private fun setupEraserPaint() {
        toolType = ToolType.ERASER
    }

    override fun onDraw(canvas: Canvas) {
        if (isClear) {
            canvas.drawColor(Color.TRANSPARENT);
            isClear = false
        }

        drawLine(canvas, teammatePoints)
        drawLine(canvas, myPoints)
        drawLine(canvas, teacherPoints)
    }

    private fun drawLine(canvas: Canvas, line: MutableList<ServiceLine>) {
        line.forEachIndexed { _, serviceLine ->
            canvas.drawPath(serviceLine.path, serviceLine.drawPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {  // Starts a new line in the path
                println("Start print new line on $touchX : $touchY")
                currentLine = ServiceLine(
                    createLine(UUID.randomUUID(), touchX, touchY, LineOrder.FIRST),
                    Path(),
                    if (toolType == ToolType.PEN) createPenPaint() else createEraserPaint(),
                    false
                )
//                currentLine?.path?.moveTo(touchX, touchY)
                myPoints.add(currentLine!!)

                sendToSession(touchX, touchY, LineOrder.FIRST)
            }

            MotionEvent.ACTION_MOVE -> { // Draws line between last point and this point
                println("$touchX : $touchY")
//                currentLine?.path?.lineTo(touchX, touchY)
                currentLine?.line?.points?.add(fillDrawPoint(touchX, touchY, LineOrder.MIDDLE))
                sendToSession(touchX, touchY, LineOrder.MIDDLE)
            }

            MotionEvent.ACTION_UP -> {
                println("Finish print line on $touchX : $touchY")
//                currentLine?.line?.points?.add(fillDrawPoint(touchX, touchY, LineOrder.LAST))
                sendToSession(touchX, touchY, LineOrder.LAST)
                currentLine = null
            }

            else -> return false
        }
        postInvalidate()
        return true
    }

    private fun sendToSession(touchX: Float, touchY: Float, order: LineOrder) {
        runBlocking {
            RoomClient.send(
                PrintCommand(
                    block = LineBlock(
                        currentLine?.line!!.id,
                        currentLine?.line!!.type,
                        Point(
                            x = touchX,
                            y = touchY,
                            timestamp = System.currentTimeMillis(),
                            order
                        )
                    )
                )
            )
        }
    }

    private fun sendCleanToSession() {
        runBlocking {
            RoomClient.send(
                CleanCommand(
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    private fun createLine(
        currentLineId: UUID,
        touchX: Float,
        touchY: Float,
        order: LineOrder
    ): Line {
        return Line(
            currentLineId.toString(),
            Role.STUDENT,
            toolType,
            mutableListOf(fillDrawPoint(touchX, touchY, order))
        )
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
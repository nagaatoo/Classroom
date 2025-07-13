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
import ru.numbdev.notebook.dto.LineBlock
import ru.numbdev.notebook.dto.CommandToRoom
import ru.numbdev.notebook.dto.command.CleanCommand
import ru.numbdev.notebook.dto.command.Command
import ru.numbdev.notebook.dto.command.PrintCommand
import ru.numbdev.notebook.room.LineService
import ru.numbdev.notebook.room.RoomStateParams
import java.util.UUID


class PaintView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private val gson = Gson();

    // Solution for clear
    private var isClear = false
    private var roomId: UUID? = null

    private var notePaint: Paint = initNotePaint()
    private val noteLinesX: MutableList<Path> = mutableListOf()
    private val noteLinesY: MutableList<Path> = mutableListOf()

    init {
        isFocusable = true;
        setFocusableInTouchMode(true);
        setBackgroundColor(Color.WHITE)
        setup()
        viewTreeObserver.addOnGlobalLayoutListener { setupNote() }
    }

    private fun setup() {
        GlobalScope.launch {
            RoomClient.initClient()
            RoomClient.getSession()?.incoming?.consumeEach {
                val currentCommand =
                    gson.fromJson(it.data.toString(Charsets.UTF_8), CommandToRoom::class.java)
                when (currentCommand.command) {
                    Command.INIT -> {
                        roomId = currentCommand.roomId
                        RoomStateParams.role = currentCommand.role
                        LineService.doInitState(currentCommand.lines)
                    }
                    Command.TO_PAGE -> {
                        LineService.doInitState(currentCommand.lines)
                    }
                    Command.CLEAN -> LineService.doCleanLines(currentCommand.lines)
                    Command.PRINT -> LineService.doPrintState(currentCommand.lines)
                    Command.TEACHER_CLEAN -> LineService.doTeacherCleanLines(currentCommand.lines)
                    else -> {}
                }

                postInvalidate()
            }

            runBlocking {
                RoomClient.changePage(RoomStateParams.currentPage)
            }
        }
    }

    private fun setupNote() {
        for (i in 0 .. width step 100) {
            val newPathX = Path()
            newPathX.moveTo(0F, i.toFloat())
            newPathX.lineTo(height.toFloat(), i.toFloat())
            noteLinesX.add(newPathX)

            val newPathY = Path()
            newPathY.moveTo(i.toFloat(), 0F)
            newPathY.lineTo(i.toFloat(), height.toFloat())
            noteLinesY.add(newPathY)
        }
    }

    private fun initNotePaint(): Paint {
        val drawPaint = Paint()
        drawPaint.style = Paint.Style.FILL;
        drawPaint.setColor(Color.BLACK);
        drawPaint.isAntiAlias = true;
        drawPaint.strokeWidth = 2F;
        drawPaint.style = Paint.Style.STROKE;
        drawPaint.strokeJoin = Paint.Join.ROUND;
        drawPaint.strokeCap = Paint.Cap.ROUND;

        return drawPaint
    }

    fun changeTool() {
        LineService.changeTool()

    }

    fun clean() {
        cleanState()
        sendCleanToSession()
    }

    private fun cleanState() {
        println("Clean")
        LineService.cleanMy()
        isClear = true
        invalidate()
    }

    /**
     * Полная реинициализация PaintView при смене страницы
     */
    fun reinitialize() {
        println("Reinitializing PaintView")
        
        // Очищаем все состояние
        isClear = true
        
        // Очищаем линии сетки
        noteLinesX.clear()
        noteLinesY.clear()
        
        // Сбрасываем состояние LineService
        LineService.reinitialize()
        
        // Пересоздаем Paint
        notePaint = initNotePaint()

        // Устанавливаем белый фон
        setBackgroundColor(Color.WHITE)
        
        // Пересоздаем сетку если размеры уже известны
        if (width > 0 && height > 0) {
            setupNote()
        }

        isFocusable = true;
        setFocusableInTouchMode(true);
        setBackgroundColor(Color.WHITE)
        runBlocking {
            RoomClient.changePage(RoomStateParams.currentPage)
        }
        // Принудительная перерисовка
        invalidate()
        
        println("PaintView reinitialized")
    }

    override fun onDraw(canvas: Canvas) {
        if (isClear) {
            canvas.drawColor(Color.TRANSPARENT);
            isClear = false
        }

        printNotebook(canvas)
        LineService.drawLines(canvas)
    }

    private fun printNotebook(canvas: Canvas) {
        for (i in 0..<noteLinesX.size) {
            canvas.drawPath(noteLinesX[i], notePaint)
            canvas.drawPath(noteLinesY[i], notePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Если нет роли (инициализация приходит с сервера), то считаем, что тетрадь не готова
        if (!LineService.isInit()) {
            return false
        }

        val touchX = event.x
        val touchY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {  // Starts a new line in the path
                println("Start print new line on $touchX : $touchY")
                val lineBlock = LineService.createNewLine(touchX, touchY)
                sendToSession(lineBlock)
            }

            MotionEvent.ACTION_MOVE -> { // Draws line between last point and this point
                println("$touchX : $touchY")
                val lineBlock = LineService.moveLine(touchX, touchY)
                sendToSession(lineBlock)
            }

            MotionEvent.ACTION_UP -> {
                println("Finish print line on $touchX : $touchY")
                val lineBlock = LineService.endLine(touchX, touchY)
                sendToSession(lineBlock)
            }

            else -> return false
        }
        postInvalidate()
        return true
    }

    private fun sendToSession(lineBlock: LineBlock) {
        runBlocking {
            RoomClient.send(
                PrintCommand(
                    block = lineBlock
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

}
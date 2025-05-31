package ru.numbdev.notebook

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ru.numbdev.notebook.room.RoomStateParams
import ru.numbdev.notebook.view.PaintView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Показываем информацию о пользователе
        setupUserInfo()

        findViewById<Button>(R.id.tool).setOnClickListener {
            findViewById<PaintView>(R.id.paintView).changeTool()
        }

        findViewById<Button>(R.id.cleanAll).setOnClickListener {
            findViewById<PaintView>(R.id.paintView).clean()
        }
    }

    private fun setupUserInfo() {
        val userInfoText = findViewById<TextView>(R.id.userInfoText)
        val roleText = if (RoomStateParams.isTeacher) "Учитель" else "Ученик"
        val roomText = RoomStateParams.selectedRoomId?.toString()?.substring(0, 8) ?: "Неизвестна"
        userInfoText.text = "${RoomStateParams.username} ($roleText)\nКомната: $roomText"
    }
}
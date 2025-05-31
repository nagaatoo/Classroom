package ru.numbdev.notebook

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ru.numbdev.notebook.room.RoomStateParams
import java.util.UUID

class LoginActivity : AppCompatActivity() {

    private val roomIds = mutableListOf(
        UUID.fromString("9ffeec9b-1035-4b6f-b6e7-6a51ce97d943"),
        UUID.fromString("015949bb-3694-4841-bdb6-101d565fa586")
    )

    private val roomNames = mutableListOf(
        "Комната 1 (Математика)",
        "Комната 2 (Физика)"
    )

    private lateinit var usernameEditText: EditText
    private lateinit var roleRadioGroup: RadioGroup
    private lateinit var roomSpinner: Spinner
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Инициализация views
        usernameEditText = findViewById(R.id.usernameEditText)
        roleRadioGroup = findViewById(R.id.roleRadioGroup)
        roomSpinner = findViewById(R.id.roomSpinner)
        loginButton = findViewById(R.id.loginButton)

        // Настройка Spinner для выбора комнаты
        setupRoomSpinner()

        // Обработчик нажатия на кнопку входа
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            
            if (username.isNotEmpty()) {
                // Определяем роль пользователя
                val selectedRoleId = roleRadioGroup.checkedRadioButtonId
                val isTeacher = selectedRoleId == R.id.teacherRadioButton
                
                // Получаем выбранную комнату
                val selectedRoomPosition = roomSpinner.selectedItemPosition
                val selectedRoomId = roomIds[selectedRoomPosition]
                val selectedRoomName = roomNames[selectedRoomPosition]
                
                // Сохраняем данные пользователя
                RoomStateParams.userId = UUID.randomUUID().toString()
                RoomStateParams.username = username
                RoomStateParams.isTeacher = isTeacher
                RoomStateParams.selectedRoomId = selectedRoomId
                
                // Показываем сообщение о входе
                val roleText = if (isTeacher) "учителя" else "ученика"
                Toast.makeText(this, "Вход выполнен как $roleText: $username\nКомната: $selectedRoomName", Toast.LENGTH_LONG).show()
                
                // Переходим к главной активности
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Введите имя пользователя", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRoomSpinner() {
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item,
            roomNames
        )
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        roomSpinner.adapter = adapter
    }
} 
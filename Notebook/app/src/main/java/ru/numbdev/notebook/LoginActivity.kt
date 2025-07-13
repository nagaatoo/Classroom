package ru.numbdev.notebook

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.numbdev.notebook.client.ApiClient
import ru.numbdev.notebook.room.RoomStateParams

class LoginActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Инициализация views
        usernameEditText = findViewById(R.id.usernameEditText)
        loginButton = findViewById(R.id.loginButton)

        // Обработчик нажатия на кнопку входа
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()

            if (username.isNotEmpty()) {
                // Отключаем кнопку во время запроса
                loginButton.isEnabled = false
                loginButton.text = "Авторизация..."

                // Выполняем REST-запрос для авторизации
                lifecycleScope.launch {
                    try {
                        val loginResponse = ApiClient.login(username)

                        // Сохраняем данные пользователя
                        RoomStateParams.username = username
                        RoomStateParams.userId = loginResponse.userId
                        RoomStateParams.role = loginResponse.role
                        RoomStateParams.isTeacher = loginResponse.role.name == "TEACHER"

                        // Показываем сообщение о входе
                        val roleText = if (RoomStateParams.isTeacher) "учителя" else "ученика"
                        Toast.makeText(this@LoginActivity, "Вход выполнен как $roleText: $username", Toast.LENGTH_LONG).show()

                        // Переходим к выбору комнаты
                        val intent = Intent(this@LoginActivity, RoomSelectionActivity::class.java)
                        startActivity(intent)
                        finish()

                    } catch (e: Exception) {
                        // Обработка ошибок
                        Toast.makeText(this@LoginActivity, "Ошибка авторизации: ${e.message}", Toast.LENGTH_LONG).show()

                        // Включаем кнопку обратно
                        loginButton.isEnabled = true
                        loginButton.text = "Войти в класс"
                    }
                }
            } else {
                Toast.makeText(this, "Введите имя пользователя", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
package ru.numbdev.notebook

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.numbdev.notebook.client.ApiClient
import ru.numbdev.notebook.dto.Room
import ru.numbdev.notebook.dto.RoomPage
import ru.numbdev.notebook.room.RoomStateParams

class RoomSelectionActivity : AppCompatActivity() {

    private lateinit var roomsListView: ListView
    private lateinit var progressBar: ProgressBar
    private lateinit var userInfoTextView: TextView
    private lateinit var createNewRoomButton: Button
    private lateinit var roomAdapter: ArrayAdapter<String>
    private var rooms: List<Room> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_room_selection)

        initViews()
        loadRooms()
    }

    private fun initViews() {
        roomsListView = findViewById(R.id.roomsListView)
        progressBar = findViewById(R.id.progressBar)
        userInfoTextView = findViewById(R.id.userInfoTextView)
        createNewRoomButton = findViewById(R.id.createNewRoomButton)

        // Показываем информацию о пользователе
        val roleText = if (RoomStateParams.isTeacher) "Учитель" else "Ученик"
        userInfoTextView.text = "Добро пожаловать, ${RoomStateParams.username}! ($roleText)"

        // Настройка адаптера для ListView
        roomAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        roomsListView.adapter = roomAdapter

        // Обработчик кликов по комнатам
        roomsListView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedRoom = rooms[position]
            selectRoom(selectedRoom)
        }

        // Обработчик кнопки создания новой комнаты
        createNewRoomButton.setOnClickListener {
            createNewRoom()
        }
    }

    private fun loadRooms() {
        progressBar.visibility = View.VISIBLE
        roomsListView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                rooms = ApiClient.getRooms().rooms
                
                // Обновляем adapter с названиями комнат и их ID
                val roomDisplayNames = rooms.map { room ->
                    "${room.name} (${room.id.toString().substring(0, 8)}...)"
                }
                
                roomAdapter.clear()
                roomAdapter.addAll(roomDisplayNames)
                roomAdapter.notifyDataSetChanged()

                progressBar.visibility = View.GONE
                roomsListView.visibility = View.VISIBLE

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@RoomSelectionActivity, "Ошибка загрузки комнат: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun selectRoom(room: Room) {
        // Сохраняем выбранную комнату
        RoomStateParams.selectedRoomId = room.id

        Toast.makeText(this, "Выбрана комната: ${room.name}", Toast.LENGTH_SHORT).show()

        // Переходим к главной активности
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun createNewRoom() {
        // Устанавливаем пустой room_id для создания новой комнаты
        RoomStateParams.selectedRoomId = null

        Toast.makeText(this, "Создается новая комната...", Toast.LENGTH_SHORT).show()

        // Переходим к главной активности
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
} 
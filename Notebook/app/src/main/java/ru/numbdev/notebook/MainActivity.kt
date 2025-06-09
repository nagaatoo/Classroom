package ru.numbdev.notebook

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import kotlinx.coroutines.runBlocking
import ru.numbdev.notebook.adapter.PaintPagesAdapter
import ru.numbdev.notebook.client.RoomClient
import ru.numbdev.notebook.fragment.PaintPageFragment
import ru.numbdev.notebook.room.RoomStateParams

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var pageIndicator: TextView
    private lateinit var adapter: PaintPagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация views
        initViews()

        // Показываем информацию о пользователе
        setupUserInfo()

        // Настройка ViewPager2
        setupViewPager()

        // Настройка кнопок
        setupButtons()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        pageIndicator = findViewById(R.id.pageIndicator)
    }

    private fun setupViewPager() {
        adapter = PaintPagesAdapter(this)
        viewPager.adapter = adapter

        // Отключаем стандартное перелистывание ViewPager2
        viewPager.isUserInputEnabled = false

        // Слушатель изменения страницы
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                // Реинициализируем PaintView для новой страницы
                getCurrentFragment()?.reinitialize()

                updatePageIndicator(position)
                RoomStateParams.currentPage = position
                runBlocking {
                    RoomClient.changePage(position)
                }
            }
        })

        updatePageIndicator(0)
    }

    private fun setupButtons() {
        // Кнопки управления инструментами
        findViewById<Button>(R.id.tool).setOnClickListener {
            getCurrentFragment()?.changeTool()
        }

        findViewById<Button>(R.id.cleanAll).setOnClickListener {
            getCurrentFragment()?.clean()
        }

        // Кнопки навигации между страницами
        findViewById<Button>(R.id.previousPageButton).setOnClickListener {
            goToPreviousPage()
        }

        findViewById<Button>(R.id.nextPageButton).setOnClickListener {
            goToNextPage()
        }
    }

    private fun setupUserInfo() {
        val userInfoText = findViewById<TextView>(R.id.userInfoText)
        val roleText = if (RoomStateParams.isTeacher) "Учитель" else "Ученик"
        val roomText = RoomStateParams.selectedRoomId?.toString()?.substring(0, 8) ?: "Неизвестна"
        userInfoText.text = "${RoomStateParams.username} ($roleText)\nКомната: $roomText"
    }

    private fun updatePageIndicator(position: Int) {
        val pageNumber = adapter.getCurrentPageNumber(position)
        pageIndicator.text = "Страница $pageNumber"
    }

    private fun getCurrentFragment(): PaintPageFragment? {
        val currentPosition = viewPager.currentItem
        return supportFragmentManager.findFragmentByTag("f$currentPosition") as? PaintPageFragment
    }

    private fun goToNextPage() {
        val currentPosition = viewPager.currentItem
        val maxPosition = adapter.itemCount - 1

        if (currentPosition == maxPosition) {
            // Создаем новую страницу
            val newPosition = adapter.addPage()
            viewPager.setCurrentItem(newPosition, true)
        } else {
            // Переходим к существующей следующей странице
            viewPager.setCurrentItem(currentPosition + 1, true)
        }
    }

    private fun goToPreviousPage() {
        val currentPosition = viewPager.currentItem
        if (currentPosition > 0) {
            viewPager.setCurrentItem(currentPosition - 1, true)
        }
    }
}
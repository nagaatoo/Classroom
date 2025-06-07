package ru.numbdev.notebook.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ru.numbdev.notebook.fragment.PaintPageFragment

class PaintPagesAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val pages = mutableListOf<Int>()

    init {
        // Добавляем первую страницу
        addPage()
    }

    override fun getItemCount(): Int = pages.size

    override fun createFragment(position: Int): Fragment {
        return PaintPageFragment.newInstance(pages[position])
    }

    fun addPage(): Int {
        val newPageNumber = pages.size + 1
        pages.add(newPageNumber)
        notifyItemInserted(pages.size - 1)
        return pages.size - 1
    }

    fun getCurrentPageNumber(position: Int): Int {
        return if (position < pages.size) pages[position] else 1
    }
} 
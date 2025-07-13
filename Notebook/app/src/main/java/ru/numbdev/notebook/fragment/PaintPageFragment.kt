package ru.numbdev.notebook.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import ru.numbdev.notebook.R
import ru.numbdev.notebook.view.PaintView

class PaintPageFragment : Fragment() {

    private lateinit var paintView: PaintView
    private var pageNumber: Int = 0

    companion object {
        private const val ARG_PAGE_NUMBER = "page_number"

        fun newInstance(pageNumber: Int): PaintPageFragment {
            val fragment = PaintPageFragment()
            val args = Bundle()
            args.putInt(ARG_PAGE_NUMBER, pageNumber)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageNumber = arguments?.getInt(ARG_PAGE_NUMBER) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_paint_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        paintView = view.findViewById(R.id.paintView)
    }

    fun changeTool() {
        if (::paintView.isInitialized) {
            paintView.changeTool()
        }
    }

    fun clean() {
        if (::paintView.isInitialized) {
            paintView.clean()
        }
    }

    /**
     * Полная реинициализация PaintView при смене страницы
     */
    fun reinitialize() {
        if (::paintView.isInitialized) {
            paintView.reinitialize()
        }
    }
} 
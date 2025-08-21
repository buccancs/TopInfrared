package com.topdon.lib.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.topdon.lib.ui.R
import com.topdon.lib.ui.widget.IndicateView
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.dialog_tip_preview.view.*
import java.util.Timer
import kotlin.collections.ArrayList

class TipPreviewDialog : DialogFragment() {

    private lateinit var titleList: ArrayList<String>
    private var dis: Disposable?=null
    var closeEvent: ((check: Boolean) -> Unit)? = null
    private var canceled = false
    private var hasCheck = false

    private lateinit var tvContent: TextView
    private lateinit var checkBox: CheckBox
    private lateinit var imgClose: ImageView
    private lateinit var viewPager : ViewPager
    private lateinit var indicateView: IndicateView
    private var index : Int = -1
    private val pageCount = 2
    private var timer : Timer?= Timer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_tip_preview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        titleList = arrayListOf(
            getString(R.string.preview_step_1),
            getString(R.string.preview_step_2),
        )
        checkBox = view.dialog_tip_check
        imgClose = view.img_close
        viewPager = view.view_pager
        tvContent = view.tv_content
        indicateView = view.indicate_view
        val adapter = PageAdapter(childFragmentManager)
        indicateView.itemCount = adapter.count
        viewPager.adapter = adapter
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            hasCheck = isChecked
        }
        imgClose.setOnClickListener {
            closeEvent?.invoke(hasCheck)
            dismiss()
        }
        view.tv_i_know.setOnClickListener {
            closeEvent?.invoke(hasCheck)
            dismiss()
        }
        updateIndex(0)
        viewPager.addOnPageChangeListener(object :ViewPager.OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                updateIndex(position)
            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })
    }


    fun updateIndex(position : Int){
        if (index == position){
            return
        }
        indicateView.currentIndex = position
        viewPager.setCurrentItem(position,true)
        tvContent.text = titleList[position]
        index = position
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        timer = null
    }

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = -1
        params.height = -1
        dialog?.window?.attributes = params as WindowManager.LayoutParams
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            super.show(manager, tag)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        fun newInstance(): TipPreviewDialog {
            return TipPreviewDialog()
        }
    }

    inner class PageAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager) {
        override fun getCount(): Int {
            return pageCount
        }

        override fun getItem(position: Int): Fragment {
           return when (position) {
                0 -> {
                    PageFragment.newInstance(R.drawable.preview_step_1)
                }
                1 -> {
                    PageFragment.newInstance(R.drawable.preview_step_2)
                }
                else -> {
                    PageFragment.newInstance(R.drawable.preview_step_3)
                }
            }
        }
    }
}
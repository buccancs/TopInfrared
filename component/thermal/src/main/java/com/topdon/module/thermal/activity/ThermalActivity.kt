package com.topdon.module.thermal.activity

import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.BarUtils
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.ui.MenuFirstTabView
import com.topdon.module.thermal.R
import com.topdon.module.thermal.adapter.MenuTabAdapter
import com.topdon.module.thermal.fragment.event.ThermalActionEvent
import kotlinx.android.synthetic.main.activity_thermal.*
import org.greenrobot.eventbus.EventBus

@Route(path = RouterConfig.THERMAL_MAIN)
class ThermalActivity : BaseActivity() {

    private val menuAdapter by lazy { MenuTabAdapter(this) }

    override fun initContentView() = R.layout.activity_thermal

    override fun initView() {
        setTitleText(R.string.main_thermal)
        mToolBar!!.setBackgroundColor(blackColor)
        BarUtils.setStatusBarColor(this, blackColor)
        BarUtils.setNavBarColor(window, blackColor)
        initRecycler()
        thermal_tab.setOnItemListener(object : MenuFirstTabView.OnItemListener {
            override fun selectPosition(position: Int) {
                showRecycler(position)
            }

        })
    }

    override fun initData() {

    }

    private fun initRecycler() {
        thermal_recycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        thermal_recycler.adapter = menuAdapter
        thermal_recycler.visibility = View.GONE
        menuAdapter.initType(1)
        menuAdapter.listener = object : MenuTabAdapter.OnItemClickListener {
            override fun onClick(index: Int) {
                Log.w("123", "index: $index")
                EventBus.getDefault().post(ThermalActionEvent(action = index))
            }

        }
    }

    fun showRecycler(select: Int) {
        thermal_recycler.initType(select)
        if (select == 5) {
            thermal_recycler.visibility = View.GONE
            EventBus.getDefault().post(ThermalActionEvent(action = 5000))
        } else {
            thermal_recycler.visibility = View.VISIBLE
        }
    }

}
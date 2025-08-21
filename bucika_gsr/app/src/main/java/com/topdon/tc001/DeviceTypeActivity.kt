package com.topdon.tc001

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.DeviceTools
import kotlinx.android.synthetic.main.activity_device_type.*
import kotlinx.android.synthetic.main.item_device_type.view.*

/**
 * 设备类型选择.
 *
 * Created by LCG on 2024/4/22.
 */
class DeviceTypeActivity : BaseActivity() {

    /**
     * 当前点击的设备类型.
     */
    private var clientType: IRDeviceType? = null

    override fun initContentView(): Int = R.layout.activity_device_type

    override fun initView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = MyAdapter(this).apply {
            onItemClickListener = {
                clientType = it
                when (it) {
                    IRDeviceType.TS004 -> {
                        ARouter.getInstance()
                            .build(RouterConfig.IR_DEVICE_ADD)
                            .withBoolean("isTS004", true)
                            .navigation(this@DeviceTypeActivity)
                    }
                    IRDeviceType.TC007 -> {
                        ARouter.getInstance()
                            .build(RouterConfig.IR_DEVICE_ADD)
                            .withBoolean("isTS004", false)
                            .navigation(this@DeviceTypeActivity)
                    }
                    else -> {
                        ARouter.getInstance()
                            .build(RouterConfig.IR_MAIN)
                            .withBoolean(ExtraKeyConfig.IS_TC007, false)
                            .navigation(this@DeviceTypeActivity)
                        if (DeviceTools.isConnect()) {
                            finish()
                        }
                    }
                }
            }
        }
    }

    override fun initData() {
    }

    override fun connected() {
        if (clientType?.isLine() == true) {
            finish()
        }
    }

    override fun onSocketConnected(isTS004: Boolean) {
        if (isTS004) {
            if (clientType == IRDeviceType.TS004) {
                finish()
            }
        } else {
            if (clientType == IRDeviceType.TC007) {
                finish()
            }
        }
    }

    private class MyAdapter(val context: Context) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {

        var onItemClickListener: ((type: IRDeviceType) -> Unit)? = null

        private data class ItemInfo(val isTitle:Boolean, val firstType: IRDeviceType, val secondType: IRDeviceType?)

        // Modified for bucika_gsr - only TC001 support
        private val dataList: ArrayList<ItemInfo> = arrayListOf(
            ItemInfo(true, IRDeviceType.TC001, null),
        )

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_device_type, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val firstType: IRDeviceType = dataList[position].firstType
            val secondType: IRDeviceType? = dataList[position].secondType
            holder.itemView.tv_title.isVisible = dataList[position].isTitle
            holder.itemView.tv_title.text = context.getString(if (firstType.isLine()) R.string.tc_connect_line else R.string.tc_connect_wifi)

            holder.itemView.tv_item1.text = firstType.getDeviceName()
            when (firstType) {
                // TODO: 替换 TC002 Duo 图标
                IRDeviceType.TC001 -> holder.itemView.iv_item1.setImageResource(R.drawable.ic_device_type_tc001)
                IRDeviceType.TC001_PLUS -> holder.itemView.iv_item1.setImageResource(R.drawable.ic_device_type_tc001_plus)
                IRDeviceType.TC002C_DUO -> holder.itemView.iv_item1.setImageResource(R.drawable.ic_device_type_tc001_plus)
                IRDeviceType.TC007 -> holder.itemView.iv_item1.setImageResource(R.drawable.ic_device_type_tc007)
                IRDeviceType.TS001 -> holder.itemView.iv_item1.setImageResource(R.drawable.ic_device_type_ts001)
                IRDeviceType.TS004 -> holder.itemView.iv_item1.setImageResource(R.drawable.ic_device_type_ts004)
            }

            holder.itemView.group_item2.isVisible = secondType != null
            if (secondType != null) {
                holder.itemView.tv_item2.text = secondType.getDeviceName()
                when (secondType) {
                    // TODO: 替换 TC002 Duo 图标
                    IRDeviceType.TC001 -> holder.itemView.iv_item2.setImageResource(R.drawable.ic_device_type_tc001)
                    IRDeviceType.TC001_PLUS -> holder.itemView.iv_item2.setImageResource(R.drawable.ic_device_type_tc001_plus)
                    IRDeviceType.TC002C_DUO -> holder.itemView.iv_item2.setImageResource(R.drawable.ic_device_type_tc001_plus)
                    IRDeviceType.TC007 -> holder.itemView.iv_item2.setImageResource(R.drawable.ic_device_type_tc007)
                    IRDeviceType.TS001 -> holder.itemView.iv_item2.setImageResource(R.drawable.ic_device_type_ts001)
                    IRDeviceType.TS004 -> holder.itemView.iv_item2.setImageResource(R.drawable.ic_device_type_ts004)
                }
            }
        }

        override fun getItemCount(): Int = dataList.size

        inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
            init {
                rootView.view_bg_item1.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener?.invoke(dataList[position].firstType)
                    }
                }
                rootView.view_bg_item2.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val irDeviceType: IRDeviceType = dataList[position].secondType ?: return@setOnClickListener
                        onItemClickListener?.invoke(irDeviceType)
                    }
                }
            }
        }
    }

    /**
     * 支持的热成像设备类型.
     */
    enum class IRDeviceType {
        TC001 {
            override fun isLine(): Boolean = true
            override fun getDeviceName(): String = "TC001"
        },
        TC001_PLUS {
            override fun isLine(): Boolean = true
            override fun getDeviceName(): String = "TC001 Plus"
        },
        TC002C_DUO {
            override fun isLine(): Boolean = true
            override fun getDeviceName(): String = "TC002C Duo"
        },
        TC007 {
            override fun isLine(): Boolean = false
            override fun getDeviceName(): String = "TC007"
        },
        TS001 {
            override fun isLine(): Boolean = true
            override fun getDeviceName(): String = "TS001"
        },
        TS004 {
            override fun isLine(): Boolean = false
            override fun getDeviceName(): String = "TS004"
        };

        abstract fun isLine(): Boolean
        abstract fun getDeviceName(): String
    }
}
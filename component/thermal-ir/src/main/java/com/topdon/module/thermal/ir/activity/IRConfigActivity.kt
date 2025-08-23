package com.topdon.module.thermal.ir.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.NumberTools
import com.topdon.lib.core.tools.UnitTools
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.repository.TC007Repository
import com.topdon.lib.core.socket.WebSocketProxy
import com.topdon.lib.ui.widget.MyItemDecoration
import com.topdon.lms.sdk.weiget.TToast
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.adapter.ConfigEmAdapter
import com.topdon.module.thermal.ir.bean.DataBean
import com.topdon.module.thermal.ir.bean.ModelBean
import com.topdon.module.thermal.ir.dialog.ConfigGuideDialog
import com.topdon.module.thermal.ir.dialog.IRConfigInputDialog
import com.topdon.module.thermal.ir.repository.ConfigRepository
import com.topdon.module.thermal.ir.viewmodel.IRConfigViewModel
import kotlinx.android.synthetic.main.activity_ir_config.*
import kotlinx.android.synthetic.main.item_ir_config_config.view.*
import kotlinx.android.synthetic.main.item_ir_config_foot.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Route(path = RouterConfig.IR_SETTING)
class IRConfigActivity : BaseActivity(), View.OnClickListener {

    private var isTC007 = false

    private val viewModel: IRConfigViewModel by viewModels()

    private lateinit var adapter: ConfigAdapter

    override fun initContentView(): Int = R.layout.activity_ir_config

    @SuppressLint("SetTextI18n")
    override fun initView() {
        isTC007 = intent.getBooleanExtra(ExtraKeyConfig.IS_TC007, false)

        tv_default_temp_title.text = "${getString(R.string.thermal_config_environment)} ${UnitTools.showConfigC(-10, if (isTC007) 50 else 55)}"
        tv_default_dis_title.text = "${getString(R.string.thermal_config_distance)} (0.2~${if (isTC007) 4 else 5}m)"
        tv_default_em_title.text = "${getString(R.string.thermal_config_radiation)} (${if (isTC007) "0.1" else "0.01"}~1.00)"
        tv_default_temp_unit.text = UnitTools.showUnit()

        iv_default_selector.setOnClickListener(this)
        view_default_temp_bg.setOnClickListener(this)
        view_default_dis_bg.setOnClickListener(this)
        tv_default_em_value.setOnClickListener(this)

        adapter = ConfigAdapter(this, isTC007)
        adapter.onSelectListener = {
            viewModel.checkConfig(isTC007, it)
        }
        adapter.onDeleteListener = {
            TipDialog.Builder(this)
                .setMessage(getString(R.string.tip_config_delete, "${getString(R.string.thermal_custom_mode)}${it.name}"))
                .setPositiveListener(R.string.app_confirm) {
                    viewModel.deleteConfig(isTC007, it.id)
                }
                .setCancelListener(R.string.app_cancel)
                .create().show()
        }
        adapter.onUpdateListener = {
            viewModel.updateCustom(isTC007, it)
        }
        adapter.onAddListener = View.OnClickListener {
            TipDialog.Builder(this)
                .setMessage(R.string.tip_myself_model)
                .setPositiveListener(R.string.app_confirm) {
                    viewModel.addConfig(isTC007)
                }
                .setCancelListener(R.string.app_cancel)
                .create().show()
        }

        val itemDecoration = MyItemDecoration(this)
        itemDecoration.wholeBottom = 20f

        recycler_view.addItemDecoration(itemDecoration)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = ConcatAdapter(adapter, ConfigEmAdapter(this))

        viewModel.configLiveData.observe(this) {
            tv_default_temp_value.text = NumberTools.to02(UnitTools.showUnitValue(it.defaultModel.environment))
            tv_default_dis_value.text = NumberTools.to02(it.defaultModel.distance)
            tv_default_em_value.text = NumberTools.to02(it.defaultModel.radiation)
            iv_default_selector.isSelected = true

            showGuideDialog(it)

            if (isTC007 && WebSocketProxy.getInstance().isTC007Connect()) {
                lifecycleScope.launch {
                    val config = ConfigRepository.readConfig(true)
                    TC007Repository.setIRConfig(config.environment, config.distance, config.radiation)
                }
            }
        }
        viewModel.getConfig(isTC007)
    }

    override fun initData() {
    }

    private fun showGuideDialog(modelBean: ModelBean) {
        if (SharedManager.configGuideStep == 0) {
            iv_default_selector.isSelected = modelBean.defaultModel.use
            adapter.refresh(modelBean.myselfModel)
            return
        }
        val guideDialog = ConfigGuideDialog(this, isTC007, modelBean.defaultModel)
        guideDialog.setOnDismissListener {
            if (Build.VERSION.SDK_INT >= 31) {
                window?.decorView?.setRenderEffect(null)
            }
            iv_default_selector.isSelected = modelBean.defaultModel.use
            adapter.refresh(modelBean.myselfModel)
        }
        guideDialog.show()

        if (Build.VERSION.SDK_INT >= 31) {
            window?.decorView?.setRenderEffect(RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.MIRROR))
        } else {
            lifecycleScope.launch {
                delay(100)
                guideDialog.blurBg(ll_root)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_default_selector -> {
                viewModel.checkConfig(isTC007, 0)
            }
            view_default_temp_bg -> {
                IRConfigInputDialog(this, IRConfigInputDialog.Type.TEMP, isTC007)
                    .setInput(UnitTools.showUnitValue(viewModel.configLiveData.value?.defaultModel?.environment!!))
                    .setConfirmListener {
                        viewModel.updateDefaultEnvironment(isTC007, UnitTools.showToCValue(it))
                    }
                    .show()
            }
            view_default_dis_bg -> {
                IRConfigInputDialog(this, IRConfigInputDialog.Type.DIS, isTC007)
                    .setInput(viewModel.configLiveData.value?.defaultModel?.distance)
                    .setConfirmListener {
                        viewModel.updateDefaultDistance(isTC007, it)
                    }
                    .show()
            }
            tv_default_em_value -> {
                IRConfigInputDialog(this, IRConfigInputDialog.Type.EM, isTC007)
                    .setInput(viewModel.configLiveData.value?.defaultModel?.radiation)
                    .setConfirmListener {
                        viewModel.updateDefaultRadiation(isTC007, it)
                    }
                    .show()
            }
        }
    }

    private class ConfigAdapter(val context: Context, val isTC007: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        private val dataList: ArrayList<DataBean> = ArrayList()

        var onSelectListener: ((id: Int) -> Unit)? = null
        
        var onDeleteListener: ((bean: DataBean) -> Unit)? = null
        
        var onUpdateListener: ((bean: DataBean) -> Unit)? = null

        var onAddListener: View.OnClickListener? = null

        @SuppressLint("NotifyDataSetChanged")
        fun refresh(newList: List<DataBean>) {
            dataList.clear()
            dataList.addAll(newList)
            notifyDataSetChanged()
        }

        override fun getItemViewType(position: Int): Int {
            return if (position < dataList.size) 0 else 1
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == 0) {
                ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.item_ir_config_config, parent, false))
            } else {
                FootViewHolder(LayoutInflater.from(context).inflate(R.layout.item_ir_config_foot, parent, false))
            }
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ItemViewHolder) {
                val dataBean = dataList[position]
                holder.itemView.tv_name.text = "${context.getString(R.string.thermal_custom_mode)}${dataBean.name}"
                holder.itemView.iv_selector.isSelected = dataBean.use

                holder.itemView.tv_temp_title.text = "${context.getString(R.string.thermal_config_environment)} ${UnitTools.showConfigC(-10, if (isTC007) 50 else 55)}"
                holder.itemView.tv_dis_title.text = "${context.getString(R.string.thermal_config_distance)} (0.2~${if (isTC007) 4 else 5}m)"
                holder.itemView.tv_em_title.text = "${context.getString(R.string.thermal_config_radiation)} (${if (isTC007) "0.1" else "0.01"}~1.00)"
                holder.itemView.tv_temp_unit.text = UnitTools.showUnit()

                holder.itemView.tv_temp_value.text = NumberTools.to02(UnitTools.showUnitValue(dataBean.environment))
                holder.itemView.tv_dis_value.text = NumberTools.to02(dataBean.distance)
                holder.itemView.tv_em_value.text = NumberTools.to02(dataBean.radiation)
            } else if (holder is FootViewHolder) {
                holder.itemView.tv_add.setTextColor(if (dataList.size >= 10) 0x80ffffff.toInt() else 0xccffffff.toInt())
            }
        }

        override fun getItemCount(): Int = dataList.size + 1

        inner class ItemViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
            init {
                rootView.iv_selector.setOnClickListener {
                    val position: Int = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onSelectListener?.invoke(dataList[position].id)
                    }
                }
                rootView.iv_del.setOnClickListener {
                    val position: Int = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onDeleteListener?.invoke(dataList[position])
                    }
                }
                rootView.view_temp_bg.setOnClickListener {
                    val position: Int = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        IRConfigInputDialog(context, IRConfigInputDialog.Type.TEMP, isTC007)
                            .setInput(UnitTools.showUnitValue(dataList[position].environment))
                            .setConfirmListener {
                                itemView.tv_temp_value.text = NumberTools.to02(UnitTools.showToCValue(it))
                                dataList[position].environment = UnitTools.showToCValue(it)
                                onUpdateListener?.invoke(dataList[position])
                            }
                            .show()
                    }
                }
                rootView.view_dis_bg.setOnClickListener {
                    val position: Int = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        IRConfigInputDialog(context, IRConfigInputDialog.Type.DIS, isTC007)
                            .setInput(dataList[position].distance)
                            .setConfirmListener {
                                itemView.tv_dis_value.text = it.toString()
                                dataList[position].distance = it
                                onUpdateListener?.invoke(dataList[position])
                            }
                            .show()
                    }
                }
                rootView.tv_em_value.setOnClickListener {
                    val position: Int = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        IRConfigInputDialog(context, IRConfigInputDialog.Type.EM, isTC007)
                            .setInput(dataList[position].radiation)
                            .setConfirmListener {
                                itemView.tv_em_value.text = it.toString()
                                dataList[position].radiation = it
                                onUpdateListener?.invoke(dataList[position])
                            }
                            .show()
                    }
                }
            }
        }

        inner class FootViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
            init {
                rootView.view_add.setOnClickListener {
                    if (dataList.size < 10) {
                        val position: Int = bindingAdapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            onAddListener?.onClick(it)
                        }
                    } else {
                        TToast.shortToast(context, R.string.config_add_tip)
                    }
                }
                rootView.tv_all_emissivity.setOnClickListener {
                    context.startActivity(Intent(context, IREmissivityActivity::class.java))
                }
            }
        }
    }
}
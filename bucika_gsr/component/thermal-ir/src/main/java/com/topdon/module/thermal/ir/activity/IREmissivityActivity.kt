package com.topdon.module.thermal.ir.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.tools.UnitTools
import com.topdon.lib.ui.widget.MyItemDecoration
import com.topdon.module.thermal.ir.R
import com.topdon.module.thermal.ir.view.EmissivityView
import kotlinx.android.synthetic.main.activity_ir_emissivity.*
import kotlinx.android.synthetic.main.item_ir_emissivity_title.*
import kotlinx.android.synthetic.main.item_ir_emissivity_title.view.*

/**
 * 常用材料发射率.
 *
 * Created by LCG on 2024/10/14.
 */
class IREmissivityActivity : BaseActivity() {

    override fun initContentView(): Int = R.layout.activity_ir_emissivity

    override fun initView() {
        val dataArray: Array<ItemBean> = buildDataArray()
        tv_title.text = dataArray[0].name
        emissivity_view.refreshText(dataArray[0].buildTextList(this))

        val itemDecoration = MyItemDecoration(this)
        itemDecoration.wholeBottom = 20f

        val layoutManager = LinearLayoutManager(this)
        recycler_view.layoutManager = layoutManager
        recycler_view.adapter = MyAdapter(this, dataArray)
        recycler_view.addItemDecoration(itemDecoration)
        recycler_view.addOnScrollListener(MyOnScrollListener(cl_title, layoutManager, dataArray))
    }

    override fun initData() {
    }


    private class MyOnScrollListener(val titleView: View, val layoutManager: LinearLayoutManager, val dataArray: Array<ItemBean>) : RecyclerView.OnScrollListener() {
        /**
         * 当前展示的标题在列表中的 position
         */
        private var currentPosition: Int = 0

        /**
         * 标题文字
         */
        private val tvTitle: TextView = titleView.findViewById(R.id.tv_title)
        
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val seeFirstPosition = layoutManager.findFirstVisibleItemPosition()
            if (seeFirstPosition == RecyclerView.NO_POSITION) {
                return
            }
            if (seeFirstPosition == currentPosition) {
                return
            }

            if (dataArray[seeFirstPosition].isTitle) {//往上顶，将下一目录的标题顶到顶部了
                currentPosition = seeFirstPosition
                tvTitle.text = dataArray[currentPosition].name
                titleView.translationY = 0f
            } else {
                //在可见范围内查找当前目录最后一个项目
                val seeLastPosition = layoutManager.findLastVisibleItemPosition()
                var nextTitlePosition = -1
                for (i in seeFirstPosition .. seeLastPosition) {
                    if (dataArray[i].isTitle) {
                        nextTitlePosition = i
                        break
                    }
                }
                if (nextTitlePosition < 0) {
                    //当滑得非常快时，dataArray[seeFirstPosition].isTitle == true 判断分支未必触发，这里兜底
                    currentPosition = findTitlePosition(seeFirstPosition)
                    tvTitle.text = dataArray[currentPosition].name
                    titleView.translationY = 0f
                } else {
                    val nextTitleView: View = layoutManager.findViewByPosition(nextTitlePosition) ?: return
                    if (nextTitleView.top <= titleView.height) {
                        currentPosition = findTitlePosition(seeFirstPosition)
                        tvTitle.text = dataArray[currentPosition].name
                        titleView.translationY = -(titleView.height - nextTitleView.top).toFloat()
                    } else {
                        titleView.translationY = 0f
                    }
                }
            }
        }

        /**
         * 从指定 position 处，往上遍历查找该 position 对应的 title position.
         */
        private fun findTitlePosition(position: Int): Int {
            for (i in position downTo 0) {
                if (dataArray[i].isTitle) {
                    return i
                }
            }
            return 0
        }
    }

    private class MyAdapter(val context: Context, val dataArray: Array<ItemBean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun getItemViewType(position: Int): Int = if (dataArray[position].isTitle) 0 else 1

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == 0) {//标题
                TitleViewHolder(LayoutInflater.from(context).inflate(R.layout.item_ir_emissivity_title, parent, false))
            } else {//内容
                val emissivityView = EmissivityView(context)
                emissivityView.setPadding(SizeUtils.dp2px(12f), 0, SizeUtils.dp2px(12f), 0)
                ValueViewHolder(emissivityView)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val itemBean: ItemBean = dataArray[position]
            if (holder is TitleViewHolder) {
                holder.itemView.tv_title.text = itemBean.name
                holder.itemView.emissivity_view.isAlignTop = true
                holder.itemView.emissivity_view.drawTopLine = true
                holder.itemView.emissivity_view.refreshText(itemBean.buildTextList(context))
            } else if (holder is ValueViewHolder) {
                holder.emissivityView.refreshText(itemBean.buildTextList(context))
            }
        }

        override fun getItemCount(): Int = dataArray.size


        private class TitleViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView)

        private class ValueViewHolder(val emissivityView: EmissivityView) : RecyclerView.ViewHolder(emissivityView)
    }

    /**
     * 一项发射率数据封装
     * @param isTitle true-标题 false-内容
     * @param name 名称，如铝、氧化钢等
     * @param minTemp 最低温度，单位摄氏度
     * @param maxTemp 最高温度，单位摄氏度
     * @param emStr 发射率文字
     */
    private data class ItemBean(
        val isTitle: Boolean = false,
        val name: String,
        val minTemp: Int? = null,
        val maxTemp: Int? = null,
        val emStr: String? = null,
    ) {
        private var textList: ArrayList<String> = ArrayList(3)

        fun buildTextList(context: Context): ArrayList<String> {
            if (textList.isEmpty()) {
                if (isTitle) {
                    textList.add(context.getString(R.string.material_label))
                    textList.add(context.getString(R.string.material_temp, UnitTools.showUnit()))
                    textList.add(context.getString(R.string.thermal_config_radiation))
                } else {
                    textList.add(name)
                    if (minTemp != null || maxTemp != null) {
                        if (minTemp == null || maxTemp == null || minTemp == maxTemp) {
                            textList.add(UnitTools.showNoUnit((minTemp ?: maxTemp)!!.toFloat()))
                        } else {
                            textList.add(UnitTools.showNoUnit(minTemp.toFloat()) + "~" + UnitTools.showNoUnit(maxTemp.toFloat()))
                        }
                    } else {
                        if (emStr != null) {
                            textList.add("-")
                        }
                    }
                    if (emStr != null) {
                        textList.add(emStr)
                    }
                }
            }
            return textList
        }
    }

    private fun buildDataArray(): Array<ItemBean> = arrayOf(
        //金属
        ItemBean(true, getString(R.string.material_metal)),
        //铝
        ItemBean(name = getString(R.string.material_aluminum)),
        ItemBean(name = getString(R.string.material_polished_aluminum), minTemp = 100, emStr = "0.09"),
        ItemBean(name = getString(R.string.material_comm_aluminum_foil), minTemp = 100, emStr = "0.09"),
        ItemBean(name = getString(R.string.material_mild_alumina), minTemp = 25, maxTemp = 600, emStr = "0.10～0.20"),
        ItemBean(name = getString(R.string.material_alumina), minTemp = 25, maxTemp = 600, emStr = "0.30～0.40"),
        //黄铜
        ItemBean(name = getString(R.string.material_brass)),
        ItemBean(name = getString(R.string.material_bronze_mirror), minTemp = 28, emStr = "0.03"),
        ItemBean(name = getString(R.string.material_oxide), minTemp = 200, maxTemp = 600, emStr = "0.59～0.61"),
        //铬
        ItemBean(name = getString(R.string.material_chromium)),
        ItemBean(name = getString(R.string.material_polished_chromium), minTemp = 40, maxTemp = 1090, emStr = "0.08～0.36"),
        //铜
        ItemBean(name = getString(R.string.material_copper)),
        ItemBean(name = getString(R.string.material_bronze_mirror_1), minTemp = 100, emStr = "0.05"),
        ItemBean(name = getString(R.string.material_copper_oxide), minTemp = 25, emStr = "0.078"),
        ItemBean(name = getString(R.string.material_oxide_bronze), minTemp = 800, maxTemp = 1100, emStr = "0.66～0.54"),
        ItemBean(name = getString(R.string.material_bronze_water), minTemp = 1080, maxTemp = 1280, emStr = "0.16～0.13"),
        //金
        ItemBean(name = getString(R.string.material_gold)),
        ItemBean(name = getString(R.string.material_golden_mirror), minTemp = 230, maxTemp = 630, emStr = "0.02"),
        //铁
        ItemBean(name = getString(R.string.material_iron)),
        ItemBean(name = getString(R.string.material_polished_cast_iron), minTemp = 200, emStr = "0.21"),
        ItemBean(name = getString(R.string.material_process_cast_iron), minTemp = 20, emStr = "0.44"),
        ItemBean(name = getString(R.string.material_full_rusty_surface), minTemp = 20, emStr = "0.69"),
        ItemBean(name = getString(R.string.material_cast_iron_oxidation, UnitTools.showWithUnit(600f)), minTemp = 19, maxTemp = 600, emStr = "0.64～0.78"),
        ItemBean(name = getString(R.string.material_e_iron_oxide), minTemp = 125, maxTemp = 520, emStr = "0.78～0.82"),
        ItemBean(name = getString(R.string.material_iron_oxide), minTemp = 500, maxTemp = 1200, emStr = "0.85～0.89"),
        ItemBean(name = getString(R.string.material_iron_plate), minTemp = 925, maxTemp = 1120, emStr = "0.87～0.95"),
        ItemBean(name = getString(R.string.material_cast_iron_oxygen), minTemp = 25, emStr = "0.8"),
        ItemBean(name = getString(R.string.material_melt_surface), minTemp = 22, emStr = "0.94"),
        ItemBean(name = getString(R.string.material_melt_cast_iron), minTemp = 1300, maxTemp = 1400, emStr = "0.29"),
        ItemBean(name = getString(R.string.material_pure_iron), minTemp = 1515, maxTemp = 1680, emStr = "0.42～0.45"),
        //钢
        ItemBean(name = getString(R.string.material_steel)),
        ItemBean(name = getString(R.string.material_steel_1, UnitTools.showWithUnit(600f))),
        ItemBean(name = getString(R.string.material_oxide_steel), minTemp = 100, emStr = "0.74"),
        ItemBean(name = getString(R.string.material_metrot_low_carbon_steel), minTemp = 1600, maxTemp = 1800, emStr = "0.28"),
        ItemBean(name = getString(R.string.material_steel_water), minTemp = 1500, maxTemp = 1650, emStr = "0.42～0.53"),
        //铅
        ItemBean(name = getString(R.string.material_lead)),
        ItemBean(name = getString(R.string.material_pure_lead), minTemp = 125, maxTemp = 225, emStr = "0.06～0.08"),
        ItemBean(name = getString(R.string.material_mild_oxidation_lead), minTemp = 25, maxTemp = 300, emStr = "0.20～0.45"),
        //镁
        ItemBean(name = getString(R.string.material_magnesium)),
        ItemBean(name = getString(R.string.material_magnesium_oxide), minTemp = 275, maxTemp = 825, emStr = "0.55～0.20"),
        //汞
        ItemBean(name = getString(R.string.material_mercury)),
        ItemBean(name = getString(R.string.material_mercury), minTemp = 0, maxTemp = 100, emStr = "0.09～0.12"),
        //镍
        ItemBean(name = getString(R.string.material_nickel)),
        ItemBean(name = getString(R.string.material_plating_polished_nickel), minTemp = 25, emStr = "0.05"),
        ItemBean(name = getString(R.string.material_nickel_not_polished), minTemp = 20, emStr = "0.01"),
        ItemBean(name = getString(R.string.material_nickel_wire), minTemp = 185, maxTemp = 1010, emStr = "0.09～0.19"),
        ItemBean(name = getString(R.string.material_nickel_plate_oxidized), minTemp = 198, maxTemp = 600, emStr = "0.37～0.48"),
        ItemBean(name = getString(R.string.material_nickel_oxide), minTemp = 650, maxTemp = 1255, emStr = "0.59～0.86"),
        //镍合金
        ItemBean(name = getString(R.string.material_nickel_alloy)),
        ItemBean(name = getString(R.string.material_nickel_chromium_alloy_line), minTemp = 50, maxTemp = 1000, emStr = "0.65～0.79"),
        ItemBean(name = getString(R.string.material_nickel_chromium_alloy), minTemp = 50, maxTemp = 1040, emStr = "0.64～0.76"),
        ItemBean(name = getString(R.string.material_nickel_chromium_heat_resistance), minTemp = 50, maxTemp = 500, emStr = "0.95～0.98"),
        //银
        ItemBean(name = getString(R.string.material_silver)),
        ItemBean(name = getString(R.string.material_polished_silver), minTemp = 100, emStr = "0.05"),
        //不锈钢
        ItemBean(name = getString(R.string.material_stainless_steel)),
        ItemBean(name = getString(R.string.material_eight_stainless_steel), minTemp = 25, emStr = "0.16"),
        ItemBean(name = "304（8Cr,18Ni）", minTemp = 215, maxTemp = 490, emStr = "0.44～0.36"),
        ItemBean(name = "310（25Cr,20Ni）", minTemp = 215, maxTemp = 520, emStr = "0.90～0.97"),
        //锡
        ItemBean(name = getString(R.string.material_tin)),
        ItemBean(name = getString(R.string.material_commercial_tin), minTemp = 100, emStr = "0.07"),
        //锌
        ItemBean(name = getString(R.string.material_zinc)),
        ItemBean(name = getString(R.string.material_400c_zinc_oxide, UnitTools.showWithUnit(400f)), minTemp = 400, emStr = "0.01"),
        ItemBean(name = getString(R.string.material_galvanized_brighter_iron_board), minTemp = 28, emStr = "0.23"),
        ItemBean(name = getString(R.string.material_gray_zinc_oxide), minTemp = 25, emStr = "0.28"),

        //非金属
        ItemBean(true, getString(R.string.material_nonMetal)),
        ItemBean(name = getString(R.string.material_brick), minTemp = 1100, emStr = "0.75"),
        ItemBean(name = getString(R.string.material_fire_brick), minTemp = 1100, emStr = "0.75"),
        ItemBean(name = getString(R.string.material_graphite_black), minTemp = 96, maxTemp = 225, emStr = "0.95"),
        ItemBean(name = getString(R.string.material_enamel_white), minTemp = 18, emStr = "0.9"),
        ItemBean(name = getString(R.string.material_asphalt), minTemp = 0, maxTemp = 200, emStr = "0.85"),
        ItemBean(name = getString(R.string.material_glass_surface), minTemp = 23, emStr = "0.94"),
        ItemBean(name = getString(R.string.material_heat_resistant_glass), minTemp = 200, maxTemp = 540, emStr = "0.85～0.95"),
        ItemBean(name = getString(R.string.material_wall_powder), minTemp = 20, emStr = "0.9"),
        ItemBean(name = getString(R.string.material_oak), minTemp = 20, emStr = "0.9"),
        ItemBean(name = getString(R.string.material_carbon_slice), emStr = "0.85"),
        ItemBean(name = getString(R.string.material_insulating_tablet), emStr = "0.91～0.94"),
        ItemBean(name = getString(R.string.material_metal_piece), emStr = "0.88～0.90"),
        ItemBean(name = getString(R.string.material_glass_tube), emStr = "0.9"),
        ItemBean(name = getString(R.string.material_coil), emStr = "0.87"),
        ItemBean(name = getString(R.string.material_enamel_product), emStr = "0.9"),
        ItemBean(name = getString(R.string.material_enamel_pattern), emStr = "0.83～0.95"),
        //电容器
        ItemBean(name = getString(R.string.material_capacitor)),
        ItemBean(name = getString(R.string.material_rotating_capacitor), emStr = "0.30～0.34"),
        ItemBean(name = getString(R.string.material_ceramic_bottle_capacitor), emStr = "0.9"),
        ItemBean(name = getString(R.string.material_film_capacitance), emStr = "0.90～0.93"),
        ItemBean(name = getString(R.string.material_mica_capacitor), emStr = "0.94～0.95"),
        ItemBean(name = getString(R.string.material_lighting_groove_mica_capacitor), emStr = "0.90～0.93"),
        ItemBean(name = getString(R.string.material_glass_capacitor), emStr = "0.91～0.92"),
        //半导体
        ItemBean(name = getString(R.string.material_semiconductor)),
        ItemBean(name = getString(R.string.material_crystal_tube_plastic_seal), emStr = "0.80～0.90"),
        ItemBean(name = getString(R.string.material_crystal_tube_metal), emStr = "0.30～0.40"),
        ItemBean(name = getString(R.string.material_diode), emStr = "0.89～0.90"),
        //传输线圈
        ItemBean(name = getString(R.string.material_transmission_coil)),
        ItemBean(name = getString(R.string.material_pulse_transmission_coil), emStr = "0.91～0.92"),
        ItemBean(name = getString(R.string.material_flat_white_layer_coil), emStr = "0.88～0.93"),
        ItemBean(name = getString(R.string.material_top_coil), emStr = "0.91～0.92"),
        //电子材料
        ItemBean(name = getString(R.string.material_electronic)),
        ItemBean(name = getString(R.string.material_epoxy_glass_board), emStr = "0.86"),
        ItemBean(name = getString(R.string.material_epoxy_phenol_board), emStr = "0.8"),
        ItemBean(name = getString(R.string.material_gold_plated_copper), emStr = "0.3"),
        ItemBean(name = getString(R.string.material_copper_with_welded_welds), emStr = "0.35"),
        ItemBean(name = getString(R.string.material_tin_coated_lead_wire), emStr = "0.28"),
        ItemBean(name = getString(R.string.material_copper_wire), emStr = "0.87～0.88"),
    )
}
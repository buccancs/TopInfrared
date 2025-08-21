package com.topdon.lib.core.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.topdon.lib.core.BaseApplication
import com.topdon.lib.core.R
import com.topdon.lib.core.bean.CarDetectBean
import com.topdon.lib.core.bean.CarDetectChildBean
import com.topdon.lib.core.common.SharedManager
import kotlinx.android.synthetic.main.dialog_car_detect.view.*
import kotlinx.android.synthetic.main.item_car_detect_child_layout.view.*
import kotlinx.android.synthetic.main.item_car_detect_layout.view.*
import kotlinx.android.synthetic.main.toolbar_lay.view.*

/**
 * 汽车检测类型拾取弹框.
 */
class CarDetectDialog(context: Context, val listener: ((bean: CarDetectChildBean) -> Unit)) :
    Dialog(context, R.style.DefaultDialog) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCancelable(true)
        setCanceledOnTouchOutside(false)


        val rootView: View = LayoutInflater.from(context).inflate(R.layout.dialog_car_detect, null)
        setContentView(rootView)

        rootView.title_view.setLeftClickListener { dismiss() }

        rootView.rcy_detect.layoutManager =
            LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rootView.rcy_detect?.adapter = CarDetectAdapter(context, getDetectList())


        /*window?.let {
            val layoutParams = it.attributes
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            it.attributes = layoutParams
        }*/
    }

    companion object {
        @JvmStatic
        fun getDetectList(): MutableList<CarDetectBean> {
            val dataList: MutableList<CarDetectBean> = ArrayList()
            val data1List: MutableList<CarDetectChildBean> = ArrayList()
            val data2List: MutableList<CarDetectChildBean> = ArrayList()
            data1List.add(
                CarDetectChildBean(
                    0, 0,
                    BaseApplication.instance.getString(R.string.abnormal_description1),
                    BaseApplication.instance.getString(R.string.abnormal_item1),
                    "40~70"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 1,
                    BaseApplication.instance.getString(R.string.abnormal_description2),
                    BaseApplication.instance.getString(R.string.abnormal_item2),
                    "200~400"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 2,
                    BaseApplication.instance.getString(R.string.abnormal_description3),
                    BaseApplication.instance.getString(R.string.abnormal_item3),
                    "200~400"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 3,
                    BaseApplication.instance.getString(R.string.abnormal_description4),
                    BaseApplication.instance.getString(R.string.abnormal_item4),
                    "40~60"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 4,
                    BaseApplication.instance.getString(R.string.abnormal_description5),
                    BaseApplication.instance.getString(R.string.abnormal_item5),
                    "40~60"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 5,
                    BaseApplication.instance.getString(R.string.abnormal_description6),
                    BaseApplication.instance.getString(R.string.abnormal_item6),
                    "40~60"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 6,
                    BaseApplication.instance.getString(R.string.abnormal_description7),
                    BaseApplication.instance.getString(R.string.abnormal_item7),
                    "40~60"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 7,
                    BaseApplication.instance.getString(R.string.abnormal_description8),
                    BaseApplication.instance.getString(R.string.abnormal_item8),
                    "80~100"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 8,
                    BaseApplication.instance.getString(R.string.abnormal_description9),
                    BaseApplication.instance.getString(R.string.abnormal_item9),
                    "80~100"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 9,
                    BaseApplication.instance.getString(R.string.abnormal_description10),
                    BaseApplication.instance.getString(R.string.abnormal_item10),
                    "80~100"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 10,
                    BaseApplication.instance.getString(R.string.abnormal_description11),
                    BaseApplication.instance.getString(R.string.abnormal_item11),
                    "80~100"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 11,
                    BaseApplication.instance.getString(R.string.abnormal_description12),
                    BaseApplication.instance.getString(R.string.abnormal_item12),
                    "80~100"
                )
            )
            data1List.add(
                CarDetectChildBean(
                    0, 12,
                    BaseApplication.instance.getString(R.string.abnormal_description13),
                    BaseApplication.instance.getString(R.string.abnormal_item13),
                    "80~100"
                )
            )
            val carDetectBean1 = CarDetectBean(
                BaseApplication.instance.getString(R.string.abnormal_title1),
                data1List
            )
            data2List.add(
                CarDetectChildBean(
                    1, 0,
                    BaseApplication.instance.getString(R.string.abnormal_description14),
                    BaseApplication.instance.getString(R.string.abnormal_item14),
                    "20~50"
                )
            )
            data2List.add(
                CarDetectChildBean(
                    1, 1,
                    BaseApplication.instance.getString(R.string.abnormal_description15),
                    BaseApplication.instance.getString(R.string.abnormal_item15),
                    "20~50"
                )
            )
            data2List.add(
                CarDetectChildBean(
                    1, 2,
                    BaseApplication.instance.getString(R.string.abnormal_description16),
                    BaseApplication.instance.getString(R.string.abnormal_item16),
                    "20~50"
                )
            )
            data2List.add(
                CarDetectChildBean(
                    1, 3,
                    BaseApplication.instance.getString(R.string.abnormal_description17),
                    BaseApplication.instance.getString(R.string.abnormal_item17),
                    "20~50"
                )
            )
            data2List.add(
                CarDetectChildBean(
                    1, 4,
                    BaseApplication.instance.getString(R.string.abnormal_description18),
                    BaseApplication.instance.getString(R.string.abnormal_item18),
                    "20~50"
                )
            )
            val carDetectBean2 = CarDetectBean(
                BaseApplication.instance.getString(R.string.abnormal_title2),
                data2List
            )
            dataList.add(carDetectBean1)
            dataList.add(carDetectBean2)
            return dataList
        }
    }

    inner class CarDetectAdapter(val act: Context, private var carDetects: List<CarDetectBean>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_car_detect_layout, parent, false)
            return ItemView(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ItemView) {
                val bean = carDetects[position]
                holder.tvTitle.text = bean.title
                holder.rcyDetectChild.layoutManager =
                    LinearLayoutManager(act, RecyclerView.VERTICAL, false)
                val carDetectChildAdapter = CarDetectChildAdapter(act, bean.detectChildBeans)
                carDetectChildAdapter.listener = listener@{ _, item ->
                    carDetects.forEach { it ->
                        it.detectChildBeans.forEach {
                            it.isSelected = false
                        }
                    }
                    item.isSelected = true
                    carDetectChildAdapter.notifyDataSetChanged()
                    SharedManager.saveCarDetectInfo(item)
                    listener.invoke(item)
                    dismiss()
                }

                var selectCarDetect = SharedManager.getCarDetectInfo()
                carDetects.forEachIndexed { index, carDetectBean ->
                    carDetectBean.detectChildBeans.forEachIndexed { childIndex, carDetectChildBean ->
                        if (selectCarDetect == null) {
                            carDetectChildBean.isSelected = (index == 0 && childIndex == 0)
                        } else {
                            carDetectChildBean.isSelected =
                                TextUtils.equals(carDetectChildBean.item, selectCarDetect.item)
                        }
                    }
                }

                holder.rcyDetectChild?.adapter = carDetectChildAdapter
            }
        }

        override fun getItemCount(): Int {
            return carDetects.size
        }

        inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvTitle: TextView = itemView.tv_title
            val rcyDetectChild: RecyclerView = itemView.rcy_detect_child
        }
    }


    class CarDetectChildAdapter(
        val context: Context,
        private var carChildDetects: List<CarDetectChildBean>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var listener: ((index: Int, bean: CarDetectChildBean) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_car_detect_child_layout, parent, false)
            return ItemView(view)
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ItemView) {
                val bean = carChildDetects[position]
                holder.tvTitle.text = bean.item
                holder.viewLine.visibility =
                    if (position == carChildDetects.size - 1) View.GONE else View.VISIBLE
                holder.ivSelectState.setImageResource(if (bean.isSelected) R.drawable.ic_car_detect_selected else R.drawable.ic_car_detect_unselected)
                holder.rlyParent.setOnClickListener {
                    listener?.invoke(position, carChildDetects[position])
                }
            }
        }

        override fun getItemCount(): Int {
            return carChildDetects.size
        }

        inner class ItemView(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val rlyParent: RelativeLayout = itemView.rly_parent
            val tvTitle: TextView = itemView.tv_name
            val ivSelectState: ImageView = itemView.iv_select_state
            val viewLine: View = itemView.view_line
        }
    }
}
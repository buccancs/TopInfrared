package com.topdon.house.activity

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.SizeUtils
import com.topdon.house.R
import com.topdon.house.event.DetectItemListEvent
import com.topdon.house.viewmodel.DetectViewModel
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.db.entity.DirDetect
import com.topdon.lib.core.db.entity.ItemDetect
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.ui.widget.MyItemDecoration
import com.topdon.lms.sdk.weiget.TToast
import kotlinx.android.synthetic.main.activity_item_edit.*
import kotlinx.android.synthetic.main.item_edit_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.util.Collections

@SuppressLint("NotifyDataSetChanged")
class ItemEditActivity : BaseActivity(), View.OnClickListener {
    private val adapter = MyAdapter(this)
    private val itemTouchCallback = MyItemTouchCallback()

    private val viewModel: DetectViewModel by viewModels()

    override fun initContentView(): Int = R.layout.activity_item_edit

    override fun initView() {
        iv_copy.isEnabled = false
        tv_copy.isEnabled = false
        iv_del.isEnabled = false
        tv_del.isEnabled = false
        view_copy.isEnabled = false
        view_del.isEnabled = false

        iv_exit.setOnClickListener(this)
        iv_save.setOnClickListener(this)
        cl_dir.setOnClickListener(this)
        view_select_all.setOnClickListener(this)
        view_copy.setOnClickListener(this)
        view_del.setOnClickListener(this)

        et_dir_name.addTextChangedListener {
            val dirDetect: DirDetect? = viewModel.dirLD.value
            if (dirDetect != null) {
                dirDetect.dirName = it?.toString() ?: ""
            }
        }

        adapter.onSelectChangeListener = {
            iv_copy.isEnabled = it > 0
            tv_copy.isEnabled = it > 0
            iv_del.isEnabled = it > 0
            tv_del.isEnabled = it > 0
            view_copy.isEnabled = it > 0
            view_del.isEnabled = it > 0
            iv_select_all.isSelected = adapter.isSelectAll
            tv_select_all.setText(if (adapter.isSelectAll) R.string.app_cancel_select_all else R.string.report_select_all)
            tv_title.text = if (it > 0) getString(R.string.chosen_item, it) else getString(R.string.not_selected)
        }
        adapter.onStateChangeListener = { oldState, newState ->
            val dirDetect: DirDetect? = viewModel.dirLD.value
            if (dirDetect != null) {
                when (oldState) {
                    1 -> dirDetect.goodCount--
                    2 -> dirDetect.warnCount--
                    3 -> dirDetect.dangerCount--
                }
                when (newState) {
                    1 -> dirDetect.goodCount++
                    2 -> dirDetect.warnCount++
                    3 -> dirDetect.dangerCount++
                }
                tv_good_count.text = dirDetect.getGoodCountStr()
                tv_warn_count.text = dirDetect.getWarnCountStr()
                tv_danger_count.text = dirDetect.getDangerCountStr()
            }
        }
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
        recycler_view.addItemDecoration(MyItemDecoration(this).apply { wholeBottom = 16f })
        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(recycler_view)

        viewModel.dirLD.observe(this) {
            if (it != null) {
                et_dir_name.setText(it.dirName)
                tv_good_count.text = it.getGoodCountStr()
                tv_warn_count.text = it.getWarnCountStr()
                tv_danger_count.text = it.getDangerCountStr()
                adapter.refresh(it.itemList)
                itemTouchCallback.refresh(it.itemList)
            }
        }
        viewModel.queryDirById(intent.getLongExtra(ExtraKeyConfig.DIR_ID, 0))

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitTipsDialog()
            }
        })
    }

    override fun initData() {
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_exit -> showExitTipsDialog()
            iv_save -> {
                val dirDetect: DirDetect = viewModel.dirLD.value ?: return
                showLoadingDialog()
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getInstance().houseDetectDao().refreshDir(dirDetect)
                    withContext(Dispatchers.Main) {
                        TToast.shortToast(this@ItemEditActivity, R.string.tip_save_success)
                        dismissLoadingDialog()
                        EventBus.getDefault().post(DetectItemListEvent(dirDetect.id))
                        delay(100)
                        finish()
                    }
                }
            }
            cl_dir -> {
                adapter.isExpand = !adapter.isExpand
                if (adapter.isExpand) {
                    iv_triangle.setImageResource(R.drawable.svg_house_triangle_up)
                    cl_dir.setBackgroundResource(R.drawable.bg_corners10_top_solid_23202e)
                } else {
                    iv_triangle.setImageResource(R.drawable.svg_house_triangle_down)
                    cl_dir.setBackgroundResource(R.drawable.bg_corners10_solid_23202e)
                }
            }
            view_select_all -> {
                adapter.isSelectAll = !adapter.isSelectAll
            }
            view_copy -> {
                adapter.copySelect()
                TToast.shortToast(this@ItemEditActivity, R.string.ts004_copy_success)
            }
            view_del -> {
                TipDialog.Builder(this)
                    .setTitleMessage(getString(R.string.tips_del_item_title))
                    .setMessage(R.string.tips_del_item_content)
                    .setCancelListener(R.string.app_cancel) {
                    }
                    .setPositiveListener(R.string.report_delete) {
                        cl_empty.isVisible = adapter.isSelectAll
                        cl_bottom.isVisible = !adapter.isSelectAll
                        cl_dir.isVisible = !adapter.isSelectAll
                        adapter.delSelect()
                        TToast.shortToast(this@ItemEditActivity, R.string.test_results_delete_success)
                    }
                    .create().show()
            }
        }
    }

    private fun showExitTipsDialog() {
        TipDialog.Builder(this)
            .setMessage(R.string.diy_tip_save)
            .setPositiveListener(R.string.app_exit) {
                finish()
            }
            .setCancelListener(R.string.app_cancel)
            .create().show()
    }

    private class MyItemTouchCallback : ItemTouchHelper.Callback() {
        var dataList: ArrayList<ItemDetect> = ArrayList(0)

        fun refresh(newList: ArrayList<ItemDetect>) {
            dataList = newList
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = viewHolder.bindingAdapterPosition
            val toPosition = target.bindingAdapterPosition

            if (fromPosition == dataList.size - 1 || toPosition == dataList.size - 1) {
                if (viewHolder is MyAdapter.ViewHolder) {
                    viewHolder.refreshIsLast(toPosition == dataList.size - 1)
                }
                if (target is MyAdapter.ViewHolder) {
                    target.refreshIsLast(fromPosition == dataList.size - 1)
                }
            }

            if (fromPosition < toPosition) {
                for (i in fromPosition until toPosition) {
                    Collections.swap(dataList, i, i + 1)
                }
            } else {
                for (i in fromPosition downTo toPosition + 1) {
                    Collections.swap(dataList, i, i - 1)
                }
            }
            recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }
    }

    private class MyAdapter(val context: Context) : RecyclerView.Adapter<MyAdapter.ViewHolder>() {
        private var dataList: ArrayList<ItemDetect> = ArrayList(0)

        var isExpand = true
            set(value) {
                if (field != value) {
                    field = value
                    notifyDataSetChanged()
                }
            }

        private var selectCount = 0
        
        var isSelectAll: Boolean
            get() = selectCount == dataList.size && dataList.size > 0
            set(value) {
                if (value) {
                    selectCount = dataList.size
                    for (item in dataList) {
                        item.hasSelect = true
                    }
                } else {
                    selectCount = 0
                    for (item in dataList) {
                        item.hasSelect = false
                    }
                }
                onSelectChangeListener?.invoke(selectCount)
                notifyItemRangeChanged(0, itemCount)
            }

        var onSelectChangeListener: ((selectSize: Int) -> Unit)? = null
        
        var onStateChangeListener: ((oldState: Int, newState: Int) -> Unit)? = null

        fun refresh(newList: ArrayList<ItemDetect>) {
            dataList = newList
            notifyDataSetChanged()
        }

        fun delSelect() {
            selectCount = 0
            if (isSelectAll) {
                dataList.clear()
                notifyItemRangeRemoved(0, itemCount)
            } else {
                val isDelLast = dataList.last().hasSelect
                for (i in dataList.size - 1 downTo 0) {
                    if (dataList[i].hasSelect) {
                        val itemDetect: ItemDetect = dataList.removeAt(i)
                        notifyItemRemoved(i)
                        onStateChangeListener?.invoke(itemDetect.state, 0)
                    }
                }
                if (isDelLast) {
                    notifyItemChanged(dataList.size - 1)
                }
            }
            onSelectChangeListener?.invoke(0)
        }

        fun copySelect() {
            selectCount *= 2
            val selectIndexList: ArrayList<Int> = ArrayList()
            for (i in dataList.indices) {
                if (dataList[i].hasSelect) {
                    selectIndexList.add(i)
                }
            }
            val isCopyLast = dataList.last().hasSelect
            for ((hasAddCount, oldIndex) in selectIndexList.withIndex()) {
                val oldItem: ItemDetect = dataList[oldIndex + hasAddCount]
                val newItem: ItemDetect = oldItem.copyOne(itemName = oldItem.copyName())
                dataList.add(oldIndex + hasAddCount + 1, newItem)
                notifyItemInserted(oldIndex + hasAddCount + 1)
                if (oldItem.state > 0) {
                    onStateChangeListener?.invoke(0, oldItem.state)
                }
            }
            if (isCopyLast) {
                notifyItemChanged(dataList.size - 2)
            }
            onSelectChangeListener?.invoke(selectCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_edit_item, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val itemDetect: ItemDetect = dataList[position]
            holder.itemView.iv_select.isSelected = itemDetect.hasSelect
            holder.itemView.et_input_name.setText(itemDetect.itemName)
            holder.itemView.tv_good.isSelected = itemDetect.state == 1
            holder.itemView.tv_warn.isSelected = itemDetect.state == 2
            holder.itemView.tv_danger.isSelected = itemDetect.state == 3
            holder.itemView.tv_state.text = itemDetect.getStateStr(context)
            holder.refreshIsLast(position == dataList.size - 1)
        }

        override fun getItemCount(): Int = if (isExpand) dataList.size else 0

        inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
            init {
                rootView.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val item: ItemDetect = dataList[position]
                        item.hasSelect = !item.hasSelect
                        selectCount += if (item.hasSelect) 1 else -1
                        rootView.iv_select.isSelected = item.hasSelect
                        onSelectChangeListener?.invoke(selectCount)
                    }
                }
                rootView.et_input_name.addTextChangedListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        dataList[position].itemName = it?.toString() ?: ""
                    }
                }
                rootView.tv_good.setOnClickListener {
                    handleStateChange(1)
                }
                rootView.tv_warn.setOnClickListener {
                    handleStateChange(2)
                }
                rootView.tv_danger.setOnClickListener {
                    handleStateChange(3)
                }
            }

            fun refreshIsLast(isLastItem: Boolean) {
                if (isLastItem) {
                    itemView.setBackgroundResource(R.drawable.bg_corners10_bottom_solid_23202e)
                    itemView.setPadding(0, SizeUtils.dp2px(6f), 0, SizeUtils.dp2px(16f))
                } else {
                    itemView.setBackgroundColor(0xff23202e.toInt())
                    itemView.setPadding(0, SizeUtils.dp2px(6f), 0, SizeUtils.dp2px(6f))
                }
            }

            private fun handleStateChange(newState: Int) {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) {
                    return
                }
                val itemDetect: ItemDetect = dataList[position]
                if (itemDetect.state == newState) {
                    return
                }
                onStateChangeListener?.invoke(itemDetect.state, newState)
                itemDetect.state = newState

                itemView.tv_good.isSelected = itemDetect.state == 1
                itemView.tv_warn.isSelected = itemDetect.state == 2
                itemView.tv_danger.isSelected = itemDetect.state == 3
                itemView.tv_state.text = itemDetect.getStateStr(context)
            }
        }
    }
}
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
import com.topdon.house.R
import com.topdon.house.event.DetectDirListEvent
import com.topdon.house.viewmodel.DetectViewModel
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.db.entity.DirDetect
import com.topdon.lib.core.db.entity.HouseDetect
import com.topdon.lib.core.db.entity.ItemDetect
import com.topdon.lib.core.dialog.TipDialog
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lms.sdk.weiget.TToast
import kotlinx.android.synthetic.main.activity_dir_edit.*
import kotlinx.android.synthetic.main.item_edit_dir.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import java.util.Collections

@SuppressLint("NotifyDataSetChanged")
class DirEditActivity : BaseActivity(), View.OnClickListener {
    private val adapter = MyAdapter(this)
    private val itemTouchCallback = MyItemTouchCallback()

    private val viewModel: DetectViewModel by viewModels()

    override fun initContentView(): Int = R.layout.activity_dir_edit

    override fun initView() {
        iv_copy.isEnabled = false
        tv_copy.isEnabled = false
        iv_del.isEnabled = false
        tv_del.isEnabled = false
        view_copy.isEnabled = false
        view_del.isEnabled = false

        iv_exit.setOnClickListener(this)
        iv_save.setOnClickListener(this)
        view_select_all.setOnClickListener(this)
        view_copy.setOnClickListener(this)
        view_del.setOnClickListener(this)
        tv_add.setOnClickListener(this)

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
        recycler_view.setHasFixedSize(true)
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = adapter
        ItemTouchHelper(itemTouchCallback).attachToRecyclerView(recycler_view)

        viewModel.detectLD.observe(this) {
            if (it != null) {
                adapter.refresh(it.dirList)
                itemTouchCallback.refresh(it.dirList)
            }
        }
        viewModel.queryById(intent.getLongExtra(ExtraKeyConfig.DETECT_ID, 0))

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
                val houseDetect: HouseDetect = viewModel.detectLD.value ?: return
                showLoadingDialog()
                lifecycleScope.launch(Dispatchers.IO) {
                    AppDatabase.getInstance().houseDetectDao().refreshDetect(houseDetect)
                    withContext(Dispatchers.Main) {
                        TToast.shortToast(this@DirEditActivity, R.string.tip_save_success)
                        dismissLoadingDialog()
                        EventBus.getDefault().post(DetectDirListEvent(houseDetect.id))
                        delay(100)
                        finish()
                    }
                }
            }
            view_select_all -> {
                adapter.isSelectAll = !adapter.isSelectAll
            }
            view_copy -> {
                adapter.copySelect()
                TToast.shortToast(this@DirEditActivity, R.string.ts004_copy_success)
            }
            view_del -> {
                TipDialog.Builder(this)
                    .setTitleMessage(getString(R.string.tips_del_item_title))
                    .setMessage(R.string.tips_del_item_content)
                    .setCancelListener(R.string.app_cancel) {
                    }
                    .setPositiveListener(R.string.report_delete) {
                        adapter.delSelect()
                        recycler_view.isVisible = adapter.dataList.isNotEmpty()
                        cl_bottom.isVisible = adapter.dataList.isNotEmpty()
                        cl_empty.isVisible = adapter.dataList.isEmpty()
                        TToast.shortToast(this@DirEditActivity, R.string.test_results_delete_success)
                    }
                    .create().show()
            }
            tv_add -> {
                recycler_view.isVisible = true
                cl_bottom.isVisible = true
                cl_empty.isVisible = false
                val houseDetect: HouseDetect = viewModel.detectLD.value ?: return
                val dirList: ArrayList<DirDetect> = DirDetect.buildDefaultDirList(parentId = houseDetect.id)
                for (i in dirList.indices) {
                    val dir: DirDetect = dirList[i]
                    dir.itemList = ItemDetect.buildDefaultItemList(dir.id, i)
                }
                houseDetect.dirList = dirList
                viewModel.detectLD.value = houseDetect
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
        private var dataList: ArrayList<DirDetect> = ArrayList(0)

        fun refresh(newList: ArrayList<DirDetect>) {
            dataList = newList
        }

        override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
            return makeMovementFlags(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0)
        }

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            val fromPosition = viewHolder.bindingAdapterPosition
            val toPosition = target.bindingAdapterPosition
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
        var dataList: ArrayList<DirDetect> = ArrayList(0)

        private var selectCount = 0
        
        var isSelectAll: Boolean
            get() = selectCount == dataList.size && dataList.size > 0
            set(value) {
                if (value) {
                    selectCount = dataList.size
                    for (dir in dataList) {
                        dir.hasSelect = true
                    }
                } else {
                    selectCount = 0
                    for (dir in dataList) {
                        dir.hasSelect = false
                    }
                }
                onSelectChangeListener?.invoke(selectCount)
                notifyItemRangeChanged(0, itemCount)
            }

        var onSelectChangeListener: ((selectSize: Int) -> Unit)? = null

        fun refresh(newList: ArrayList<DirDetect>) {
            dataList = newList
            notifyDataSetChanged()
        }

        fun delSelect() {
            selectCount = 0
            if (isSelectAll) {
                dataList.clear()
                notifyItemRangeRemoved(0, itemCount)
            } else {
                for (i in dataList.size - 1 downTo 0) {
                    if (dataList[i].hasSelect) {
                        dataList.removeAt(i)
                        notifyItemRemoved(i)
                    }
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
            for ((hasAddCount, oldIndex) in selectIndexList.withIndex()) {
                val newDir: DirDetect = dataList[oldIndex + hasAddCount].copyOne()
                dataList.add(oldIndex + hasAddCount + 1, newDir)
                notifyItemInserted(oldIndex + hasAddCount + 1)
            }
            onSelectChangeListener?.invoke(selectCount)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_edit_dir, parent, false))
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.itemView.iv_select.isSelected = dataList[position].hasSelect
            holder.itemView.et_input_name.setText(dataList[position].dirName)
        }

        override fun getItemCount(): Int = dataList.size

        inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
            init {
                rootView.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val dir: DirDetect = dataList[position]
                        dir.hasSelect = !dir.hasSelect
                        selectCount += if (dir.hasSelect) 1 else -1
                        rootView.iv_select.isSelected = dir.hasSelect
                        onSelectChangeListener?.invoke(selectCount)
                    }
                }
                rootView.et_input_name.addTextChangedListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        dataList[position].dirName = it?.toString() ?: ""
                    }
                }
            }
        }
    }
}
package com.topdon.module.user.activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.topdon.lib.core.config.RouterConfig
import com.topdon.lib.core.ktbase.BaseActivity
import com.topdon.lib.core.utils.Constants
import com.topdon.module.user.R
import kotlinx.android.synthetic.main.activity_electronic_manual.*
import kotlinx.android.synthetic.main.item_electronic_manual.view.item_lay
import kotlinx.android.synthetic.main.item_electronic_manual.view.item_text

@Route(path = RouterConfig.ELECTRONIC_MANUAL)
class ElectronicManualActivity : BaseActivity() {

    override fun initContentView() = R.layout.activity_electronic_manual

    override fun initView() {
        val productType = intent.getIntExtra(Constants.SETTING_TYPE, 0)

        title_view.setTitleText(if (productType == Constants.SETTING_BOOK) R.string.electronic_manual else R.string.app_question)

        val adapter = MyAdapter(productType == 1)
        adapter.onPickListener = { isTS001 ->
            if (isTS001) {
                if (productType == Constants.SETTING_BOOK) {
                } else {
                    ARouter.getInstance().build(RouterConfig.QUESTION).withBoolean("isTS001", true).navigation(this)
                }
            } else {
                if (productType == Constants.SETTING_BOOK) {
                    ARouter.getInstance().build(RouterConfig.PDF).withBoolean("isTS001", false).navigation(this)
                } else {
                    ARouter.getInstance().build(RouterConfig.QUESTION).withBoolean("isTS001", false).navigation(this)
                }
            }
        }

        electronic_manual_recycler.layoutManager = LinearLayoutManager(this)
        electronic_manual_recycler.adapter = adapter
    }

    override fun initData() {

    }

    private class MyAdapter(private val isFAQ: Boolean) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var onPickListener: ((isTS001: Boolean) -> Unit)? = null

        private val optionList: ArrayList<String> = ArrayList(2)

        init {
            if (isFAQ) {
                optionList.add("TS001")
            }
            optionList.add("TS004")
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ItemViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_electronic_manual, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ItemViewHolder) {
                holder.rootView.item_text.text = optionList[position]
                holder.rootView.item_lay.setOnClickListener {
                    onPickListener?.invoke(isFAQ && position == 0)
                }
            }
        }

        override fun getItemCount(): Int = optionList.size

        private class ItemViewHolder(val rootView: View) : RecyclerView.ViewHolder(rootView)
    }

}
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
import com.topdon.module.user.R
import com.topdon.module.user.model.QuestionData
import com.topdon.module.user.model.FaqRepository
import kotlinx.android.synthetic.main.activity_question.*
import kotlinx.android.synthetic.main.item_question.view.item_question_info
import kotlinx.android.synthetic.main.item_question.view.item_question_lay
import java.util.ArrayList

@Route(path = RouterConfig.QUESTION)
class QuestionActivity : BaseActivity() {

    override fun initContentView() = R.layout.activity_question

    override fun initView() {
        val adapter = MyAdapter(FaqRepository.getQuestionList(intent.getBooleanExtra("isTS001", false)))
        adapter.onItemClickListener = {
            ARouter.getInstance()
                .build(RouterConfig.QUESTION_DETAILS)
                .withString("question", it.question)
                .withString("answer", it.answer)
                .navigation(this)
        }

        question_recycler.layoutManager = LinearLayoutManager(this)
        question_recycler.adapter = adapter
    }

    override fun initData() {

    }

    private class MyAdapter(private val questionList: ArrayList<QuestionData>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        var onItemClickListener: ((data: QuestionData) -> Unit)? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ItemHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_question, parent, false))
        }

        override fun getItemCount(): Int = questionList.size

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder is ItemHolder) {
                holder.rootView.item_question_info.text = questionList[position].question
                holder.rootView.item_question_lay.setOnClickListener {
                    onItemClickListener?.invoke(questionList[position])
                }
            }
        }

        private class ItemHolder(val rootView: View) : RecyclerView.ViewHolder(rootView)
    }
}
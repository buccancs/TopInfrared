package com.topdon.house.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.topdon.house.R
import com.topdon.lib.core.config.ExtraKeyConfig
import com.topdon.lib.core.ktbase.BaseActivity
import kotlinx.android.synthetic.main.activity_text_input.*

@SuppressLint("SetTextI18n")
class TextInputActivity : BaseActivity(), View.OnClickListener {
    override fun initContentView(): Int = R.layout.activity_text_input

    override fun initView() {
        iv_exit.setOnClickListener(this)
        iv_save.setOnClickListener(this)
        tv_quick_input1.setOnClickListener(this)
        tv_quick_input2.setOnClickListener(this)
        tv_quick_input3.setOnClickListener(this)
        tv_quick_input4.setOnClickListener(this)

        et_input.addTextChangedListener {
            tv_input_count.text = "${it?.length ?: 0}/200"
        }
        tv_title.text = intent.getStringExtra(ExtraKeyConfig.ITEM_NAME)
        et_input.setText(intent.getStringExtra(ExtraKeyConfig.RESULT_INPUT_TEXT))
    }

    override fun initData() {
    }

    override fun onClick(v: View?) {
        when (v) {
            iv_exit -> finish()
            iv_save -> {
                val intent = Intent()
                intent.putExtra(ExtraKeyConfig.RESULT_INPUT_TEXT, et_input.text.toString())
                setResult(RESULT_OK, intent)
                finish()
            }
            tv_quick_input1 -> {
                et_input.setText(et_input.text.toString() + tv_quick_input1.text)
                et_input.setSelection(et_input.text.length)
            }
            tv_quick_input2 -> {
                et_input.setText(et_input.text.toString() + tv_quick_input2.text)
                et_input.setSelection(et_input.text.length)
            }
            tv_quick_input3 -> {
                et_input.setText(et_input.text.toString() + tv_quick_input3.text)
                et_input.setSelection(et_input.text.length)
            }
            tv_quick_input4 -> {
                et_input.setText(et_input.text.toString() + tv_quick_input4.text)
                et_input.setSelection(et_input.text.length)
            }
        }
    }
}
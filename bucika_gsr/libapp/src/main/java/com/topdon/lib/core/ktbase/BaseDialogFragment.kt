package com.topdon.lib.core.ktbase

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import com.topdon.lib.core.R

/**
 * 使用 DataBinding 的基础 DialogFragment.
 *
 * Created by LCG on 2024/10/14.
 */
abstract class BaseDialogFragment<B : ViewDataBinding> : AppCompatDialogFragment() {

    /**
     * 在 [onDestroyView] 要将 binding 置为 null，
     * 而将 binding 声明为可为 null 类型使用太过麻烦，使用该变量做一重包装避免该问题.
     */
    private var _binding: B? = null
    /**
     * 注意：由于 Fragment 存在时间比其视图长，binding 将在 [onDestroyView] 置为 null.
     *
     * 仅可在 [onCreateView] 与 [onDestroyView] 之间访问.
     */
    protected val binding: B get() = _binding!!


    /**
     * 子类实现该方法，返回使用 DataBinding 的 layout 资源 Id.
     */
    @LayoutRes
    protected abstract fun initContentLayoutId(): Int
    /**
     * 子类实现该方法，执行 onViewCreated 之后的初始化逻辑.
     */
    protected abstract fun initView(savedInstanceState: Bundle?)



    /**
     * 对话框 [Dialog.setCanceledOnTouchOutside] 的值.
     */
    var isCanceledOnTouchOutSide: Boolean = true
        set(value) {
            field = value
            dialog?.setCanceledOnTouchOutside(value)
        }
    /**
     * 子类可重写该方法，执行 onCreateDialog 阶段创建 Dialog 后的相关设置.
     */
    protected open fun afterDialogCreate(layoutParams: WindowManager.LayoutParams) {

    }
    /**
     * 子类可重写该方法，返回 Dialog 要使用的 themeResId.
     */
    @StyleRes
    protected open fun getDialogThemeResId(): Int = R.style.base_dialog


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), getDialogThemeResId())
        dialog.setCancelable(isCancelable)
        dialog.setCanceledOnTouchOutside(isCanceledOnTouchOutSide)
        dialog.window?.let {
            val layoutParams = it.attributes
            afterDialogCreate(layoutParams)
            dialog.onWindowAttributesChanged(layoutParams)
        }
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = DataBindingUtil.inflate(inflater, initContentLayoutId(), container, false)
        _binding?.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initView(savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    fun show(context: Context) {
        if (isAdded) {
            return
        }
        if (context is FragmentActivity) {
            super.show(context.supportFragmentManager, null)
            context.supportFragmentManager.executePendingTransactions()
        }
    }
}
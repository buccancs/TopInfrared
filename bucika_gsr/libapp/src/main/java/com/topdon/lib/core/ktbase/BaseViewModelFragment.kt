package com.topdon.lib.core.ktbase

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider

abstract class BaseViewModelFragment<VM : BaseViewModel> : BaseFragment() {

    protected lateinit var viewModel: VM

    abstract fun providerVMClass(): Class<VM>?

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initVM()
        super.onViewCreated(view, savedInstanceState)
    }

    private fun initVM() {
        providerVMClass()?.let {
            viewModel = ViewModelProvider(this).get(it)
            lifecycle.addObserver(viewModel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::viewModel.isInitialized)
            lifecycle.removeObserver(viewModel)
    }
}
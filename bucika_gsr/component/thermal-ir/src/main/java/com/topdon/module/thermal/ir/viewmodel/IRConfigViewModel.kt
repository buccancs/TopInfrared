package com.topdon.module.thermal.ir.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.topdon.lib.core.utils.SingleLiveEvent
import com.topdon.module.thermal.ir.bean.DataBean
import com.topdon.module.thermal.ir.bean.ModelBean
import com.topdon.module.thermal.ir.repository.ConfigRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IRConfigViewModel(application: Application) : AndroidViewModel(application) {

    val configLiveData = SingleLiveEvent<ModelBean>()

    fun getConfig(isTC007: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            configLiveData.postValue(ConfigRepository.read(isTC007))
        }
    }

    fun updateDefaultEnvironment(isTC007: Boolean, environment: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val modelBean = configLiveData.value ?: ConfigRepository.read(isTC007)
            modelBean.defaultModel.environment = environment
            ConfigRepository.update(isTC007, modelBean)
            configLiveData.postValue(modelBean)
        }
    }

    fun updateDefaultDistance(isTC007: Boolean, distance: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val modelBean = configLiveData.value ?: ConfigRepository.read(isTC007)
            modelBean.defaultModel.distance = distance
            ConfigRepository.update(isTC007, modelBean)
            configLiveData.postValue(modelBean)
        }
    }

    fun updateDefaultRadiation(isTC007: Boolean, radiation: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val modelBean = configLiveData.value ?: ConfigRepository.read(isTC007)
            modelBean.defaultModel.radiation = radiation
            ConfigRepository.update(isTC007, modelBean)
            configLiveData.postValue(modelBean)
        }
    }

    fun addConfig(isTC007: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val modelBean = configLiveData.value ?: ConfigRepository.read(isTC007)

            var index = 0
            modelBean.myselfModel.forEach {
                index = index.coerceAtLeast(it.id)
            }
            index++

            modelBean.myselfModel.add(DataBean(id = index, name = index.toString()))

            ConfigRepository.update(isTC007, modelBean)
            configLiveData.postValue(modelBean)
        }
    }

    fun checkConfig(isTC007: Boolean, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val modelBean = configLiveData.value ?: ConfigRepository.read(isTC007)
            modelBean.defaultModel.use = id == 0
            modelBean.myselfModel.forEach {
                it.use = it.id == id
            }
            ConfigRepository.update(isTC007, modelBean)
            configLiveData.postValue(modelBean)
        }
    }

    fun deleteConfig(isTC007: Boolean, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val modelBean = configLiveData.value ?: ConfigRepository.read(isTC007)
            var removeAt = modelBean.myselfModel.size
            for (i in modelBean.myselfModel.indices) {
                val dataBean = modelBean.myselfModel[i]
                if (dataBean.id == id) {
                    if (dataBean.use) {
                        modelBean.defaultModel.use = true
                    }
                    modelBean.myselfModel.removeAt(i)
                    removeAt = i
                    break
                }
            }

            if (removeAt < modelBean.myselfModel.size) {
                for (i in removeAt until modelBean.myselfModel.size) {
                    val dataBean = modelBean.myselfModel[i]
                    dataBean.id = i + 1
                    dataBean.name = dataBean.id.toString()
                }
            }

            ConfigRepository.update(isTC007, modelBean)
            configLiveData.postValue(modelBean)
        }
    }

    fun updateCustom(isTC007: Boolean, dataBean: DataBean) {
        viewModelScope.launch(Dispatchers.IO) {
            val modelBean = configLiveData.value ?: ConfigRepository.read(isTC007)
            for (i in modelBean.myselfModel.indices) {
                if (modelBean.myselfModel[i].id == dataBean.id) {
                    modelBean.myselfModel[i] = dataBean
                    break
                }
            }
            ConfigRepository.update(isTC007, modelBean)
            configLiveData.postValue(modelBean)
        }
    }

}
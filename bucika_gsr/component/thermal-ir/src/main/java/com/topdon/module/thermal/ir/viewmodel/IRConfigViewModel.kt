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

    /**
     * 读取配置数据
     */
    fun getConfig(isTC007: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            configLiveData.postValue(ConfigRepository.read(isTC007))
        }
    }

    /**
     * 更新默认参数中的环境温度，单位摄氏度。
     */
    fun updateDefaultEnvironment(isTC007: Boolean, environment: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val modelBean = configLiveData.value ?: ConfigRepository.read(isTC007)
            modelBean.defaultModel.environment = environment
            ConfigRepository.update(isTC007, modelBean)
            configLiveData.postValue(modelBean)
        }
    }

    /**
     * 更新默认参数中的距离，单位不详。
     */
    fun updateDefaultDistance(isTC007: Boolean, distance: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val modelBean = configLiveData.value ?: ConfigRepository.read(isTC007)
            modelBean.defaultModel.distance = distance
            ConfigRepository.update(isTC007, modelBean)
            configLiveData.postValue(modelBean)
        }
    }

    /**
     * 更新默认参数中的发射率。
     */
    fun updateDefaultRadiation(isTC007: Boolean, radiation: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val modelBean = configLiveData.value ?: ConfigRepository.read(isTC007)
            modelBean.defaultModel.radiation = radiation
            ConfigRepository.update(isTC007, modelBean)
            configLiveData.postValue(modelBean)
        }
    }

    /**
     * 增加一个自定义模式
     */
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

    /**
     * 选择模式
     * @param id 0:默认模式   > 0 采用自定义模式
     */
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

    /**
     * 删除自定义模式
     * @param id 自定义模式 id
     */
    fun deleteConfig(isTC007: Boolean, id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val modelBean = configLiveData.value ?: ConfigRepository.read(isTC007)
            var removeAt = modelBean.myselfModel.size
            for (i in modelBean.myselfModel.indices) {
                val dataBean = modelBean.myselfModel[i]
                if (dataBean.id == id) {
                    if (dataBean.use) {//删除当前正在使用的自定义模式，变更为使用默认模式
                        modelBean.defaultModel.use = true
                    }
                    modelBean.myselfModel.removeAt(i)
                    removeAt = i
                    break
                }
            }

            // BUG 28055 提的问题，删除后要把后面名称往前补，虽然实际使用非常怪，先按 BUG 改吧
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

    /**
     * 更新一项自定义参数.
     */
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
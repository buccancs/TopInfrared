package com.topdon.house.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.topdon.lib.core.db.AppDatabase
import com.topdon.lib.core.db.entity.DirDetect
import com.topdon.lib.core.db.entity.HouseDetect
import com.topdon.lib.core.db.entity.ItemDetect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetectViewModel(application: Application) : AndroidViewModel(application) {
    
    val detectListLD =  MutableLiveData<List<HouseDetect>>()
    
    fun queryAll() {
        viewModelScope.launch(Dispatchers.IO) {
            detectListLD.postValue(AppDatabase.getInstance().houseDetectDao().queryAll())
        }
    }

    val detectLD = MutableLiveData<HouseDetect?>()
    
    fun queryById(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            detectLD.postValue(AppDatabase.getInstance().houseDetectDao().queryById(id))
        }
    }
    
    fun insertDefaultDirs(houseDetect: HouseDetect) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance().houseDetectDao().insertDefaultDirs(houseDetect)
            detectLD.postValue(houseDetect)
        }
    }

    val dirLD = MutableLiveData<DirDetect>()
    
    fun queryDirById(dirId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dirLD.postValue(AppDatabase.getInstance().houseDetectDao().queryDir(dirId))
        }
    }

    val copyDetectLD = MutableLiveData<Pair<Int, HouseDetect>>()
    
    fun copyDetect(position: Int, houseDetect: HouseDetect) {
        viewModelScope.launch(Dispatchers.IO) {
            copyDetectLD.postValue(Pair(position, AppDatabase.getInstance().houseDetectDao().copyDetect(houseDetect)))
        }
    }

    val copyDirLD = MutableLiveData<Pair<Int, DirDetect>>()
    
    fun copyDir(layoutIndex: Int, dirDetect: DirDetect) {
        viewModelScope.launch(Dispatchers.IO) {
            val dirList: ArrayList<DirDetect> = dirDetect.houseDetect.dirList
            val position = dirList.indexOf(dirDetect)
            if (position >= 0) {
                val newDir: DirDetect = AppDatabase.getInstance().houseDetectDao().copyDir(dirList, position)
                dirList.add(position + 1, newDir)
                copyDirLD.postValue(Pair(layoutIndex, newDir))
            }
        }
    }

    val copyItemLD = MutableLiveData<Pair<Int, ItemDetect>>()
    
    fun copyItem(layoutIndex: Int, itemDetect: ItemDetect) {
        viewModelScope.launch(Dispatchers.IO) {
            val itemList: ArrayList<ItemDetect> = itemDetect.dirDetect.itemList
            val position = itemList.indexOf(itemDetect)
            if (position >= 0) {
                val newItem = AppDatabase.getInstance().houseDetectDao().copyItem(itemList, position)
                itemList.add(position + 1, newItem)
                copyItemLD.postValue(Pair(layoutIndex, newItem))
            }
        }
    }

    val delItemLD = MutableLiveData<Pair<Int, ItemDetect>>()
    
    fun delItem(layoutIndex: Int, itemDetect: ItemDetect) {
        viewModelScope.launch(Dispatchers.IO) {
            val dirDetect: DirDetect = itemDetect.dirDetect
            val itemList: ArrayList<ItemDetect> = dirDetect.itemList
            val position = itemList.indexOf(itemDetect)
            if (position >= 0) {
                AppDatabase.getInstance().houseDetectDao().deleteItem(itemDetect)
                itemList.removeAt(position)

                if (itemList.isEmpty()) {
                    val dirList: ArrayList<DirDetect> = dirDetect.houseDetect.dirList
                    val dirPosition = dirList.indexOf(dirDetect)
                    if (dirPosition >= 0) {
                        AppDatabase.getInstance().houseDetectDao().deleteDir(dirDetect)
                        dirList.removeAt(dirPosition)
                    }
                    if (dirList.isEmpty()) {
                        detectLD.postValue(dirDetect.houseDetect)
                    } else {
                        delItemLD.postValue(Pair(layoutIndex, itemDetect))
                    }
                } else {
                    if (itemDetect.state > 0) {
                        val dir = itemDetect.dirDetect
                        when (itemDetect.state) {
                            1 -> dir.goodCount--
                            2 -> dir.warnCount--
                            3 -> dir.dangerCount--
                        }
                        updateDir(dir)
                    }
                    delItemLD.postValue(Pair(layoutIndex, itemDetect))
                }
            }
        }
    }

    fun updateDir(vararg dirDetect: DirDetect) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance().houseDetectDao().updateDir(*dirDetect)
        }
    }
    
    fun updateItem(vararg itemDetect: ItemDetect) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance().houseDetectDao().updateItem(*itemDetect)
        }
    }

    fun deleteMore(vararg houseDetect: HouseDetect) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getInstance().houseDetectDao().deleteDetect(*houseDetect)
            queryAll()
        }
    }
}
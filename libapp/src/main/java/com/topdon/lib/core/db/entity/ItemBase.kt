package com.topdon.lib.core.db.entity

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.blankj.utilcode.util.Utils
import com.topdon.lib.core.R

open class ItemBase {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(index = true)
    open var parentId: Long = 0

    @ColumnInfo
    var position: Int = 0

    @ColumnInfo
    var itemName: String = ""

    @ColumnInfo
    var state: Int = 0

    @ColumnInfo
    var inputText: String = ""

    @ColumnInfo
    var image1: String = ""
    
    @ColumnInfo
    var image2: String = ""
    
    @ColumnInfo
    var image3: String = ""
    
    @ColumnInfo
    var image4: String = ""

    override fun equals(other: Any?): Boolean = other is ItemBase && other.id == id

    override fun hashCode(): Int = id.toInt()

    fun getStateStr(context: Context): String = when (state) {
        1 -> context.getString(R.string.house_state_good)
        2 -> context.getString(R.string.house_state_repair)
        3 -> context.getString(R.string.house_state_replace)
        else -> ""
    }

    fun getImageSize(): Int {
        var result = 0
        if (image1.isNotEmpty()) {
            result++
        }
        if (image2.isNotEmpty()) {
            result++
        }
        if (image3.isNotEmpty()) {
            result++
        }
        if (image4.isNotEmpty()) {
            result++
        }
        return result
    }

    fun buildImageList(): ArrayList<String> {
        val resultList: ArrayList<String> = ArrayList(4)
        if (image1.isNotEmpty()) {
            resultList.add(image1)
        }
        if (image2.isNotEmpty()) {
            resultList.add(image2)
        }
        if (image3.isNotEmpty()) {
            resultList.add(image3)
        }
        if (image4.isNotEmpty()) {
            resultList.add(image4)
        }
        return resultList
    }

    fun addOneImage(imagePath: String?) {
        if (imagePath.isNullOrEmpty()) {
            return
        }
        if (image1.isEmpty()) {
            image1 = imagePath
        } else if (image2.isEmpty()) {
            image2 = imagePath
        } else if (image3.isEmpty()) {
            image3 = imagePath
        } else if (image4.isEmpty()) {
            image4 = imagePath
        }
    }

    fun delOneImage(imageNum: Int) {
        when (imageNum) {
            4 -> {
                image4 = ""
            }
            3 -> {
                if (image4.isEmpty()) {
                    image3 = ""
                } else {
                    image3 = image4
                    image4 = ""
                }
            }
            2 -> {
                if (image3.isEmpty()) {
                    image2 = ""
                } else {
                    image2 = image3
                    if (image4.isEmpty()) {
                        image3 = ""
                    } else {
                        image3 = image4
                        image4 = ""
                    }
                }
            }
            1 -> {
                if (image2.isEmpty()) {
                    image1 = ""
                } else {
                    image1 = image2
                    if (image3.isEmpty()) {
                        image2 = ""
                    } else {
                        image2 = image3
                        if (image4.isEmpty()) {
                            image3 = ""
                        } else {
                            image3 = image4
                            image4 = ""
                        }
                    }
                }
            }
        }
    }
}

@Entity(foreignKeys = [ForeignKey(
    entity = DirDetect::class,
    parentColumns = ["id"],
    childColumns = ["parentId"],
    onDelete = ForeignKey.CASCADE,
    onUpdate = ForeignKey.CASCADE,)])
class ItemDetect() : ItemBase() {
    @Ignore
    constructor(parentId: Long, position: Int, itemName: String) : this() {
        this.parentId = parentId
        this.position = position
        this.itemName = itemName
    }

    @ColumnInfo(index = true)
    override var parentId: Long = 0

    @Ignore
    var hasSelect = false

    @Ignore
    var dirDetect = DirDetect()

    fun copyName(): String = "$itemName(1)"

    fun copyOne(parentId: Long = this.parentId, position: Int = this.position, itemName: String = this.itemName): ItemDetect {
        val newItemDetect = ItemDetect()
        newItemDetect.id = 0
        newItemDetect.parentId = parentId
        newItemDetect.position = position
        newItemDetect.itemName = itemName
        newItemDetect.state = state
        newItemDetect.inputText = inputText
        newItemDetect.image1 = image1
        newItemDetect.image2 = image2
        newItemDetect.image3 = image3
        newItemDetect.image4 = image4
        newItemDetect.hasSelect = hasSelect
        newItemDetect.dirDetect = dirDetect
        return newItemDetect
    }

    fun toItemReport(): ItemReport {
        val itemReport = ItemReport()
        itemReport.id = 0
        itemReport.parentId = 0
        itemReport.position = position
        itemReport.itemName = itemName
        itemReport.state = state
        itemReport.inputText = inputText
        itemReport.image1 = image1
        itemReport.image2 = image2
        itemReport.image3 = image3
        itemReport.image4 = image4
        return itemReport
    }

    companion object {
        
        fun buildDefaultItemList(parentId: Long, position: Int): ArrayList<ItemDetect> = when (position) {
            0 -> arrayListOf(
                ItemDetect(parentId, 0, Utils.getApp().getString(R.string.detect_item1)),
                ItemDetect(parentId, 1, Utils.getApp().getString(R.string.detect_dir1_item2)),
                ItemDetect(parentId, 2, Utils.getApp().getString(R.string.detect_dir1_item3)),
                ItemDetect(parentId, 3, Utils.getApp().getString(R.string.detect_dir1_item4)),
                ItemDetect(parentId, 4, Utils.getApp().getString(R.string.detect_dir1_item5)),
                ItemDetect(parentId, 5, Utils.getApp().getString(R.string.detect_dir1_item6)),
                ItemDetect(parentId, 6, Utils.getApp().getString(R.string.detect_dir1_item7)),
            )
            1 -> arrayListOf(
                ItemDetect(parentId, 0, Utils.getApp().getString(R.string.detect_item1)),
                ItemDetect(parentId, 1, Utils.getApp().getString(R.string.detect_dir2_item2)),
                ItemDetect(parentId, 2, Utils.getApp().getString(R.string.detect_dir2_item3)),
                ItemDetect(parentId, 3, Utils.getApp().getString(R.string.detect_dir2_item4)),
                ItemDetect(parentId, 4, Utils.getApp().getString(R.string.detect_dir2_item5)),
                ItemDetect(parentId, 5, Utils.getApp().getString(R.string.detect_dir2_item6)),
            )
            2 -> arrayListOf(
                ItemDetect(parentId, 0, Utils.getApp().getString(R.string.detect_dir3_item1)),
                ItemDetect(parentId, 1, Utils.getApp().getString(R.string.detect_dir3_item2)),
                ItemDetect(parentId, 2, Utils.getApp().getString(R.string.detect_dir3_item3)),
            )
            3 -> arrayListOf(
                ItemDetect(parentId, 0, Utils.getApp().getString(R.string.detect_item1)),
                ItemDetect(parentId, 1, Utils.getApp().getString(R.string.detect_dir4_item2)),
                ItemDetect(parentId, 2, Utils.getApp().getString(R.string.detect_dir4_item3)),
                ItemDetect(parentId, 3, Utils.getApp().getString(R.string.detect_dir4_item4)),
                ItemDetect(parentId, 4, Utils.getApp().getString(R.string.detect_dir4_item5)),
                ItemDetect(parentId, 5, Utils.getApp().getString(R.string.detect_dir4_item6)),
                ItemDetect(parentId, 6, Utils.getApp().getString(R.string.detect_dir4_item7)),
            )
            4 -> arrayListOf(
                ItemDetect(parentId, 0, Utils.getApp().getString(R.string.detect_item1)),
                ItemDetect(parentId, 1, Utils.getApp().getString(R.string.detect_dir5_item2)),
                ItemDetect(parentId, 2, Utils.getApp().getString(R.string.detect_dir5_item3)),
                ItemDetect(parentId, 3, Utils.getApp().getString(R.string.detect_dir5_item4)),
                ItemDetect(parentId, 4, Utils.getApp().getString(R.string.detect_dir5_item5)),
                ItemDetect(parentId, 5, Utils.getApp().getString(R.string.detect_dir5_item6)),
                ItemDetect(parentId, 6, Utils.getApp().getString(R.string.detect_dir5_item7)),
                ItemDetect(parentId, 7, Utils.getApp().getString(R.string.detect_dir5_item8)),
                ItemDetect(parentId, 8, Utils.getApp().getString(R.string.detect_dir5_item9)),
            )
            5 -> arrayListOf(
                ItemDetect(parentId, 0, Utils.getApp().getString(R.string.detect_item1)),
                ItemDetect(parentId, 1, Utils.getApp().getString(R.string.detect_dir6_item2)),
                ItemDetect(parentId, 2, Utils.getApp().getString(R.string.detect_dir6_item3)),
            )
            6 -> arrayListOf(
                ItemDetect(parentId, 0, Utils.getApp().getString(R.string.detect_item1)),
                ItemDetect(parentId, 1, Utils.getApp().getString(R.string.detect_dir7_item2)),
                ItemDetect(parentId, 2, Utils.getApp().getString(R.string.detect_dir7_item3)),
                ItemDetect(parentId, 3, Utils.getApp().getString(R.string.detect_dir7_item4)),
                ItemDetect(parentId, 4, Utils.getApp().getString(R.string.detect_dir7_item5)),
                ItemDetect(parentId, 5, Utils.getApp().getString(R.string.detect_dir7_item6)),
                ItemDetect(parentId, 6, Utils.getApp().getString(R.string.detect_dir7_item7)),
                ItemDetect(parentId, 7, Utils.getApp().getString(R.string.detect_dir7_item8)),
                ItemDetect(parentId, 8, Utils.getApp().getString(R.string.detect_dir7_item9)),
            )
            7 -> arrayListOf(
                ItemDetect(parentId, 0, Utils.getApp().getString(R.string.detect_item1)),
                ItemDetect(parentId, 1, Utils.getApp().getString(R.string.detect_dir8_item2)),
                ItemDetect(parentId, 2, Utils.getApp().getString(R.string.detect_dir8_item3)),
                ItemDetect(parentId, 3, Utils.getApp().getString(R.string.detect_dir8_item4)),
                ItemDetect(parentId, 4, Utils.getApp().getString(R.string.detect_dir8_item5)),
            )
            8 -> arrayListOf(
                ItemDetect(parentId, 0, Utils.getApp().getString(R.string.detect_item1)),
                ItemDetect(parentId, 1, Utils.getApp().getString(R.string.detect_dir9_item2)),
                ItemDetect(parentId, 2, Utils.getApp().getString(R.string.detect_dir9_item3)),
                ItemDetect(parentId, 3, Utils.getApp().getString(R.string.detect_dir9_item4)),
                ItemDetect(parentId, 4, Utils.getApp().getString(R.string.detect_dir9_item5)),
            )
            9 -> arrayListOf(
                ItemDetect(parentId, 0, Utils.getApp().getString(R.string.detect_item1)),
                ItemDetect(parentId, 1, Utils.getApp().getString(R.string.detect_dir10_item2)),
                ItemDetect(parentId, 2, Utils.getApp().getString(R.string.detect_dir10_item3)),
                ItemDetect(parentId, 3, Utils.getApp().getString(R.string.detect_dir10_item4)),
            )
            else -> arrayListOf(ItemDetect(parentId, 0, Utils.getApp().getString(R.string.detect_item1)))
        }
    }
}

@Entity(foreignKeys = [ForeignKey(
    entity = DirReport::class,
    parentColumns = ["id"],
    childColumns = ["parentId"],
    onDelete = ForeignKey.CASCADE,
    onUpdate = ForeignKey.CASCADE,)])
class ItemReport : ItemBase() {
    
    @ColumnInfo(index = true)
    override var parentId: Long = 0
}
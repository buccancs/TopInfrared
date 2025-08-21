package com.topdon.lib.core.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.blankj.utilcode.util.Utils
import com.topdon.lib.core.R

/**
 * 检测 或 报告的一项目录.
 *
 * Created by LCG on 2024/8/19.
 */
open class DirBase {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    /**
     * 所对应的检测或报告目录 Id
     */
    @ColumnInfo(index = true)
    open var parentId: Long = 0

    /**
     * 该目录在检测或报告目录列表中的 index.
     */
    @ColumnInfo
    var position: Int = 0

    /**
     * 目录名，如“一楼”
     */
    @ColumnInfo
    var dirName: String = ""

    /**
     * 没问题项目的数量.
     */
    @ColumnInfo
    var goodCount: Int = 0

    /**
     * 需维修项目的数量.
     */
    @ColumnInfo
    var warnCount: Int = 0

    /**
     * 需更换项目的数量.
     */
    @ColumnInfo
    var dangerCount: Int = 0




    override fun equals(other: Any?): Boolean = other is DirBase && other.id == id

    override fun hashCode(): Int = id.toInt()

    fun getGoodCountStr(): String = if (goodCount > 99) "99+" else goodCount.toString()

    fun getWarnCountStr(): String = if (warnCount > 99) "99+" else warnCount.toString()

    fun getDangerCountStr(): String = if (dangerCount > 99) "99+" else dangerCount.toString()
}



/**
 * 检测所属的一项目录.
 */
@Entity(foreignKeys = [ForeignKey(
    entity = HouseDetect::class,
    parentColumns = ["id"],
    childColumns = ["parentId"],
    onDelete = ForeignKey.CASCADE,
    onUpdate = ForeignKey.CASCADE,)])
class DirDetect() : DirBase() {
    @Ignore
    constructor(parentId: Long, position: Int, dirName: String) : this() {
        this.parentId = parentId
        this.position = position
        this.dirName = dirName
    }

    /**
     * 所对应的检测 Id
     */
    @ColumnInfo(index = true)
    override var parentId: Long = 0

    /**
     * 该目录是否已选中，仅用于目录编辑界面.
     */
    @Ignore
    var hasSelect = false

    /**
     * 该目录是否处于展开状态
     */
    @Ignore
    var isExpand: Boolean = false

    /**
     * 该目录所属的检测.
     */
    @Ignore
    var houseDetect = HouseDetect()

    /**
     * 该目录下的项目列表
     */
    @Ignore
    var itemList: ArrayList<ItemDetect> = ArrayList()

    /**
     * 返回一个 id 为 0，名称添加 (1)，position + 1，itemList 复制，其余属性完全一致的新对象.
     */
    fun copyOne(): DirDetect {
        val newDirDetect = DirDetect()
        newDirDetect.id = 0
        newDirDetect.parentId = parentId
        newDirDetect.position = position + 1
        newDirDetect.dirName = "$dirName(1)"
        newDirDetect.goodCount = goodCount
        newDirDetect.warnCount = warnCount
        newDirDetect.dangerCount = dangerCount
        newDirDetect.isExpand = isExpand
        newDirDetect.hasSelect = hasSelect
        newDirDetect.houseDetect = houseDetect
        val newItemList: ArrayList<ItemDetect> = ArrayList(itemList.size)
        for (oldItem in itemList) {
            newItemList.add(oldItem.copyOne(parentId = 0))
        }
        newDirDetect.itemList = newItemList
        return newDirDetect
    }

    /**
     * 将当前检测目录转换为报告目录，注意 id、parent 重置为 0，无效目录剔除.
     */
    fun toDirReport(): DirReport {
        val dirReport = DirReport()
        dirReport.id = 0
        dirReport.parentId = 0
        dirReport.position = position
        dirReport.dirName = dirName
        dirReport.goodCount = goodCount
        dirReport.warnCount = warnCount
        dirReport.dangerCount = dangerCount

        val newItemList: ArrayList<ItemReport> = ArrayList(itemList.size)
        for (itemDetect in itemList) {
            if (itemDetect.state > 0 || itemDetect.inputText.isNotEmpty() || itemDetect.image1.isNotEmpty()) {
                newItemList.add(itemDetect.toItemReport())
            }
        }
        dirReport.itemList = newItemList
        return dirReport
    }

    companion object {
        /**
         * 构建默认的检测目录列表.
         */
        fun buildDefaultDirList(parentId: Long): ArrayList<DirDetect> = arrayListOf(
            DirDetect(parentId, 0, Utils.getApp().getString(R.string.detect_dir1_root)),
            DirDetect(parentId, 1, Utils.getApp().getString(R.string.detect_dir2_root)),
            DirDetect(parentId, 2, Utils.getApp().getString(R.string.detect_dir3_root)),
            DirDetect(parentId, 3, Utils.getApp().getString(R.string.detect_dir4_root)),
            DirDetect(parentId, 4, Utils.getApp().getString(R.string.detect_dir5_root)),
            DirDetect(parentId, 5, Utils.getApp().getString(R.string.detect_dir6_root)),
            DirDetect(parentId, 6, Utils.getApp().getString(R.string.detect_dir7_root)),
            DirDetect(parentId, 7, Utils.getApp().getString(R.string.detect_dir8_root)),
            DirDetect(parentId, 8, Utils.getApp().getString(R.string.detect_dir9_root)),
            DirDetect(parentId, 9, Utils.getApp().getString(R.string.detect_dir10_root)),
            DirDetect(parentId, 10, Utils.getApp().getString(R.string.detect_dir11_root)),
        )
    }
}



/**
 * 报告所属的一项目录.
 */
@Entity(foreignKeys = [ForeignKey(
    entity = HouseReport::class,
    parentColumns = ["id"],
    childColumns = ["parentId"],
    onDelete = ForeignKey.CASCADE,
    onUpdate = ForeignKey.CASCADE,)])
class DirReport : DirBase() {
    /**
     * 所对应的报告 Id
     */
    @ColumnInfo(index = true)
    override var parentId: Long = 0

    /**
     * 该目录下的项目列表
     */
    @Ignore
    var itemList: ArrayList<ItemReport> = ArrayList()
}
package com.topdon.lib.ui.utils

/**
 * @author: CaiSongL
 * @date: 2023/3/31 9:56
 */
internal object CenterItemUtils {
    fun getMinDifferItem(itemHeights: List<CenterViewItem>): CenterViewItem {
        var minItem = itemHeights[0] //默认第一个是最小差值
        for (i in itemHeights.indices) {
            //遍历获取最小差值
            if (itemHeights[i].differ <= minItem.differ) {
                minItem = itemHeights[i]
            }
        }
        return minItem
    }

    class CenterViewItem
    //当前Item索引
    //当前item和居中位置的差值
        (var position: Int, var differ: Int)
}
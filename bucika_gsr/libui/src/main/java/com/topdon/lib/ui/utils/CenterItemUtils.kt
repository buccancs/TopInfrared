package com.topdon.lib.ui.utils

internal object CenterItemUtils {
    fun getMinDifferItem(itemHeights: List<CenterViewItem>): CenterViewItem {
        var minItem = itemHeights[0]
        for (i in itemHeights.indices) {
            if (itemHeights[i].differ <= minItem.differ) {
                minItem = itemHeights[i]
            }
        }
        return minItem
    }

    class CenterViewItem
        (var position: Int, var differ: Int)
}
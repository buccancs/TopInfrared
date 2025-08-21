package com.topdon.lib.ui.transform

import android.view.View

/**
 * @author: CaiSongL
 * @date: 2023/4/1 11:32
 */
@Deprecated("热成像-菜单-拍照已重构，不需要这个类了")
interface HorizontalScrollItemTransformer {
    fun transformItem(item: View, position: Float)
}
package com.topdon.lib.core.tools

import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.widget.EditText
import java.util.regex.Pattern

class InputTextFilterTool {

    /**
     * 设置editText过滤器
     *
     * @param editText
     */
    fun setEditTextFilter(editText: EditText) {
        val oldFilters = editText.filters
        val oldFiltersLength = oldFilters.size
        val newFilters = arrayOfNulls<InputFilter>(oldFiltersLength + 1)
        if (oldFiltersLength > 0) {
            System.arraycopy(oldFilters, 0, newFilters, 0, oldFiltersLength)
        }
        //添加新的过滤规则
        newFilters[oldFiltersLength] = mInputFilter
        editText.filters = newFilters
    }

    //过滤表情
    private var mInputFilter: InputFilter = object : InputFilter {
        //        Pattern emoji = Pattern.compile("[\ud83c\udc00-\ud83c\udfff]|[\ud83d\udc00-\ud83d\udfff]|[\u2600-\u27ff]", Pattern.UNICODE_CASE | Pattern.CASE_INSENSITIVE);
        var emoji = Pattern.compile(
            "[^\u0020-\u007E\u00A0-\u00BE\u2E80-\uA4CF\uF900-\uFAFF\uFE30-\uFE4F\uFF00-\uFFEF\u0080-\u009F\u2000-\u201f\\r\\n]",
            Pattern.UNICODE_CASE or Pattern.CASE_INSENSITIVE
        )

        override fun filter(
            source: CharSequence,
            start: Int,
            end: Int,
            dest: Spanned,
            dstart: Int,
            dend: Int
        ): CharSequence? {
            val emojiMatcher = emoji.matcher(source)
            if (emojiMatcher.find()) {
                Log.w("123", "不支持输入表情")
                return ""
            }
            return null
        }
    }
}
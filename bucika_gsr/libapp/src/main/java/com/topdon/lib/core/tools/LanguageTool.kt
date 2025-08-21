package com.topdon.lib.core.tools

import android.content.Context
import com.blankj.utilcode.util.Utils
import com.topdon.lib.core.R
import com.topdon.lib.core.common.SharedManager
import com.topdon.lib.core.tools.ConstantLanguages

object LanguageTool {

    /**
     * 获取显示各国语言
     */
    fun showLanguage(context: Context): String {
        return when (SharedManager.getLanguage(context)) {
            ConstantLanguages.ZH_CN -> context.getString(R.string.china)
            ConstantLanguages.ZH_TW -> context.getString(R.string.china_tw)
            ConstantLanguages.ENGLISH -> context.getString(R.string.english)
            ConstantLanguages.RU -> context.getString(R.string.russian)
            ConstantLanguages.ES -> context.getString(R.string.espanol)
            ConstantLanguages.GERMAN -> context.getString(R.string.deutsch)
            ConstantLanguages.JA -> context.getString(R.string.japanese)
            ConstantLanguages.PT -> context.getString(R.string.portugues)
            ConstantLanguages.FR -> context.getString(R.string.french)
            ConstantLanguages.IT -> context.getString(R.string.italian)
            ConstantLanguages.PL -> context.getString(R.string.polish)
            ConstantLanguages.CS -> context.getString(R.string.czech)
            ConstantLanguages.UK -> context.getString(R.string.ukrainian)
            ConstantLanguages.NL -> context.getString(R.string.dutch)
            else -> context.getString(R.string.english)
        }
    }

    /**
     * 获取各国语言简称
     * (用于服务端多语言的识别)
     */
    fun useLanguage(context: Context): String {
        return when (SharedManager.getLanguage(context)) {
            ConstantLanguages.ZH_CN -> "zh-CN"
            ConstantLanguages.ZH_TW -> "zh-HK"
            ConstantLanguages.ENGLISH -> "en-WW"
            ConstantLanguages.RU -> "ru-RU"
            ConstantLanguages.ES -> "es-ES"
            ConstantLanguages.GERMAN -> "de-DE"
            ConstantLanguages.JA -> "ja-JP"
            ConstantLanguages.PT -> "pt-PT"
            ConstantLanguages.FR -> "fr-FR"
            ConstantLanguages.IT -> "it-IT"
            ConstantLanguages.PL -> "pl-PL"
            ConstantLanguages.CS -> "cs-CZ"
            ConstantLanguages.UK -> "uk-UA"
            ConstantLanguages.NL -> "nl-NL"
            else -> "en-WW"
        }
    }

    /**
     * 获取各国语言简称
     * (用于声明接口)
     */
    fun useStatementLanguage(): String {
        return when (SharedManager.getLanguage(Utils.getApp())) {
            ConstantLanguages.ZH_CN -> "CN"
            ConstantLanguages.ZH_TW -> "HK"
            ConstantLanguages.ENGLISH -> "EN"
            ConstantLanguages.RU -> "RU"
            ConstantLanguages.ES -> "ES"
            ConstantLanguages.GERMAN -> "DE"
            ConstantLanguages.JA -> "JP"
            ConstantLanguages.PT -> "PT"
            ConstantLanguages.FR -> "FR"
            ConstantLanguages.IT -> "IT"
            ConstantLanguages.PL -> "pl"
            ConstantLanguages.CS -> "cs"
            ConstantLanguages.UK -> "uk"
            ConstantLanguages.NL -> "NL"
            else -> "EN"
        }
    }
}
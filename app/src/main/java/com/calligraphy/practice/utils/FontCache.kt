package com.calligraphy.practice.utils

import android.content.Context
import android.graphics.Typeface

/**
 * 字体缓存管理器
 * 避免重复加载相同字体，提升性能
 */
object FontCache {
    private val cache = mutableMapOf<String, Typeface>()

    /**
     * 获取字体，如果已缓存则返回缓存的实例
     */
    fun getTypeface(context: Context, fontPath: String): Typeface {
        return cache.getOrPut(fontPath) {
            try {
                Typeface.createFromAsset(context.assets, fontPath)
            } catch (e: Exception) {
                Typeface.DEFAULT
            }
        }
    }

    /**
     * 清空缓存（通常不需要调用）
     */
    fun clear() {
        cache.clear()
    }
}

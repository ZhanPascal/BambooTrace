package com.calligraphy.practice

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用Application类
 * 用于Hilt依赖注入初始化
 */
@HiltAndroidApp
class CalligraphyApp : Application()

package com.riseming.app.utils

import android.text.TextUtils
import android.util.Log
import com.example.designpatterns.BuildConfig
import java.util.*

object LogUtils {
    private const val CUSTOM_TAG = "RISE_MING"
    private fun log(tag: String, message: String?, level: LogLevel) {
        if (message == null || !BuildConfig.DEBUG) {
            return
        }
        when (level) {
            LogUtils.LogLevel.WARN -> Log.w(tag, message)
            LogUtils.LogLevel.ERROR -> Log.e(tag, message)
            LogUtils.LogLevel.DEBUG -> Log.d(tag, message)
        }
    }

    fun logW(message: String?, tag: String = generateTag(getStackTrace())) =
        log(tag, message, LogLevel.WARN)

    fun logE(message: String?, tag: String = generateTag(getStackTrace())) =
        log(tag, message, LogLevel.ERROR)

    fun logD(message: String?, tag: String = generateTag(getStackTrace())) =
        log(tag, message, LogLevel.DEBUG)

    enum class LogLevel {
        WARN,
        ERROR,
        DEBUG
    }

    private fun generateTag(caller: StackTraceElement): String {
        val formatString = "%s.%s(L:%d)"
        var callerClazzName = caller.className
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1)
        var log = String.format(
            Locale.getDefault(), formatString,
            callerClazzName, caller.methodName, caller.lineNumber
        )
        log = if (TextUtils.isEmpty(CUSTOM_TAG)) log else "$CUSTOM_TAG: $log"
        return log
    }

    private fun getStackTrace(): StackTraceElement {
        return Thread.currentThread().stackTrace[4]
    }
}
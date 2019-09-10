package com.riseming.app

import android.app.Application
import com.tencent.smtt.sdk.QbSdk

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        QbSdk.initX5Environment(this, null)
    }
}
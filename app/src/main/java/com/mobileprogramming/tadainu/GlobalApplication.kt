package com.mobileprogramming.tadainu

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.mobileprogramming.tadainu.homeFeat.data.InitialDataUtil
import data.Prefs

class GlobalApplication: Application() {
    companion object{
        lateinit var prefs: Prefs
    }
    override fun onCreate() {
        prefs= Prefs(applicationContext)
        super.onCreate()
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_KEY)
        InitialDataUtil.initializeData(this)
    }
}
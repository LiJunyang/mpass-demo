package com.hsbcd.mpaastest.kotlin.samples.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.annotation.IdRes
import androidx.multidex.MultiDex
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSONObject
import com.alipay.mobile.framework.quinoxless.QuinoxlessFramework
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.hsbc.kotlin.baselibs.config.AppConfig
import com.hsbc.kotlin.baselibs.utils.NLog
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName
import com.hsbcd.mpaastest.kotlin.samples.model.EnvInfo
import com.hsbcd.mpaastest.kotlin.samples.model.TntInfo
import com.hsbcd.mpaastest.kotlin.samples.util.ResourceUtil
import com.squareup.leakcanary.LeakCanary
import com.squareup.leakcanary.RefWatcher
import java.io.File

class ImplusApplication : Application() {

    private val TAG = "App"

    private var refWatcher: RefWatcher? = null

    companion object {
        var CURRENT_ACTIVITY: Activity? = null

        var SIMPLE_CACHE: SimpleCache? = null

        var ENV_INFO_LIST: List<EnvInfo>? = null

        var TNT_INFO_LIST: List<TntInfo>? = null

        var CURRENT_ENV_INFO: EnvInfo? = null

        var CURRENT_TNT_INFO: TntInfo? = null


        fun getEnvInfoList(): List<EnvInfo?>? {
            return ImplusApplication.ENV_INFO_LIST
        }

        fun getTntInfoList(): List<TntInfo?>? {
            return ImplusApplication.TNT_INFO_LIST
        }

        fun getCurrentEnvInfo(): EnvInfo? {
            return ImplusApplication.CURRENT_ENV_INFO
        }

        fun setCurrentEnvInfo(@IdRes id: Int) {
            CURRENT_ENV_INFO = ENV_INFO_LIST!!.stream().filter { info: EnvInfo -> info.id === id }.findFirst().get()
        }

        fun getCurrentTntInfo(): TntInfo? {
            return ImplusApplication.CURRENT_TNT_INFO
        }

        fun setCurrentTntInfo(@IdRes id: Int) {
            CURRENT_TNT_INFO = TNT_INFO_LIST!!.stream().filter { info: TntInfo -> info.id === id }
                .findFirst().get()
        }

        fun getCurrentActivity(): Activity? {
            return ImplusApplication.CURRENT_ACTIVITY
        }

        fun getSimpleCache(): SimpleCache? {
            return ImplusApplication.SIMPLE_CACHE
        }

        fun getRefWatcher(context: Context): RefWatcher? {
            val implusApplication = context.applicationContext as ImplusApplication
            return implusApplication.refWatcher
        }
    }


    override fun onCreate() {
        super.onCreate()
        initMpaas()
        initApp()
        initLeakCanary()
        initRouter()

        bindCurrentActivity()
        // 初始化exoplayer缓存，用于视频播放
        initExoPlayerCache()
        // 初始化登录环境信息
        initEnvInfo()
        initTntInfo()
    }

    private fun initApp() {
        AppConfig.init(this)
        AppConfig.openDebug()
    }

    private fun initLeakCanary() {
        refWatcher = if (LeakCanary.isInAnalyzerProcess(this))
            RefWatcher.DISABLED
        else LeakCanary.install(this)

        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    private fun initRouter() {
        ARouter.openLog()
        ARouter.openDebug()
        ARouter.init(this)
    }

    private fun initMpaas() {
        QuinoxlessFramework.init()
    }

    private fun setupMpaas() {
        QuinoxlessFramework.setup(this) {

        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
        setupMpaas()
    }

    private val mActivityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            NLog.d(TAG, "onCreated: " + activity.componentName.className)
        }

        override fun onActivityStarted(activity: Activity) {
            NLog.d(TAG, "onStart: " + activity.componentName.className)
        }

        override fun onActivityResumed(activity: Activity) {
            NLog.d(TAG, "onResume: " + activity.componentName.className)
        }

        override fun onActivityPaused(activity: Activity) {
            NLog.d(TAG, "onPause: " + activity.componentName.className)
        }

        override fun onActivityStopped(activity: Activity) {
            NLog.d(TAG, "onStop: " + activity.componentName.className)
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            NLog.d(TAG, "onSaveInstanceState: " + activity.componentName.className)
        }

        override fun onActivityDestroyed(activity: Activity) {
            NLog.d(TAG, "onDestroy: " + activity.componentName.className)
        }
    }


    private fun bindCurrentActivity() {
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {
                // 记录当前activity，用于全局弹窗
                Log.w(
                    LoggerName.UI,
                    "current activity is: " + activity.componentName.shortClassName
                )
                ImplusApplication.CURRENT_ACTIVITY = activity
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

    private fun initExoPlayerCache() {
        ImplusApplication.SIMPLE_CACHE = SimpleCache(
            File(this.cacheDir, "media"),
            LeastRecentlyUsedCacheEvictor(1024 * 1024 * 1024),
            StandaloneDatabaseProvider(this)
        )
    }

    private fun initEnvInfo() {
        val envInfoListStr: String = ResourceUtil.readJsonFile(this, "env_info_list.json")
        ImplusApplication.ENV_INFO_LIST = JSONObject.parseArray(
            envInfoListStr,
            EnvInfo::class.java
        )
        ImplusApplication.CURRENT_ENV_INFO = ImplusApplication.ENV_INFO_LIST?.get(0)
    }

    private fun initTntInfo() {
        val tntInfoListStr: String = ResourceUtil.readJsonFile(this, "tnt_info_list.json")
        ImplusApplication.TNT_INFO_LIST = JSONObject.parseArray(
            tntInfoListStr,
            TntInfo::class.java
        )
        ImplusApplication.CURRENT_TNT_INFO = ImplusApplication.TNT_INFO_LIST?.get(0)
    }

}
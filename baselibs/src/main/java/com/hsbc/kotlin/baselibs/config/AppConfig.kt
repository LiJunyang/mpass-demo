package com.hsbc.kotlin.baselibs.config

import android.app.Application

object AppConfig {

    const val TAG = "AppConfig"

    var debug = false

    private var application: Application? = null

    /**
     * Init, it must be call before used .
     */
    fun init(application: Application) {
        AppConfig.application = application
    }

    fun getApplication(): Application {
        if (application == null) {
            throw RuntimeException("Please init in Application#onCreate first.")
        }
        return application!!
    }

    fun openDebug() {
        debug = true
    }

}
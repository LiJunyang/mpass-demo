package com.hsbc.kotlin.baselibs.widget

import android.view.View

abstract class OnNoDoubleClickListener : View.OnClickListener {

    private var mThrottleFirstTime: Long = 1000
    private var mLastClickTime: Long = 0

    constructor()
    constructor(throttleFirstTime: Long) {
        mThrottleFirstTime = throttleFirstTime
    }

    override fun onClick(v: View?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - mLastClickTime > mThrottleFirstTime) {
            mLastClickTime = currentTime
            onNoDoubleClick(v)
        }
    }

    abstract fun onNoDoubleClick(v: View?)

}
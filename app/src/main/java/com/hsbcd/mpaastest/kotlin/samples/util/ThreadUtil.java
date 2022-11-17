/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import android.os.Looper;

/**
 * @author liyalong
 * @version ThreadUtil.java, v 0.1 2022年09月07日 21:47 liyalong
 */
public class ThreadUtil {

    /**
     * 判断当前线程是否为UI主线程
     *
     * @return
     */
    public static boolean isUiThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }
}
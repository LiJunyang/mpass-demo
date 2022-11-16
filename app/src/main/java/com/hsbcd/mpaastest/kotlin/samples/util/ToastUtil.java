/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import android.app.Activity;

import com.alipay.fc.ccmimplus.sdk.core.util.UIUtils;

import org.apache.commons.lang3.StringUtils;

/**
 * toast工具类
 *
 * @author maping.mp
 * @version 1.0: ToatUtils.java, v 0.1 2021年07月14日 3:35 下午 maping.mp Exp $
 */
public class ToastUtil {

    /**
     * 执行toast
     *
     * @param context
     * @param message
     * @param time
     */
    public static void makeToast(Activity context, String message, int time) {
        if (StringUtils.isBlank(message)) {
            return;
        }

        context.runOnUiThread(() -> UIUtils.makeText(context, message, time));
    }
}
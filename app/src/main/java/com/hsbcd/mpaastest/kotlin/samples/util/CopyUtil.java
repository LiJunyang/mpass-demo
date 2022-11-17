/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * @author liyalong
 * @version CopyUtil.java, v 0.1 2022年08月24日 15:28 liyalong
 */
public class CopyUtil {

    /**
     * 拷贝文本到剪切板
     *
     * @param context 上下文
     * @param text    文本
     */
    public static void copyToClipboard(Context context, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

}
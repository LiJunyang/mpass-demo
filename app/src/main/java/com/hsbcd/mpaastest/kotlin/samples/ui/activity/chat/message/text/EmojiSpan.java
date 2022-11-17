/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.text;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;

/**
 * @author liyalong
 * @version EmojiSpan.java, v 0.1 2022年10月14日 13:59 liyalong
 */
public class EmojiSpan extends ImageSpan {

    private String emojiStr;

    public EmojiSpan(@NonNull Context context, @NonNull Bitmap bitmap, int verticalAlignment, String emojiStr) {
        super(context, bitmap, verticalAlignment);
        this.emojiStr = emojiStr;
    }

    public String getEmojiStr() {
        return emojiStr;
    }
}
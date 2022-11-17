/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.text;

import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.NonNull;

/**
 * @author liyalong
 * @version AtUserSpan.java, v 0.1 2022年10月14日 14:48 liyalong
 */
public class AtUserSpan extends MetricAffectingSpan {

    private String atUserId;

    private String atUserName;

    public AtUserSpan(String atUserId, String atUserName) {
        this.atUserId = atUserId;
        this.atUserName = atUserName;
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint textPaint) {
    }

    @Override
    public void updateDrawState(TextPaint tp) {
    }

    public String getAtUserId() {
        return atUserId;
    }

    public String getAtUserName() {
        return atUserName;
    }
}
/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.richtext;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.RichTextContent;
import com.alipay.fc.ccmimplus.sdk.core.util.DensityUtils;


/**
 * 富文本消息，支持展示表情图标
 *
 * @author maping.mp
 * @version 1.0: CustomTextView.java, v 0.1 2021年11月05日 3:41 下午 maping.mp Exp $
 */
public class RichTextView extends WebView {

    private Context context;

    public RichTextView(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    public RichTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public void rend(RichTextContent richTxtContent) {
        this.loadData(richTxtContent.getText(), "text/html", "utf-8");
        int width = (int) (0.6F * DensityUtils.getDisplayWidth(context));
        setLayoutParams(new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT));
    }
}
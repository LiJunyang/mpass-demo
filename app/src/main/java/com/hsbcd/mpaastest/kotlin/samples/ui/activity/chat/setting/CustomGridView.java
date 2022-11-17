/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

/**
 * 自定义的网格视图
 *
 * @author liyalong
 * @version CustomGridView.java, v 0.1 2022年08月11日 19:21 liyalong
 */
public class CustomGridView extends GridView {

    public CustomGridView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomGridView(Context context) {
        super(context);
    }

    public CustomGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 重写该方法，解决ScrollView内部嵌套GridView的情况下，GridView默认显示不全的问题
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandSpec = MeasureSpec.makeMeasureSpec(
                Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);
    }
}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;
import android.widget.ViewFlipper;

import cn.com.hsbc.hsbcchina.cert.R;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;

/**
 * 滑动翻页的ViewFlipper
 *
 * @author liyalong
 * @version SlideViewFlipper.java, v 0.1 2022年09月05日 16:24 liyalong
 */
public class SlideViewFlipper extends ViewFlipper implements GestureDetector.OnGestureListener {

    private Context context;

    private GestureDetector gestureDetector;

    public SlideViewFlipper(Context context) {
        super(context);

        this.context = context;
        initGestureDetector(context);
    }

    public SlideViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        initGestureDetector(context);
    }

    private void initGestureDetector(Context context) {
        this.gestureDetector = new GestureDetector(context, this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        this.gestureDetector.onTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        float distance = e1.getX() - e2.getX();
        Log.d(LoggerName.UI, "onFling: " + distance);

        // 从右向左滑，翻到下一页
        if (distance > 0) {
            goToNextPage();
            return true;
        }
        // 从左向右滑，翻到上一页
        else if (distance < 0) {
            goToPreviousPage();
            return true;
        }

        return false;
    }

    public void goToNextPage() {
        this.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.push_left_in));
        this.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.push_left_out));
        this.showNext();
    }

    public void goToPreviousPage() {
        this.setInAnimation(AnimationUtils.loadAnimation(context, R.anim.push_right_in));
        this.setOutAnimation(AnimationUtils.loadAnimation(context, R.anim.push_right_out));
        this.showPrevious();
    }

}
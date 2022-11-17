/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;

/**
 * 消息列表视图
 *
 * @author liyalong
 * @version MessageListView.java, v 0.1 2022年09月05日 15:40 liyalong
 */
public class MessageListView extends RecyclerView implements GestureDetector.OnGestureListener {

    private GestureDetector gestureDetector;

    private OnClickListListener listener;

    public MessageListView(@NonNull Context context) {
        super(context);
        initGestureDetector(context);
    }

    public MessageListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initGestureDetector(context);
    }

    public MessageListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initGestureDetector(context);
    }

    /**
     * 通过监听GestureDetector的单击事件，解决RecyclerView.setOnClickListener()无效的问题
     * 参考：https://blog.csdn.net/lty406910111/article/details/79239176
     *
     * @param context
     */
    private void initGestureDetector(Context context) {
        gestureDetector = new GestureDetector(context, this);

        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
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
        // 回调单击事件
        if (this.listener != null) {
            this.listener.onClick();
        }

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
        return false;
    }

    public void setOnClickListListener(OnClickListListener listener) {
        this.listener = listener;
    }

    public interface OnClickListListener {
        void onClick();
    }

//    @Override
//    public void onScrolled(int dx, int dy) {
//        super.onScrolled(dx, dy);
//        Log.e(LoggerName.UI, "onScrolled: " + dx + "/" + dy);
//
//        if (dx == 0 && dy == 0) {
//            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
//            int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
//            Log.e(LoggerName.UI, lastPosition + "/" + (getAdapter().getItemCount() - 1));
//            if (lastPosition != getAdapter().getItemCount() - 1) {
//                //Stream.of(Thread.currentThread().getStackTrace()).forEach(System.out::println);
//                //scrollToBottom();
//            }
//        }
//    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        scrollToBottom();
    }

    public void scrollToBottom() {
        scrollTo(getAdapter().getItemCount() - 1);
    }

    public void scrollToTop(int loadMessageCount) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();

        // 滚动到刚好能显示出最后一条刚加载到的历史消息的位置，用户感官是列表往上滚动了一行
        int lastPosition = linearLayoutManager.findLastVisibleItemPosition();
        scrollTo(loadMessageCount + lastPosition - 1);
    }

    public void scrollTo(int position) {
        post(() -> {
            Log.e(LoggerName.UI, "scrollToPosition: " + position);
            scrollToPosition(position);

//            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
//            View target = linearLayoutManager.findViewByPosition(position);
//            if (target != null) {
//                int offset = getMeasuredHeight() - target.getMeasuredHeight();
//                linearLayoutManager.scrollToPositionWithOffset(position, offset);
//            }

//            LinearLayoutManager linearLayoutManager = (LinearLayoutManager) getLayoutManager();
//            linearLayoutManager.scrollToPositionWithOffset(position, Integer.MIN_VALUE);
        });
    }

}
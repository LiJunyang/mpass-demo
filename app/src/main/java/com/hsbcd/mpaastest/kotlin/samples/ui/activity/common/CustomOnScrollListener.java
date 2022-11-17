/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import android.util.Log;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;

/**
 * 列表上滑/下滑监听器
 *
 * @author liyalong
 * @version CustomOnScrollListener.java, v 0.1 2022年08月15日 13:41 liyalong
 */
public abstract class CustomOnScrollListener extends RecyclerView.OnScrollListener {

    /**
     * 当前已加载的页码
     */
    private int pageIndex = 1;

    /**
     * 是否正在上滑标记
     */
    boolean isUpScroll = false;

    /**
     * 是否还有下一页
     */
    boolean hasNextPage = true;

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        // 大于0表示正在向上滑动，小于等于0表示停止或向下滑动
        isUpScroll = dy > 0;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

        // 停止滑动时再处理
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            // 当前列表的总条数
            int itemCount = manager.getItemCount();

            // 当前第一条完整可见，且最近动作为下滑，则触发刷新第一页
            int fristItemPosition = manager.findFirstCompletelyVisibleItemPosition();
            if (fristItemPosition == 0 && !isUpScroll) {
                pageIndex = 1;
                Log.i(LoggerName.UI, "CustomOnScrollListener: refresh");
                onRefresh();
            }

            // 当前最后一条完整可见，且最近动作为上滑，则触发加载下一页
            int lastItemPosition = manager.findLastCompletelyVisibleItemPosition();
            if (lastItemPosition == (itemCount - 1) && isUpScroll) {
                if (hasNextPage) {
                    pageIndex++;
                    Log.i(LoggerName.UI, "CustomOnScrollListener: load more, pageIndex=" + pageIndex);
                    onLoadMore(pageIndex);
                } else {
                    Log.i(LoggerName.UI, "CustomOnScrollListener: has no next page, maxPageIndex=" + pageIndex);
                    onNoNextPage();
                }
            }
        }
    }

    /**
     * 刷新第一页
     */
    public abstract void onRefresh();

    /**
     * 加载下一页
     *
     * @param pageIndex
     */
    public abstract void onLoadMore(int pageIndex);

    /**
     * 没有下一页
     */
    public abstract void onNoNextPage();

    /**
     * 设置是否还有下一页
     *
     * @param hasNextPage
     */
    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    /**
     * 重置页码
     */
    public void reset() {
        this.pageIndex = 1;
        this.hasNextPage = true;
    }
}
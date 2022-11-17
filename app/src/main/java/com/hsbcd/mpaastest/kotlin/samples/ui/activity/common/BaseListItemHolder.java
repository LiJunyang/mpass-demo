/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

/**
 * 列表项holder基类
 *
 * @author liyalong
 * @version BaseListItemHolder.java, v 0.1 2022年10月18日 11:27 liyalong
 */
public abstract class BaseListItemHolder<T> extends RecyclerView.ViewHolder {

    protected Context context;

    protected OnItemActionListener onItemActionListener;

    public BaseListItemHolder(Context context, View itemView) {
        super(itemView);
        this.context = context;
    }

    public BaseListItemHolder(Context context, View itemView, OnItemActionListener onItemActionListener) {
        this(context, itemView);
        this.onItemActionListener = onItemActionListener;
    }

    public abstract void bind(T item);

    public interface OnItemActionListener<T> {
        default void onClickItem(T item) {
        }

        default void onRemoveItem(T item, int position) {
        }
    }

}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * 列表adapter基类
 *
 * @author liyalong
 * @version BaseListAdapter.java, v 0.1 2022年10月18日 11:20 liyalong
 */
public abstract class BaseListAdapter<T, H extends BaseListItemHolder<T>> extends RecyclerView.Adapter<H> {

    protected Context context;

    protected List<T> items = Lists.newArrayList();

    protected BaseListItemHolder.OnItemActionListener<T> onItemActionListener;

    public BaseListAdapter(Context context) {
        this.context = context;
    }

    public BaseListAdapter(Context context, BaseListItemHolder.OnItemActionListener<T> onItemActionListener) {
        this.context = context;
        this.onItemActionListener = onItemActionListener;
    }

    @NonNull
    @Override
    public H onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return getViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull H holder, int position) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        T item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    protected abstract H getViewHolder(ViewGroup parent, int viewType);

    public void addAll(List<T> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        items.addAll(list);
    }

    public void clearAll() {
        items.clear();
    }

    public void addItem(T item) {
        items.add(item);
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }

}
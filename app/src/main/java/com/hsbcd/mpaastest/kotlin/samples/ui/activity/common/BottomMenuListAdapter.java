/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.hsbcsd.mpaastest.R;


/**
 * 底部弹出菜单项列表
 *
 * @author liyalong
 * @version BottomMenuListAdapter.java, v 0.1 2022年08月01日 20:11 liyalong
 */
public class BottomMenuListAdapter extends BaseListAdapter<BottomMenuDialog.MenuItem, BottomMenuItemHolder> {

    private BottomMenuDialog dialog;

    public BottomMenuListAdapter(Context context, BottomMenuDialog dialog) {
        super(context);
        this.dialog = dialog;
    }

    @Override
    protected BottomMenuItemHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bottom_menu_item, parent,
                false);
        return new BottomMenuItemHolder(context, view, dialog);
    }

}
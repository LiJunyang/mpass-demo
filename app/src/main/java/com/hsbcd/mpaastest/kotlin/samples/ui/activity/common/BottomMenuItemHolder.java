/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import android.content.Context;
import android.view.View;

import cn.hsbcsd.mpaastest.databinding.FragmentBottomMenuItemBinding;


/**
 * 底部弹出菜单项
 *
 * @author liyalong
 * @version BottomMenuItemHolder.java, v 0.1 2022年10月20日 16:46 liyalong
 */
public class BottomMenuItemHolder extends BaseListItemHolder<BottomMenuDialog.MenuItem> {

    private BottomMenuDialog dialog;

    private FragmentBottomMenuItemBinding binding;

    public BottomMenuItemHolder(Context context, View itemView, BottomMenuDialog dialog) {
        super(context, itemView);

        this.dialog = dialog;
        this.binding = FragmentBottomMenuItemBinding.bind(itemView);
    }

    @Override
    public void bind(BottomMenuDialog.MenuItem item) {
        binding.label.setText(item.getLabel());

        if (item.getOnClickItemListener() != null) {
            binding.getRoot().setOnClickListener(v -> {
                item.getOnClickItemListener().onClickItem();
                dialog.dismiss();
            });
        }
    }

}
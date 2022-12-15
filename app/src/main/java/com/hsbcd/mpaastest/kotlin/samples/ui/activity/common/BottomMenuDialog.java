/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;


import org.apache.commons.lang3.StringUtils;

import java.util.List;

import cn.hsbcsd.mpaastest.databinding.DialogBottomMenuBinding;

/**
 * @author liyalong
 * @version BottomMenuDialog.java, v 0.1 2022年10月20日 16:26 liyalong
 */
public class BottomMenuDialog extends BaseBottomSheetDialog {

    private DialogBottomMenuBinding binding;

    private String title;

    private List<MenuItem> menuItems;

    private boolean showCancelItem;

    public BottomMenuDialog(String title, List<MenuItem> menuItems, boolean showCancelItem) {
        super(true);
        
        this.title = title;
        this.menuItems = menuItems;
        this.showCancelItem = showCancelItem;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogBottomMenuBinding.inflate(inflater);

        bindAction();

        initMenuList();

        return binding.getRoot();
    }

    private void bindAction() {
        if (StringUtils.isBlank(title)) {
            binding.titleLayout.setVisibility(View.GONE);
        } else {
            binding.title.setText(title);
        }

        binding.cancelButton.setVisibility(showCancelItem ? View.VISIBLE : View.GONE);
        binding.cancelButton.setOnClickListener(v -> super.dismiss());
    }

    private void initMenuList() {
        BottomMenuListAdapter listAdapter = new BottomMenuListAdapter(getContext(), this);
        binding.menuListView.setAdapter(listAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.menuListView.setLayoutManager(layoutManager);

        listAdapter.setItems(menuItems);
        listAdapter.notifyDataSetChanged();
    }

    public static class MenuItem {

        private String label;

        private OnClickItemListener onClickItemListener;

        public MenuItem(String label, OnClickItemListener onClickItemListener) {
            this.label = label;
            this.onClickItemListener = onClickItemListener;
        }

        public String getLabel() {
            return label;
        }

        public OnClickItemListener getOnClickItemListener() {
            return onClickItemListener;
        }
    }

    public interface OnClickItemListener {
        void onClickItem();
    }

}
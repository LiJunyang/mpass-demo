/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.home;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import cn.hsbcsd.mpaastest.R;
import cn.hsbcsd.mpaastest.databinding.ActivityHomeBinding;


/**
 * 首页，包括会话列表，通讯录，我的设置等tab页
 *
 * @author liyalong
 * @version HomeActivity.java, v 0.1 2022年07月28日 23:05 liyalong
 */
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindAction();

        initHomeViewPager();
    }

    private void bindAction() {
        binding.messageTabGroup.setOnClickListener(v -> doSwitchTab(v));
        binding.contactsTabGroup.setOnClickListener(v -> doSwitchTab(v));
        binding.mineTabGroup.setOnClickListener(v -> doSwitchTab(v));
    }

    private void initHomeViewPager() {
        // init fragment adapter
        HomeFragmentAdapter fragmentAdapter = new HomeFragmentAdapter(this);
        fragmentAdapter.initFragmentList();

        // init view pager
        binding.homeViewPager.setUserInputEnabled(false);
        binding.homeViewPager.setAdapter(fragmentAdapter);
        binding.homeViewPager.setOffscreenPageLimit(5);

        switchToMessageTab(true);
    }

    private void doSwitchTab(View view) {
        resetTabStyle();

        if (view == binding.messageTabGroup) {
            switchToMessageTab(true);
        } else if (view == binding.contactsTabGroup) {
            switchToContactsTab(true);
        } else if (view == binding.mineTabGroup) {
            switchToMineTab(true);
        }
    }

    private void resetTabStyle() {
        setMessageTabStyle(false);
        setContactsTabStyle(false);
        setMineTabStyle(false);
    }

    private void switchToMessageTab(boolean checked) {
        binding.homeViewPager.setCurrentItem(0);
        setMessageTabStyle(checked);
    }

    private void switchToContactsTab(boolean checked) {
        binding.homeViewPager.setCurrentItem(1);
        setContactsTabStyle(checked);
    }

    private void switchToMineTab(boolean checked) {
        binding.homeViewPager.setCurrentItem(2);
        setMineTabStyle(checked);
    }

    private void setMessageTabStyle(boolean checked) {
        binding.messageTabLabel.setTextColor(getTabColor(checked));
        setTextDrawableColor(binding.messageTabLabel, checked);
    }

    private void setContactsTabStyle(boolean checked) {
        binding.contactsTabLabel.setTextColor(getTabColor(checked));
        setTextDrawableColor(binding.contactsTabLabel, checked);
    }

    private void setMineTabStyle(boolean checked) {
        binding.mineTabLabel.setTextColor(getTabColor(checked));
        setTextDrawableColor(binding.mineTabLabel, checked);
    }

    private void setTextDrawableColor(TextView textView, boolean checked) {
        for (Drawable drawable : textView.getCompoundDrawables()) {
            if (drawable != null) {
                drawable.setColorFilter(getTabColor(checked), PorterDuff.Mode.SRC_IN);
            }
        }
    }

    private int getTabColor(boolean checked) {
        return getResources().getColor(checked ? R.color.ant_blue : R.color.dark_gray);
    }


}
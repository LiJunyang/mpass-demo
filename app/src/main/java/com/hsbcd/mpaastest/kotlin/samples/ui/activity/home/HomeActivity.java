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
import androidx.lifecycle.ViewModelProvider;

import com.hsbcd.mpaastest.kotlin.samples.ui.activity.me.MineViewModel;

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

    private MineViewModel mineViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindAction();

        initHomeViewPager();

        bindMineViewModel();
    }


    private void bindMineViewModel() {
        mineViewModel = new ViewModelProvider(this).get(MineViewModel.class);

        mineViewModel.getMineUserInfo().observe(this, data -> {

        });
        mineViewModel.loadUserInfo();
    }

    private void bindAction() {
        binding.discoverTabGroup.setOnClickListener(v -> doSwitchTab(v));
        binding.communityTabGroup.setOnClickListener(v -> doSwitchTab(v));
        binding.chatTabGroup.setOnClickListener(v -> doSwitchTab(v));
        binding.insightsTabGroup.setOnClickListener(v -> doSwitchTab(v));
        binding.meTabGroup.setOnClickListener(v -> doSwitchTab(v));
    }

    private void initHomeViewPager() {
        // init fragment adapter
        HomeFragmentAdapter fragmentAdapter = new HomeFragmentAdapter(this);
        fragmentAdapter.initFragmentList();

        // init view pager
        binding.homeViewPager.setUserInputEnabled(false);
        binding.homeViewPager.setAdapter(fragmentAdapter);
        binding.homeViewPager.setOffscreenPageLimit(5);

        resetTabStyle();
        switchToChatTab(true);
    }

    private void doSwitchTab(View view) {
        resetTabStyle();
        if (view == binding.discoverTabGroup) {
            switchToDiscoverTab(true);
        } if (view == binding.communityTabGroup) {
            switchToCommunityTab(true);
        } if (view == binding.chatTabGroup) {
            switchToChatTab(true);
        } else if (view == binding.insightsTabGroup) {
            switchToInsightsTab(true);
        } else if (view == binding.meTabGroup) {
            switchToMeTab(true);
        }
    }

    private void resetTabStyle() {
        setDiscoverTabStyle(false);
        setCommunityTabStyle(false);
        setChatTabStyle(false);
        setInsightsTabStyle(false);
        setMeTabStyle(false);
    }

    private void switchToDiscoverTab(boolean checked) {
        binding.homeViewPager.setCurrentItem(0);
        setDiscoverTabStyle(checked);
    }

    private void switchToCommunityTab(boolean checked) {
        binding.homeViewPager.setCurrentItem(1);
        setCommunityTabStyle(checked);
    }

    private void switchToChatTab(boolean checked) {
        binding.homeViewPager.setCurrentItem(2);
        setChatTabStyle(checked);
    }

    private void switchToInsightsTab(boolean checked) {
        binding.homeViewPager.setCurrentItem(3);
        setInsightsTabStyle(checked);
    }

    private void switchToMeTab(boolean checked) {
        binding.homeViewPager.setCurrentItem(4);
        setMeTabStyle(checked);
    }

    private void setDiscoverTabStyle(boolean checked) {
        binding.discoverTabLabel.setTextColor(getTabColor(checked));
        setTextDrawableColor(binding.discoverTabLabel, checked);
    }

    private void setCommunityTabStyle(boolean checked) {
        binding.communityTabLabel.setTextColor(getTabColor(checked));
        setTextDrawableColor(binding.communityTabLabel, checked);
    }

    private void setChatTabStyle(boolean checked) {
        binding.chatTabLabel.setTextColor(getTabColor(checked));
        setTextDrawableColor(binding.chatTabLabel, checked);
    }

    private void setInsightsTabStyle(boolean checked) {
        binding.insightsTabLabel.setTextColor(getTabColor(checked));
        setTextDrawableColor(binding.insightsTabLabel, checked);
    }

    private void setMeTabStyle(boolean checked) {
        binding.meTabLabel.setTextColor(getTabColor(checked));
        setTextDrawableColor(binding.meTabLabel, checked);
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
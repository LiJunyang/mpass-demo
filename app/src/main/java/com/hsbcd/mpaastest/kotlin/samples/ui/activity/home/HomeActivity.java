/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.home;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.hsbcd.mpaastest.kotlin.samples.constants.ARouterPath;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.me.MineViewModel;

import cn.hsbcsd.mpaastest.R;
import cn.hsbcsd.mpaastest.databinding.ActivityHomeBinding;


/**
 * 首页，包括会话列表，通讯录，我的设置等tab页
 *
 * @author liyalong
 * @version HomeActivity.java, v 0.1 2022年07月28日 23:05 liyalong
 */
@Route(path = ARouterPath.home)
public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;

    private MineViewModel mineViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARouter.getInstance().inject(this);
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

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setDiscoverTabStyle(boolean checked) {
        if(checked){
            binding.discoverTabLabel.setImageDrawable(getDrawable(R.mipmap.bg_discover_tab_active));
        } else {
            binding.discoverTabLabel.setImageDrawable(getDrawable(R.mipmap.bg_discover_tab));
        }
        //setTextDrawableColor(binding.discoverTabLabel, checked);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setCommunityTabStyle(boolean checked) {

        if(checked){
            binding.communityTabLabel.setImageDrawable(getDrawable(R.mipmap.bg_community_tab_active));
        } else {
            binding.communityTabLabel.setImageDrawable(getDrawable(R.mipmap.bg_community_tab));
        }
//        binding.communityTabLabel.setTextColor(getTabColor(checked));
//        setTextDrawableColor(binding.communityTabLabel, checked);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setChatTabStyle(boolean checked) {

        if(checked){
            binding.chatTabLabel.setImageDrawable(getDrawable(R.mipmap.bg_chat_tab_active));
        } else {
            binding.chatTabLabel.setImageDrawable(getDrawable(R.mipmap.bg_chat_tab));
        }
       // binding.chatTabLabel.setTextColor(getTabColor(checked));
        //setTextDrawableColor(binding.chatTabLabel, checked);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setInsightsTabStyle(boolean checked) {

        if(checked){
            binding.insightsTabLabel.setImageDrawable(getDrawable(R.mipmap.bg_insights_tab_active));
        } else {
            binding.insightsTabLabel.setImageDrawable(getDrawable(R.mipmap.bg_insights_tab));
        }
       // binding.insightsTabLabel.setTextColor(getTabColor(checked));
        //setTextDrawableColor(binding.insightsTabLabel, checked);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void setMeTabStyle(boolean checked) {

        if(checked){
            binding.meTabLabel.setImageDrawable(getDrawable(R.mipmap.bg_me_tab_active));
        } else {
            binding.meTabLabel.setImageDrawable(getDrawable(R.mipmap.bg_me_tab));
        }
       // binding.meTabLabel.setTextColor(getTabColor(checked));
        //setTextDrawableColor(binding.meTabLabel, checked);
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
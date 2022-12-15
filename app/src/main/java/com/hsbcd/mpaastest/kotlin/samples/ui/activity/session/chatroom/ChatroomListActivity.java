/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import cn.hsbcsd.mpaastest.R;
import cn.hsbcsd.mpaastest.databinding.ActivityChatroomListBinding;

/**
 * 聊天室列表页
 *
 * @author liyalong
 * @version ChatroomListActivity.java, v 0.1 2022年11月08日 19:00 liyalong
 */
public class ChatroomListActivity extends AppCompatActivity {

    private ActivityChatroomListBinding binding;

    private ChatroomViewModel chatroomViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatroomListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindAction();

        bindChatroomViewModel();

        initChatroomListViewPager();

        bindChatroomListTab();
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());
    }

    private void bindChatroomViewModel() {
        chatroomViewModel = new ViewModelProvider(this).get(ChatroomViewModel.class);
    }

    private void initChatroomListViewPager() {
        ChatroomListFragmentAdapter fragmentAdapter = new ChatroomListFragmentAdapter(this);
        binding.chatroomListViewPager.setAdapter(fragmentAdapter);

        binding.chatroomListViewPager.setUserInputEnabled(false);
        binding.chatroomListViewPager.setOffscreenPageLimit(3);
    }

    private void bindChatroomListTab() {
        resetTabStyle();
        switchToAllChatroomTab(true);

        binding.allChatroomTabLayout.setOnClickListener(v -> {
            resetTabStyle();
            switchToAllChatroomTab(true);
        });

        binding.recentJoinedChatroomTabLayout.setOnClickListener(v -> {
            resetTabStyle();
            switchToRecentJoinedChatroomTab(true);
        });

        binding.adminChatroomTabLayout.setOnClickListener(v -> {
            resetTabStyle();
            switchToAdminChatroomTab(true);
        });
    }

    private void resetTabStyle() {
        switchToAllChatroomTab(false);
        switchToRecentJoinedChatroomTab(false);
        switchToAdminChatroomTab(false);
    }

    private void switchToAllChatroomTab(boolean checked) {
        binding.allChatroomTabLabel.setTextColor(getTabColor(checked));
        binding.allChatroomTabUnderline.setVisibility(checked ? View.VISIBLE : View.GONE);

        if (checked) {
            binding.chatroomListViewPager.setCurrentItem(0);
        }
    }

    private void switchToRecentJoinedChatroomTab(boolean checked) {
        binding.recentJoinedChatroomTabLabel.setTextColor(getTabColor(checked));
        binding.recentJoinedChatroomTabUnderline.setVisibility(checked ? View.VISIBLE : View.GONE);

        if (checked) {
            binding.chatroomListViewPager.setCurrentItem(1);
        }
    }

    private void switchToAdminChatroomTab(boolean checked) {
        binding.adminChatroomTabLabel.setTextColor(getTabColor(checked));
        binding.adminChatroomTabUnderline.setVisibility(checked ? View.VISIBLE : View.GONE);

        if (checked) {
            binding.chatroomListViewPager.setCurrentItem(2);
        }
    }

    private int getTabColor(boolean checked) {
        return getResources().getColor(checked ? R.color.ant_blue : R.color.black);
    }

}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author liyalong
 * @version ChatroomListFragmentAdapter.java, v 0.1 2022年11月09日 10:48 liyalong
 */
public class ChatroomListFragmentAdapter extends FragmentStateAdapter {

    private List<Fragment> fragments = Lists.newArrayList();

    public ChatroomListFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        initFragments();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    private void initFragments() {
        fragments.add(new AllChatroomListFragment());
        fragments.add(new RecentJoinedChatroomListFragment());
        fragments.add(new AdminChatroomListFragment());
    }

}
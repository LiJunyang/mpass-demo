/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.google.common.collect.Lists;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.community.CommunityFragment;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.discover.DiscoverFragment;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.insights.InsightsFragment;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.me.MeFragment;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.SessionFragment;

import java.util.List;

/**
 * @author liyalong
 * @version HomeActivity.java, v 0.1 2022年07月28日 23:05 liyalong
 */
public class HomeFragmentAdapter extends FragmentStateAdapter {

    private List<Fragment> fragmentList = Lists.newArrayList();

    public HomeFragmentAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public HomeFragmentAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    public HomeFragmentAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (fragmentList == null || fragmentList.size() <= position) {
            return new Fragment();
        }
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        return fragmentList == null ? 0 : fragmentList.size();
    }

    public void initFragmentList() {
        fragmentList.add(new DiscoverFragment());
        fragmentList.add(new CommunityFragment());
        fragmentList.add(new SessionFragment());
        fragmentList.add(new InsightsFragment());
        fragmentList.add(new MeFragment());
    }

}

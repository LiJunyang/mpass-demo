/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import cn.com.hsbc.hsbcchina.cert.databinding.ActivityNewFriendBinding;

/**
 * 新的朋友
 *
 * @author liyalong
 * @version NewFriendActivity.java, v 0.1 2022年08月02日 11:02 liyalong
 */
public class NewFriendActivity extends AppCompatActivity {

    private ActivityNewFriendBinding binding;

    private NewFriendListAdapter newFriendListAdapter;

    private ContactsViewModel contactsViewModel;

    private CustomOnScrollListener customOnScrollListener;

    private boolean refresh = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityNewFriendBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goBack.setOnClickListener(v -> super.onBackPressed());
        binding.addFriend.setOnClickListener(v -> onClickAddFriend());

        contactsViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);

        contactsViewModel.getFriendListResult().observe(this, result -> {
            binding.refreshLayout.setRefreshing(false);

            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            customOnScrollListener.setHasNextPage(result.hasNextPage());

            if (CollectionUtils.isEmpty(result.getData())) {
                return;
            }

            if (refresh) {
                newFriendListAdapter.clearAll();
                refresh = false;
            }

            newFriendListAdapter.addAll(result.getData());
            newFriendListAdapter.notifyDataSetChanged();
        });

        initNewFriendList();

        contactsViewModel.queryNewFriends(1);
    }

    private void onClickAddFriend() {
        Intent intent = new Intent(this, AddNewFriendActivity.class);
        this.startActivity(intent);
    }

    private void initNewFriendList() {
        this.newFriendListAdapter = new NewFriendListAdapter(this);
        binding.newFriendListView.setAdapter(newFriendListAdapter);

        binding.newFriendListView.setLayoutManager(new LinearLayoutManager(this));

        binding.refreshLayout.setOnRefreshListener(() -> {
            refreshNewFriendList();
        });

        customOnScrollListener = new CustomOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
                contactsViewModel.queryNewFriends(pageIndex);
            }

            @Override
            public void onRefresh() {
                //refreshNewFriendList();
            }

            @Override
            public void onNoNextPage() {
                ToastUtil.makeToast(NewFriendActivity.this, "No more new friend", 1000);
            }
        };

        binding.newFriendListView.addOnScrollListener(customOnScrollListener);
    }

    private void refreshNewFriendList() {
        refresh = true;
        customOnScrollListener.reset();
        contactsViewModel.queryNewFriends(1);
    }

}
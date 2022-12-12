/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.user;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.SelectUserViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts.ContactsViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.SelectUserListAdapter;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import java.util.Collection;

import cn.hsbcsd.mpaastest.databinding.ActivitySelectUserListBinding;

/**
 * 选择用户列表类页面的基类
 *
 * @author liyalong
 * @version SelectUserListActivity.java, v 0.1 2022年08月08日 15:38 liyalong
 */
public abstract class SelectUserListActivity extends AppCompatActivity {

    protected ActivitySelectUserListBinding binding;

    protected SelectUserListAdapter selectUserListAdapter;

    protected ContactsViewModel contactsViewModel;

    protected SelectUserViewModel selectItemViewModel;

    private CustomOnScrollListener customOnScrollListener;

    private boolean refresh = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySelectUserListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goBack.setOnClickListener(v -> super.onBackPressed());
        binding.confirmButton.setEnabled(false);
        binding.confirmButton.setOnClickListener(v -> {
            Collection<UserInfoVO> selectedUsers = selectItemViewModel.getSelectedItems();
            onClickConfirm(selectedUsers);
        });

        // 绑定联系人数据
        contactsViewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        bindContactsViewModel();

        // 绑定选择用户数据
        selectItemViewModel = new ViewModelProvider(this).get(SelectUserViewModel.class);
        bindSelectItemViewModel();

        initAllContactsList();

        contactsViewModel.queryAllContacts(1);
    }

    /**
     * 是否为单选模式
     *
     * @return
     */
    public abstract boolean isSingleSelectMode();

    /**
     * 单个用户的点击事件，仅在单选模式下有效
     *
     * @param userInfoVO
     */
    public void onClickSingleUser(UserInfoVO userInfoVO) {
    }

    /**
     * 确认按钮的点击事件，仅在多选模式下有效
     *
     * @param selectedUsers
     */
    protected void onClickConfirm(Collection<UserInfoVO> selectedUsers) {
    }

    private void bindContactsViewModel() {
        contactsViewModel.getUserInfoListResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            customOnScrollListener.setHasNextPage(result.hasNextPage());

            if (CollectionUtils.isEmpty(result.getData())) {
                return;
            }

            if (refresh) {
                selectUserListAdapter.clearAll();
                refresh = false;
            }

            selectUserListAdapter.addAll(result.getData(), true);
            selectUserListAdapter.notifyDataSetChanged();
        });
    }

    private void bindSelectItemViewModel() {
        selectItemViewModel.getSelectedItemResult().observe(this, result -> {
            int count = result.size();
            binding.selectedUserCount.setText(String.format("已选择：%d人", count));
            binding.confirmButton.setEnabled(count > 0);
        });
    }

    private void initAllContactsList() {
        this.selectUserListAdapter = new SelectUserListAdapter(this);
        binding.allContactsListView.setAdapter(selectUserListAdapter);

        binding.allContactsListView.setLayoutManager(new LinearLayoutManager(this));

        customOnScrollListener = new CustomOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
                contactsViewModel.queryAllContacts(pageIndex);
            }

            @Override
            public void onRefresh() {
                refreshContactsList();
            }

            @Override
            public void onNoNextPage() {
                ToastUtil.makeToast(SelectUserListActivity.this, "No more contact", 1000);
            }
        };

        binding.allContactsListView.addOnScrollListener(customOnScrollListener);
    }

    private void refreshContactsList() {
        refresh = true;
        customOnScrollListener.reset();
        contactsViewModel.queryAllContacts(1);
    }

}
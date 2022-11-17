/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.converter.SessionMemberConverter;
import cn.hsbcsd.mpaastest.databinding.DialogSelectAtUserBinding;
import com.hsbcd.mpaastest.kotlin.samples.enums.SessionMemberListOpTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.model.SessionMemberItem;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting.GroupChatSettingViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting.SessionMemberListAdapter;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseBottomSheetDialog;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.SelectUserViewModel;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * 选择要@的人对话框
 *
 * @author liyalong
 * @version SelectAtUserDialog.java, v 0.1 2022年10月10日 14:42 liyalong
 */
public class SelectAtUserDialog extends BaseBottomSheetDialog {

    private Context context;

    private Conversation c;

    private OnSelectListener onSelectListener;

    private DialogSelectAtUserBinding binding;

    private SessionMemberListAdapter sessionMemberListAdapter;

    private GroupChatSettingViewModel groupChatSettingViewModel;

    private SelectUserViewModel selectItemViewModel;

    private SessionMemberListOpTypeEnum opType = SessionMemberListOpTypeEnum.AT_SINGLE_USER;

    private CustomOnScrollListener customOnScrollListener;

    private boolean refresh = false;

    public SelectAtUserDialog(Context context, Conversation c, OnSelectListener onSelectListener) {
        super(false);
        
        this.context = context;
        this.c = c;
        this.onSelectListener = onSelectListener;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogSelectAtUserBinding.inflate(inflater);

        // 绑定组件动作
        bindAction();

        // 绑定群聊设置数据
        groupChatSettingViewModel = new ViewModelProvider(this).get(GroupChatSettingViewModel.class);
        bindGroupChatSettingViewModel();

        // 绑定选择用户数据
        selectItemViewModel = new ViewModelProvider(getActivity()).get(SelectUserViewModel.class);
        bindSelectItemViewModel();

        // 初始化群成员列表
        initSessionMemberList();

        // 加载群成员列表
        refreshGroupMemberList();

        return binding.getRoot();
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.dismiss());

        // 单选/多选切换
        binding.multiSelect.setOnClickListener(v -> {
            if (opType == SessionMemberListOpTypeEnum.AT_SINGLE_USER) {
                opType = SessionMemberListOpTypeEnum.AT_MULTI_USER;
                binding.multiSelect.setText("取消多选");
                binding.selectMemberLayout.setVisibility(View.VISIBLE);
            } else {
                opType = SessionMemberListOpTypeEnum.AT_SINGLE_USER;
                binding.multiSelect.setText("多选");
                binding.selectMemberLayout.setVisibility(View.GONE);
            }

            sessionMemberListAdapter.setOpType(opType);
            refreshGroupMemberList();
        });

        // 多选确认按钮
        binding.confirmButton.setOnClickListener(v -> {
            onSelectListener.onSelect(Lists.newArrayList(selectItemViewModel.getSelectedItems()));
            selectItemViewModel.clear();

            super.dismiss();
        });
    }

    private void bindGroupChatSettingViewModel() {
        // 返回成员列表查询结果后的通知
        groupChatSettingViewModel.getGroupRelationListResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(getActivity(), result.getMessage(), 1000);
                return;
            }

            customOnScrollListener.setHasNextPage(result.hasNextPage());

            if (CollectionUtils.isEmpty(result.getData())) {
                return;
            }

            if (refresh) {
                sessionMemberListAdapter.clearAll();
                if (opType == SessionMemberListOpTypeEnum.AT_SINGLE_USER) {
                    sessionMemberListAdapter.addItem(SessionMemberConverter.createAtAllMemberItem(c));
                }
                refresh = false;
            }

            // 绑定成员列表数据，并立即渲染
            List<SessionMemberItem> members = SessionMemberConverter.convertGroupRelations(result.getData());
            sessionMemberListAdapter.addAll(members);
            sessionMemberListAdapter.notifyDataSetChanged();
        });
    }

    private void bindSelectItemViewModel() {
        selectItemViewModel.getSelectedItemResult().observe(getViewLifecycleOwner(), result -> {
            int count = result.size();
            binding.selectedMemberCount.setText(String.format("已选择：%d人", count));
            binding.confirmButton.setEnabled(count > 0);
        });
    }

    private void initSessionMemberList() {
        this.sessionMemberListAdapter = new SessionMemberListAdapter(getContext(), opType,
                new BaseListItemHolder.OnItemActionListener<SessionMemberItem>() {
                    @Override
                    public void onClickItem(SessionMemberItem member) {
                        onSelectListener.onSelect(Arrays.asList(SessionMemberConverter.convertUserInfo(member)));
                        SelectAtUserDialog.super.dismiss();
                    }
                });

        binding.sessionMemberListView.setAdapter(sessionMemberListAdapter);
        binding.sessionMemberListView.setLayoutManager(new LinearLayoutManager(context));

        customOnScrollListener = new CustomOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
                groupChatSettingViewModel.queryGroupMembers(c, pageIndex);
            }

            @Override
            public void onRefresh() {
                refreshGroupMemberList();
            }

            @Override
            public void onNoNextPage() {
                ToastUtil.makeToast(getActivity(), "没有更多群成员了", 1000);
            }
        };

        binding.sessionMemberListView.addOnScrollListener(customOnScrollListener);
    }

    private void refreshGroupMemberList() {
        refresh = true;
        customOnScrollListener.reset();
        groupChatSettingViewModel.queryGroupMembers(c, 1);
    }

    public interface OnSelectListener {
        void onSelect(List<UserInfoVO> selectedUserInfos);
    }

}
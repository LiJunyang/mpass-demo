/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.converter.SessionMemberConverter;
import cn.com.hsbc.hsbcchina.cert.databinding.ActivityGroupAdminListBinding;
import com.hsbcd.mpaastest.kotlin.samples.enums.SessionMemberListOpTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.model.SessionMemberItem;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import java.util.Arrays;
import java.util.List;

/**
 * 群管理员列表页
 *
 * @author liyalong
 * @version GroupAdminListActivity.java, v 0.1 2022年09月19日 10:45 liyalong
 */
public class GroupAdminListActivity extends AppCompatActivity {

    private ActivityGroupAdminListBinding binding;

    private SessionMemberListAdapter sessionMemberListAdapter;

    private GroupChatSettingViewModel groupChatSettingViewModel;

    private Conversation c;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c == null) {
            Log.e(LoggerName.UI, "unset session，cannot go to group admin page");
            super.onBackPressed();
            return;
        }
        this.c = c;

        binding = ActivityGroupAdminListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindAction();

        // 绑定群聊设置数据
        groupChatSettingViewModel = new ViewModelProvider(this).get(GroupChatSettingViewModel.class);
        bindGroupChatSettingViewModel();

        // 初始化群管理员列表
        initSessionMemberList();
    }

    /**
     * 每次进入页面时
     */
    @Override
    protected void onStart() {
        super.onStart();

        // 加载群管理员列表
        refreshGroupMemberList();
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());

        binding.addGroupAdmin.setOnClickListener(v -> {
            Intent intent = new Intent(this, SessionMemberListActivity.class);
            intent.putExtra(SessionMemberListActivity.OP_TYPE_KEY, SessionMemberListOpTypeEnum.ADD_ADMIN.name());
            this.startActivity(intent);
        });
    }

    private void bindGroupChatSettingViewModel() {
        // 群成员更新完成后的通知
        groupChatSettingViewModel.getUpdateSettingResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "Update Success", 1000);
            } else {
                ToastUtil.makeToast(this, "Update Failed: " + result.getMessage(), 3000);
            }
        });

        // 返回成员列表查询结果后的通知
        groupChatSettingViewModel.getGroupRelationListResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 1000);
                return;
            }

            if (CollectionUtils.isEmpty(result.getData())) {
                sessionMemberListAdapter.clearAll();
                setMemberListViewVisibility();
                return;
            }

            // 绑定成员列表数据，并立即渲染
            List<SessionMemberItem> members = SessionMemberConverter.convertGroupRelations(result.getData());
            sessionMemberListAdapter.setItems(members);
            sessionMemberListAdapter.notifyDataSetChanged();
            setMemberListViewVisibility();
        });
    }

    private void setMemberListViewVisibility() {
        if (sessionMemberListAdapter.getItemCount() == 0) {
            binding.sessionMemberListView.setVisibility(View.GONE);
            binding.line.setVisibility(View.GONE);
        } else {
            binding.sessionMemberListView.setVisibility(View.VISIBLE);
            binding.line.setVisibility(View.VISIBLE);
        }
    }

    private void initSessionMemberList() {
        this.sessionMemberListAdapter = new SessionMemberListAdapter(this, SessionMemberListOpTypeEnum.LIST_ADMIN,
                new BaseListItemHolder.OnItemActionListener<SessionMemberItem>() {
                    @Override
                    public void onRemoveItem(SessionMemberItem member, int position) {
                        removeGroupAdmin(member, position);
                    }
                });

        binding.sessionMemberListView.setAdapter(sessionMemberListAdapter);
        binding.sessionMemberListView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void refreshGroupMemberList() {
        groupChatSettingViewModel.queryGroupMembers(c, 1);
    }

    private void removeGroupAdmin(SessionMemberItem member, int position) {
        groupChatSettingViewModel.removeGroupAdmin(c, Arrays.asList(member.getUserId()));

        sessionMemberListAdapter.removeItem(position);
        setMemberListViewVisibility();
    }

}
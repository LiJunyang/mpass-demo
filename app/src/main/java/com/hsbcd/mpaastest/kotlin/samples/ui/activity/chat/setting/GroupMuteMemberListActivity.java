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
import cn.hsbcsd.mpaastest.databinding.ActivityGroupMuteMemberListBinding;
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
 * 群禁言成员列表页
 *
 * @author liyalong
 * @version GroupMuteMemberListActivity.java, v 0.1 2022年09月19日 14:45 liyalong
 */
public class GroupMuteMemberListActivity extends AppCompatActivity {

    private ActivityGroupMuteMemberListBinding binding;

    private SessionMemberListAdapter sessionMemberListAdapter;

    private GroupChatSettingViewModel groupChatSettingViewModel;

    private Conversation c;

    private boolean initSettingFinish = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c == null) {
            Log.e(LoggerName.UI, "unset session，cannot go to group forbidden list");
            super.onBackPressed();
            return;
        }
        this.c = c;

        binding = ActivityGroupMuteMemberListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindAction();

        // 绑定群聊设置数据
        groupChatSettingViewModel = new ViewModelProvider(this).get(GroupChatSettingViewModel.class);
        bindGroupChatSettingViewModel();

        // 初始化禁言成员列表
        initSessionMemberList();
    }

    /**
     * 每次进入页面时
     */
    @Override
    protected void onStart() {
        super.onStart();

        // 加载禁言成员列表
        refreshGroupMuteMemberList();

        // 绑定各组件的初始值
        bindSettingValue();
    }

    private void bindSettingValue() {
        // 全员禁言开关
        binding.muteAllSwitch.setChecked(c.getGroup().isSilenceAll());

        initSettingFinish = true;
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());

        // 新增禁言成员
        binding.addMuteMember.setOnClickListener(v -> {
            Intent intent = new Intent(this, SessionMemberListActivity.class);
            intent.putExtra(SessionMemberListActivity.OP_TYPE_KEY, SessionMemberListOpTypeEnum.ADD_MUTE_MEMBER.name());
            this.startActivity(intent);
        });

        // 全员禁言开关
        binding.muteAllSwitch.setOnCheckedChangeListener((v, checked) -> {
            if (initSettingFinish) {
                if (checked) {
                    groupChatSettingViewModel.muteAll(c);
                } else {
                    groupChatSettingViewModel.cancelMuteAll(c);
                }
            }
        });
    }

    private void bindGroupChatSettingViewModel() {
        // 群设置更新完成后的通知
        groupChatSettingViewModel.getUpdateSettingResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "更新成功", 1000);
                refreshGroupMuteMemberList();
            } else {
                ToastUtil.makeToast(this, "更新失败: " + result.getMessage(), 3000);
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
        this.sessionMemberListAdapter = new SessionMemberListAdapter(this, SessionMemberListOpTypeEnum.LIST_MUTE_MEMBER,
                new BaseListItemHolder.OnItemActionListener<SessionMemberItem>() {
                    @Override
                    public void onRemoveItem(SessionMemberItem member, int position) {
                        removeGroupMuteMember(member, position);
                    }
                });

        binding.sessionMemberListView.setAdapter(sessionMemberListAdapter);
        binding.sessionMemberListView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void refreshGroupMuteMemberList() {
        groupChatSettingViewModel.queryGroupMuteMembers(c, 1);
    }

    private void removeGroupMuteMember(SessionMemberItem member, int position) {
        groupChatSettingViewModel.cancelGroupMuteMember(c, Arrays.asList(member.getUserId()));

        sessionMemberListAdapter.removeItem(position);
        setMemberListViewVisibility();
    }

}
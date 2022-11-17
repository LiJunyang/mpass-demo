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

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.converter.SessionMemberConverter;
import cn.hsbcsd.mpaastest.databinding.ActivitySessionMemberListBinding;
import com.hsbcd.mpaastest.kotlin.samples.enums.SessionMemberListOpTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.model.SessionMemberItem;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.AlertDialogFragment;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.SelectUserViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts.UserDetailActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.AddGroupMemberActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.GroupUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 会话成员列表页
 *
 * @author liyalong
 * @version SessionMemberListActivity.java, v 0.1 2022年08月15日 10:12 liyalong
 */
public class SessionMemberListActivity extends AppCompatActivity {

    public static final String OP_TYPE_KEY = "opType";

    private ActivitySessionMemberListBinding binding;

    private SessionMemberListAdapter sessionMemberListAdapter;

    private GroupChatSettingViewModel groupChatSettingViewModel;

    private SelectUserViewModel selectItemViewModel;

    private Conversation c;

    private SessionMemberListOpTypeEnum opType;

    private CustomOnScrollListener customOnScrollListener;

    private boolean refresh = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c == null) {
            Log.e(LoggerName.UI, "未设置会话，无法进入群成员列表页");
            super.onBackPressed();
            return;
        }
        this.c = c;

        String opTypeStr = getIntent().getStringExtra(OP_TYPE_KEY);
        this.opType = StringUtils.isBlank(opTypeStr) ? SessionMemberListOpTypeEnum.LIST_ALL_MEMBER
                : SessionMemberListOpTypeEnum.valueOf(opTypeStr);

        binding = ActivitySessionMemberListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindAction();

        // 绑定群聊设置数据
        groupChatSettingViewModel = new ViewModelProvider(this).get(GroupChatSettingViewModel.class);
        bindGroupChatSettingViewModel();

        // 绑定选择用户数据
        selectItemViewModel = new ViewModelProvider(this).get(SelectUserViewModel.class);
        bindSelectItemViewModel();

        initSessionMemberList();
    }

    /**
     * 每次进入页面时
     */
    @Override
    protected void onStart() {
        super.onStart();

        // 加载群成员列表
        refreshGroupMemberList();
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());

        switch (opType) {
            case LIST_ALL_MEMBER: {
                binding.titleLabel.setText("群成员");

                if (GroupUtil.canModifyGroup(c)) {
                    binding.addMember.setVisibility(View.VISIBLE);
                    binding.addMember.setOnClickListener(v -> goToAddMemberActivity());
                } else {
                    binding.addMember.setVisibility(View.GONE);
                }

                break;
            }
            case REMOVE_MEMBER: {
                binding.titleLabel.setText("移除群成员");
                binding.selectMemberLayout.setVisibility(View.VISIBLE);
                binding.confirmButton.setOnClickListener(v -> doRemoveGroupMember());
                break;
            }
            case ADD_ADMIN: {
                binding.titleLabel.setText("添加群管理员");
                binding.selectMemberLayout.setVisibility(View.VISIBLE);
                binding.confirmButton.setOnClickListener(v -> doAddGroupAdmin());
                break;
            }
            case ADD_MUTE_MEMBER: {
                binding.titleLabel.setText("添加禁言成员");
                binding.selectMemberLayout.setVisibility(View.VISIBLE);
                binding.confirmButton.setOnClickListener(v -> doAddGroupMuteMember());
                break;
            }
            case TRANSFER_OWNER: {
                binding.titleLabel.setText("选择转让对象");
                break;
            }
            default:
                break;
        }
    }

    private void bindGroupChatSettingViewModel() {
        // 设置项更新完成后的通知
        groupChatSettingViewModel.getUpdateSettingResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "更新成功", 1000);
            } else {
                ToastUtil.makeToast(this, "更新失败: " + result.getMessage(), 3000);
            }

            super.onBackPressed();
        });

        // 返回成员列表查询结果后的通知
        groupChatSettingViewModel.getGroupRelationListResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 1000);
                return;
            }

            customOnScrollListener.setHasNextPage(result.hasNextPage());

            if (CollectionUtils.isEmpty(result.getData())) {
                return;
            }

            if (refresh) {
                sessionMemberListAdapter.clearAll();
                refresh = false;
            }

            // 绑定成员列表数据，并立即渲染
            List<SessionMemberItem> members = SessionMemberConverter.convertGroupRelations(result.getData());
            sessionMemberListAdapter.addAll(members);
            sessionMemberListAdapter.notifyDataSetChanged();
        });

        // 转让群主后的通知
        groupChatSettingViewModel.getTransferOwnerResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // 跳转回群设置页
            Intent intent = new Intent(this, GroupChatSettingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void bindSelectItemViewModel() {
        selectItemViewModel.getSelectedItemResult().observe(this, result -> {
            int count = result.size();
            binding.selectedMemberCount.setText(String.format("已选择：%d人", count));
            binding.confirmButton.setEnabled(count > 0);
        });
    }

    private void initSessionMemberList() {
        this.sessionMemberListAdapter = new SessionMemberListAdapter(this, opType,
                new BaseListItemHolder.OnItemActionListener<SessionMemberItem>() {
                    @Override
                    public void onClickItem(SessionMemberItem member) {
                        onClickSingleMember(member);
                    }
                });

        binding.sessionMemberListView.setAdapter(sessionMemberListAdapter);
        binding.sessionMemberListView.setLayoutManager(new LinearLayoutManager(this));

        customOnScrollListener = new CustomOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
                doQueryGroupMembers(pageIndex);
            }

            @Override
            public void onRefresh() {
                refreshGroupMemberList();
            }

            @Override
            public void onNoNextPage() {
                ToastUtil.makeToast(SessionMemberListActivity.this, "没有更多群成员了", 1000);
            }
        };

        binding.sessionMemberListView.addOnScrollListener(customOnScrollListener);
    }

    private void refreshGroupMemberList() {
        refresh = true;
        customOnScrollListener.reset();
        doQueryGroupMembers(1);
    }

    private void doQueryGroupMembers(int pageIndex) {
        // 如果是新增禁言成员，则查询非禁言成员列表，否则默认查询全量成员列表
        if (opType == SessionMemberListOpTypeEnum.ADD_MUTE_MEMBER) {
            groupChatSettingViewModel.queryGroupUnMuteMembers(c, pageIndex);
        } else {
            groupChatSettingViewModel.queryGroupMembers(c, pageIndex);
        }
    }

    private void goToAddMemberActivity() {
        Intent intent = new Intent(this, AddGroupMemberActivity.class);
        this.startActivity(intent);
    }

    private void onClickSingleMember(SessionMemberItem member) {
        // 如果是转让群主，则触发转让动作
        if (opType == SessionMemberListOpTypeEnum.TRANSFER_OWNER) {
            AlertDialogFragment dialog = new AlertDialogFragment("确定要转让群主吗?",
                    String.format("群主将转让给 ⌜%s⌟", member.getUserName()),
                    () -> groupChatSettingViewModel.transferOwner(c, member.getUserId()));
            dialog.show(getSupportFragmentManager(), "");
        }
        // 否则默认跳转到用户详情页
        else {
            Intent intent = new Intent(this, UserDetailActivity.class);
            intent.putExtra(CommonConstants.USER_ID, member.getUserId());
            this.startActivity(intent);
        }
    }

    private void doRemoveGroupMember() {
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        groupChatSettingViewModel.removeGroupMember(c, getSelectedUserIds());
    }

    private void doAddGroupAdmin() {
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        groupChatSettingViewModel.addGroupAdmin(c, getSelectedUserIds());
    }

    private void doAddGroupMuteMember() {
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        groupChatSettingViewModel.addGroupMuteMember(c, getSelectedUserIds());
    }

    private List<String> getSelectedUserIds() {
        Collection<UserInfoVO> selectedUsers = selectItemViewModel.getSelectedItems();
        return selectedUsers.stream().map(u -> u.getUserId()).collect(Collectors.toList());
    }

}
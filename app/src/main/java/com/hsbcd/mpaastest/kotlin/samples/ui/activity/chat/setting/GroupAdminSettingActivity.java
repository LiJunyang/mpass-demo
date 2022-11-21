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

import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import cn.hsbcsd.mpaastest.databinding.ActivityGroupAdminSettingBinding;
import com.hsbcd.mpaastest.kotlin.samples.enums.SessionMemberListOpTypeEnum;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.group.GroupSetting;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.AlertDialogFragment;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.home.HomeActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.GroupUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

/**
 * 群管理员设置页
 *
 * @author liyalong
 * @version GroupAdminSettingActivity.java, v 0.1 2022年08月11日 16:01 liyalong
 */
public class GroupAdminSettingActivity extends AppCompatActivity {

    private ActivityGroupAdminSettingBinding binding;

    private GroupChatSettingViewModel groupChatSettingViewModel;

    private Conversation c;

    private boolean initSettingFinish = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupAdminSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 绑定组件动作
        bindAction();

        // 绑定聊天设置数据
        groupChatSettingViewModel = new ViewModelProvider(this).get(GroupChatSettingViewModel.class);
        bindGroupChatSettingViewModel();
    }

    /**
     * 每次进入页面时
     */
    @Override
    protected void onStart() {
        super.onStart();

        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c == null) {
            Log.e(LoggerName.UI, "unset session，cannot go to setting");
            super.onBackPressed();
            return;
        }
        this.c = c;

        // 设置组件可见性
        bindVisibility();

        // 查询群设置数据
        refreshGroupSetting();
    }

    private void refreshGroupSetting() {
        groupChatSettingViewModel.queryGroupSetting(c);
    }

    private void bindVisibility() {
        // 以下功能仅对群主可见：群管理员，转让群主，解散群聊
        binding.adminLayout.setVisibility(GroupUtil.isGroupOwner(c) ? View.VISIBLE : View.GONE);
        binding.transferOwnerLayout.setVisibility(GroupUtil.isGroupOwner(c) ? View.VISIBLE : View.GONE);
        binding.dismissGroup.setVisibility(GroupUtil.isGroupOwner(c) ? View.VISIBLE : View.GONE);
    }

    private void bindSettingValue(GroupSetting groupSetting) {
        // 群设置数据
        binding.adminOnlySwitch.setChecked(groupSetting.isAllowAdminModify());

        initSettingFinish = true;
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());

        // 群设置
        binding.adminOnlySwitch.setOnCheckedChangeListener((v, checked) -> {
            if (initSettingFinish) {
                GroupSetting groupSetting = new GroupSetting();
                groupSetting.setAllowAdminModify(checked);
                
                groupChatSettingViewModel.updateGroupSetting(c, groupSetting);
            }
        });

        // 设置群内禁言
        binding.setMuteLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, GroupMuteMemberListActivity.class);
            this.startActivity(intent);
        });

        // 设置群管理员
        binding.adminLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, GroupAdminListActivity.class);
            this.startActivity(intent);
        });

        // 转让群主
        binding.transferOwnerLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, SessionMemberListActivity.class);
            intent.putExtra(SessionMemberListActivity.OP_TYPE_KEY, SessionMemberListOpTypeEnum.TRANSFER_OWNER.name());
            this.startActivity(intent);
        });

        // 解散群聊
        binding.dismissGroup.setOnClickListener(v -> {
            AlertDialogFragment dialog = new AlertDialogFragment("是否解散本群", null,
                    () -> groupChatSettingViewModel.dismissGroup(c));
            dialog.show(getSupportFragmentManager(), "");
        });
    }

    private void bindGroupChatSettingViewModel() {
        // 设置项更新完成后的通知
        groupChatSettingViewModel.getUpdateSettingResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "更新成功", 1000);
            } else {
                ToastUtil.makeToast(this, "更新失败: " + result.getMessage(), 3000);
            }
        });

        // 解散群聊后的通知
        groupChatSettingViewModel.getDismissGroupResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // 跳转回首页
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });

        // 群设置查询完成后的通知
        groupChatSettingViewModel.getGroupSettingResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // 绑定各组件的初始值
            bindSettingValue(result.getData());
        });
    }

}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.user;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting.GroupChatSettingViewModel;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
import com.google.common.collect.Lists;

import java.util.Collection;
import java.util.List;

/**
 * 添加群成员页
 *
 * @author liyalong
 * @version AddGroupMemberActivity.java, v 0.1 2022年08月08日 15:38 liyalong
 */
public class AddGroupMemberActivity extends SelectUserListActivity {

    private GroupChatSettingViewModel groupChatSettingViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 绑定聊天设置数据
        groupChatSettingViewModel = new ViewModelProvider(this).get(GroupChatSettingViewModel.class);
        bindGroupChatSettingViewModel();
    }

    @Override
    public boolean isSingleSelectMode() {
        return false;
    }

    @Override
    protected void onClickConfirm(Collection<UserInfoVO> selectedUsers) {
        doAddGroupMember(selectedUsers);
    }

    private void bindGroupChatSettingViewModel() {
        // 设置项更新完成后的通知
        groupChatSettingViewModel.getUpdateSettingResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "Update Success", 1000);
            } else {
                ToastUtil.makeToast(this, "Update Failed: " + result.getMessage(), 3000);
            }

            super.onBackPressed();
        });
    }

    private void doAddGroupMember(Collection<UserInfoVO> selectedUsers) {
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();

        List<String> userIds = Lists.newArrayList();
        for (UserInfoVO userInfoVO : selectedUsers) {
            userIds.add(userInfoVO.getUserId());
        }

        groupChatSettingViewModel.addGroupMember(c, userIds);
    }

}
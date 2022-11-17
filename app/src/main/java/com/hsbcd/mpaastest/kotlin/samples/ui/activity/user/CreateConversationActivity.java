/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.common.service.facade.enums.SessionModeEnum;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.enums.ConversationTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.ChatActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.SessionViewModel;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * 创建单聊/群聊会话页
 *
 * @author liyalong
 * @version CreateConversationActivity.java, v 0.1 2022年08月08日 15:38 liyalong
 */
public class CreateConversationActivity extends SelectUserListActivity {

    public static final String SESSION_TYPE_KEY = "sessionType";

    public static final String SESSION_MODE_KEY = "sessionMode";

    private ConversationTypeEnum sessionType;

    private SessionModeEnum sessionMode;

    private SessionViewModel sessionViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionType = ConversationTypeEnum.getByCode(getIntent().getStringExtra(SESSION_TYPE_KEY));
        sessionMode = SessionModeEnum.getByCode(
                StringUtils.defaultIfBlank(getIntent().getStringExtra(SESSION_MODE_KEY), SessionModeEnum.IM.getCode()));

        bindAction();

        // 绑定会话数据
        sessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        bindSessionViewModel();
    }

    private void bindAction() {
        if (sessionType == ConversationTypeEnum.SINGLE) {
            binding.selectUserLayout.setVisibility(View.GONE);
            binding.titleLabel.setText(sessionMode == SessionModeEnum.SECRET_CHAT ? "发起密聊" : "发起单聊");
        } else if (sessionType == ConversationTypeEnum.GROUP) {
            binding.selectUserLayout.setVisibility(View.VISIBLE);
            binding.titleLabel.setText("发起群聊");
        }
    }

    @Override
    public boolean isSingleSelectMode() {
        return sessionType == ConversationTypeEnum.SINGLE;
    }

    @Override
    public void onClickSingleUser(UserInfoVO userInfoVO) {
        if (sessionMode == SessionModeEnum.IM) {
            // 创建单聊会话，等待创建完成后再打开消息页
            sessionViewModel.createSingleConversation(userInfoVO.getUserId());
        } else if (sessionMode == SessionModeEnum.SECRET_CHAT) {
            // 创建密聊会话
            sessionViewModel.createSecretChatConversation(userInfoVO.getUserId());
        }
    }

    @Override
    protected void onClickConfirm(Collection<UserInfoVO> selectedUsers) {
        // 如果只选中了一个用户，则退化为单聊
        if (selectedUsers.size() == 1) {
            sessionViewModel.createSingleConversation(selectedUsers.iterator().next().getUserId());
            return;
        }

        sessionViewModel.createGroupConversation(selectedUsers);
    }

    private void bindSessionViewModel() {
        sessionViewModel.getConversationResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // 设置当前会话
            AlipayCcmIMClient.getInstance().getConversationManager().setCurrentConversation(result.getData());

            // 打开会话消息页
            Intent intent = new Intent(this, ChatActivity.class);
            this.startActivity(intent);
        });
    }

}
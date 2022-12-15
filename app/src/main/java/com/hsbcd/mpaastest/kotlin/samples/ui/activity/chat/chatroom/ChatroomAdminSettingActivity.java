/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.chatroom;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom.ChatroomViewModel;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import cn.hsbcsd.mpaastest.databinding.ActivityChatroomAdminSettingBinding;

/**
 * 聊天室管理员设置页
 *
 * @author liyalong
 * @version ChatroomAdminSettingActivity.java, v 0.1 2022年11月14日 16:11 liyalong
 */
public class ChatroomAdminSettingActivity extends AppCompatActivity {

    private ActivityChatroomAdminSettingBinding binding;

    private ChatroomViewModel chatroomViewModel;

    private Conversation c;

    private boolean initSettingFinish = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatroomAdminSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 绑定组件动作
        bindAction();

        // 绑定聊天室数据
        bindChatroomViewModel();
    }

    /**
     * 每次进入页面时
     */
    @Override
    protected void onStart() {
        super.onStart();

        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c == null) {
            Log.e(LoggerName.UI, "No chat was set, can not go to setting page");
            super.onBackPressed();
            return;
        }
        this.c = c;

        bindSettingValue();
    }

    private void bindSettingValue() {
        // 允许聊天互动(全体禁言取反)
        binding.allowChatSwitch.setChecked(!c.getChatroom().isSilenceAll());

        initSettingFinish = true;
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());

        // 允许聊天互动(全体禁言取反)
        binding.allowChatSwitch.setOnCheckedChangeListener((v, checked) -> {
            if (initSettingFinish) {
                chatroomViewModel.muteAllMember(c, !checked);
            }
        });
    }

    private void bindChatroomViewModel() {
        chatroomViewModel = new ViewModelProvider(this).get(ChatroomViewModel.class);

        // 聊天室设置更新通知
        chatroomViewModel.getUpdateSettingResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "Update Success", 1000);
            } else {
                ToastUtil.makeToast(this, "Update Failed: " + result.getMessage(), 3000);
            }
        });
    }

}
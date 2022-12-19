/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.chatroom.ChatroomLiveActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import cn.hsbcsd.mpaastest.R;
import cn.hsbcsd.mpaastest.databinding.ActivityCreateChatroomBinding;

/**
 * 页
 *
 * @author liyalong
 * @version CreateChatroomActivity.java, v 0.1 2022年11月08日 17:45 liyalong
 */
public class CreateChatroomActivity extends AppCompatActivity {

    private ActivityCreateChatroomBinding binding;

    private ChatroomViewModel chatroomViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle();

        binding = ActivityCreateChatroomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindAction();

        // 绑定聊天室数据
        bindChatroomViewModel();
    }

    private void setStyle() {
        // 顶部状态栏和底部导航栏设置为跟页面背景同色
        getWindow().setStatusBarColor(getResources().getColor(R.color.dark_gray3));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_gray3));

        // 状态栏文字设置为浅色
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());

        binding.changeCover.setOnClickListener(v -> todoToast());
        binding.addLiveGroup.setOnClickListener(v -> todoToast());

        binding.createChatroom.setEnabled(false);
        binding.createChatroom.setOnClickListener(v -> {
            String chatroomName = binding.inputChatroomName.getText().toString();
            if (chatroomName.length() > 128) {
                ToastUtil.makeToast(this, "直播主题不能超过128个字符", 1000);
                return;
            }

            chatroomViewModel.createChatroom(chatroomName);
        });

        binding.inputChatroomName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.createChatroom.setEnabled(charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void todoToast() {
        ToastUtil.makeToast(this, "to be added", 1000);
    }

    private void bindChatroomViewModel() {
        chatroomViewModel = new ViewModelProvider(this).get(ChatroomViewModel.class);

        // 聊天室会话创建结果通知
        chatroomViewModel.getConversationResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // 设置当前会话
            AlipayCcmIMClient.getInstance().getConversationManager().setCurrentConversation(result.getData());

            // 打开聊天室直播页
            Intent intent = new Intent(this, ChatroomLiveActivity.class);
            this.startActivity(intent);
        });
    }
}
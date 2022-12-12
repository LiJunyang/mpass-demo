/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.common.service.facade.enums.SessionMetaTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.converter.SessionMemberConverter;
import cn.hsbcsd.mpaastest.databinding.ActivitySingleChatSettingBinding;
import com.hsbcd.mpaastest.kotlin.samples.model.SessionMemberItem;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.AlertDialogFragment;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import java.util.Arrays;

/**
 * 单聊设置页
 *
 * @author liyalong
 * @version SingleChatSettingActivity.java, v 0.1 2022年08月10日 14:14 liyalong
 */
public class SingleChatSettingActivity extends AppCompatActivity {

    private ActivitySingleChatSettingBinding binding;

    private SessionMemberGridAdapter sessionMemberGridAdapter;

    private ChatSettingViewModel chatSettingViewModel;

    private Conversation c;

    private boolean initSettingFinish = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySingleChatSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 绑定组件动作
        bindAction();

        // 绑定聊天设置数据
        chatSettingViewModel = new ViewModelProvider(this).get(ChatSettingViewModel.class);
        bindChatSettingViewModel();

        // 初始化成员网格视图
        initMemberGrid();
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

        // 绑定各组件的初始值
        bindSettingValue();

        // 绑定成员列表
        bindMemberGrid();
    }

    private void bindSettingValue() {
        binding.setTopSwitch.setChecked(c.isTopMode());
        binding.setNoDisturbSwitch.setChecked(c.isShieldMode());

        initSettingFinish = true;
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());
        binding.searchMessage.setOnClickListener(v -> ToastUtil.makeToast(this, "敬请期待", 1000));

        binding.setTopSwitch.setOnCheckedChangeListener((v, checked) -> {
            if (initSettingFinish) {
                chatSettingViewModel.setToTop(c, checked);
            }
        });

        binding.setNoDisturbSwitch.setOnCheckedChangeListener((v, checked) -> {
            if (initSettingFinish) {
                chatSettingViewModel.setNoDisturb(c, checked);
            }
        });

        binding.clearMessage.setOnClickListener(v -> {
            AlertDialogFragment dialog = new AlertDialogFragment("是否清空聊天记录", null,
                    () -> chatSettingViewModel.clearAllMessage(c));
            dialog.show(getSupportFragmentManager(), "");
        });
    }

    private void bindChatSettingViewModel() {
        chatSettingViewModel.getUpdateSettingResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "Update Success", 1000);
            } else {
                ToastUtil.makeToast(this, "Update Failed: " + result.getMessage(), 3000);
            }
        });
    }

    private void initMemberGrid() {
        this.sessionMemberGridAdapter = new SessionMemberGridAdapter(this, SessionMetaTypeEnum.SINGLE);
        binding.memberGridView.setAdapter(sessionMemberGridAdapter);
    }

    private void bindMemberGrid() {
        // 绑定成员列表数据，并立即渲染
        SessionMemberItem member = SessionMemberConverter.convertUser(c.getSingleSubUser());
        sessionMemberGridAdapter.setMembers(Arrays.asList(member));
        sessionMemberGridAdapter.notifyDataSetChanged();
    }

}
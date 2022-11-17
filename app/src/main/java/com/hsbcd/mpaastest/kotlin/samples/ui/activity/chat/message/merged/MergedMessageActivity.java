/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.merged;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageDeleteStatusEnum;
import cn.hsbcsd.mpaastest.databinding.ActivityMergedMessageBinding;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.MessageViewModel;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 合并转发消息列表页
 *
 * @author liyalong
 * @version MergedMessageActivity.java, v 0.1 2022年08月26日 20:02 liyalong
 */
public class MergedMessageActivity extends AppCompatActivity {

    public static final String SESSION_ID = "cid";

    public static final String MESSAGE_IDS = "messageIds";

    private ActivityMergedMessageBinding binding;

    private MergedMessageListAdapter mergedMessageListAdapter;

    private MessageViewModel messageViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMergedMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goBack.setOnClickListener(v -> super.onBackPressed());

        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        bindMessageViewModel();

        initMessageList();

        loadMessageList();
    }

    private void bindMessageViewModel() {
        messageViewModel.getMessageListResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            if (CollectionUtils.isEmpty(result.getData())) {
                ToastUtil.makeToast(this, "消息列表为空", 3000);
                return;
            }

            // 过滤掉已删除消息
            List<Message> messages = result.getData().stream()
                    .filter(m -> m.getDeleteStatus() == MessageDeleteStatusEnum.NORMAL)
                    .collect(Collectors.toList());

            // 渲染消息列表
            mergedMessageListAdapter.setItems(messages);
            mergedMessageListAdapter.notifyDataSetChanged();
        });
    }

    private void initMessageList() {
        this.mergedMessageListAdapter = new MergedMessageListAdapter(this);
        binding.messageListView.setAdapter(mergedMessageListAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.messageListView.setLayoutManager(layoutManager);
    }

    private void loadMessageList() {
        String cid = getIntent().getStringExtra(SESSION_ID);
        ArrayList<String> messageIds = getIntent().getStringArrayListExtra(MESSAGE_IDS);

        messageViewModel.queryMessageList(cid, messageIds);
    }

}
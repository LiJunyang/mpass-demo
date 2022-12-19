/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alipay.fc.ccmimplus.common.service.facade.enums.SessionModeEnum;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.enums.ConversationTypeEnum;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.ChatActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting.ChatSettingViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.CreateConversationActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import cn.hsbcsd.mpaastest.databinding.ActivitySecretChatSessionListBinding;

/**
 * 密聊会话列表页
 *
 * @author liyalong
 * @version SecretChatSessionListActivity.java, v 0.1 2022年10月21日 15:50 liyalong
 */
public class SecretChatSessionListActivity extends AppCompatActivity {

    private ActivitySecretChatSessionListBinding binding;

    private SessionListAdapter sessionListAdapter;

    private SessionViewModel sessionViewModel;

    private ChatSettingViewModel chatSettingViewModel;

    private CustomOnScrollListener customOnScrollListener;

    private boolean refresh = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySecretChatSessionListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bindAction();

        // 绑定会话数据
        sessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        bindSessionViewModel();

        // 绑定聊天设置数据
        chatSettingViewModel = new ViewModelProvider(this).get(ChatSettingViewModel.class);
        bindChatSettingViewModel();

        // 初始化会话列表
        initSessionList();
    }

    @Override
    public void onStart() {
        super.onStart();

        // 每次进入会话列表页时，都重新查询一次会话列表，确保数据是最新的
        refreshSessionList();
    }

    private void refreshSessionList() {
        refresh = true;
        customOnScrollListener.reset();
        sessionViewModel.querySecretChatConversations(1);
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());

        binding.createSecretChat.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateConversationActivity.class);
            intent.putExtra(CreateConversationActivity.SESSION_TYPE_KEY, ConversationTypeEnum.SINGLE.getCode());
            intent.putExtra(CreateConversationActivity.SESSION_MODE_KEY, SessionModeEnum.SECRET_CHAT.getCode());
            this.startActivity(intent);
        });

        binding.secretChatSetting.setOnClickListener(v -> ToastUtil.makeToast(this, "to be added", 1000));
    }

    private void bindSessionViewModel() {
        // 会话列表查询结果通知
        sessionViewModel.getConversationListResult().observe(this, result -> {
            binding.refreshLayout.setRefreshing(false);

            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            customOnScrollListener.setHasNextPage(result.hasNextPage());

            if (CollectionUtils.isEmpty(result.getData())) {
                return;
            }

            // 渲染会话列表
            if (refresh) {
                sessionListAdapter.clearAll();
                refresh = false;
            }

            sessionListAdapter.addAll(result.getData());
            sessionListAdapter.notifyDataSetChanged();
        });

        // 单个会话查询结果通知
        sessionViewModel.getConversationResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // 设置当前会话
            AlipayCcmIMClient.getInstance().getConversationManager().setCurrentConversation(result.getData());

            // 打开聊天消息页
            Intent intent = new Intent(this, ChatActivity.class);
            this.startActivity(intent);
        });

        // 删除会话结果通知
        sessionViewModel.getDeleteConversationResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "删除成功", 1000);
            } else {
                ToastUtil.makeToast(this, "删除失败: " + result.getMessage(), 3000);
            }
        });
    }

    private void bindChatSettingViewModel() {
        chatSettingViewModel.getUpdateSettingResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "Update Success", 1000);
                refreshSessionList();
            } else {
                ToastUtil.makeToast(this, "Update Failed: " + result.getMessage(), 3000);
            }
        });
    }

    private void initSessionList() {
        this.sessionListAdapter = new SessionListAdapter(this, SessionListAdapter.SessionListTypeEnum.SECRET_CHAT,
                new SecretChatSessionItemHolder.OnSessionItemActionListener() {
                    @Override
                    public void onClickItem(Conversation item) {
                        // 点击会话时，先查会话详情，等待返回结果后再进入消息页
                        sessionViewModel.querySingleConversation(item.getCid());
                    }

                    @Override
                    public void onRemoveItem(Conversation item, int position) {
                        // 删除会话
                        sessionListAdapter.removeItem(position);
                        // TODO 不要物理删除
                        //sessionViewModel.deleteConversation(item.getCid());
                    }

                    @Override
                    public void onClearMessage(Conversation c) {
                        // 清空聊天记录
                        chatSettingViewModel.clearAllMessage(c);
                    }
                });
        binding.sessionListView.setAdapter(sessionListAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        binding.sessionListView.setLayoutManager(linearLayoutManager);

        // 下拉刷新
        binding.refreshLayout.setOnRefreshListener(() -> {
            refreshSessionList();
        });

        // 上拉加载更多
        customOnScrollListener = new CustomOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
                sessionViewModel.querySecretChatConversations(pageIndex);
            }

            @Override
            public void onRefresh() {
                //refreshSessionList();
            }

            @Override
            public void onNoNextPage() {
                ToastUtil.makeToast(SecretChatSessionListActivity.this, "No more secret chat", 1000);
            }
        };

        binding.sessionListView.addOnScrollListener(customOnScrollListener);
    }

}
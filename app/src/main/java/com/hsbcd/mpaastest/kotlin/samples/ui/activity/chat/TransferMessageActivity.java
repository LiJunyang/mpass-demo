/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import cn.com.hsbc.hsbcchina.cert.R;
import cn.com.hsbc.hsbcchina.cert.databinding.ActivityTransferMessageBinding;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.SelectSessionViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.SessionListAdapter;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.SessionViewModel;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消息转发页
 *
 * @author liyalong
 * @version TransferMessageActivity.java, v 0.1 2022年08月25日 16:18 liyalong
 */
public class TransferMessageActivity extends AppCompatActivity {

    public static final String SESSION_ID = "cid";

    public static final String MESSAGE_ID = "messageId";

    public static final String IS_MERGE = "isMerge";

    private ActivityTransferMessageBinding binding;

    private SessionListAdapter sessionListAdapter;

    private SessionViewModel sessionViewModel;

    private SelectSessionViewModel selectItemViewModel;

    private MessageViewModel messageViewModel;

    private CustomOnScrollListener customOnScrollListener;

    private boolean refresh = false;

    /**
     * 当前是否为单选模式
     */
    private boolean singleSelectMode = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTransferMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.selectdTargetLayout.setVisibility(View.GONE);

        // 绑定组件动作
        bindAction();

        // 绑定转发对象tab
        // TODO 个人和群组暂不分开展示
        //bindTransferTargetTab();

        // 绑定会话数据
        sessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        bindSessionViewModel();

        // 绑定选择会话数据
        selectItemViewModel = new ViewModelProvider(this).get(SelectSessionViewModel.class);
        bindSelectSessionViewModel();

        // 绑定聊天消息数据层
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        bindMessageViewModel();

        // 初始化会话列表
        initSessionListView();

        // 加载会话列表
        loadSessionList();
    }

    public boolean isSingleSelectMode() {
        return singleSelectMode;
    }

    public void onClickSingleSession(Conversation c) {
        doTransferMessage(Arrays.asList(c.getCid()));
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());

        binding.multiSelect.setOnClickListener(v -> {
            // 单选 -> 多选
            if (singleSelectMode) {
                singleSelectMode = false;
                binding.multiSelect.setText("取消多选");
                binding.selectdTargetLayout.setVisibility(View.VISIBLE);
            }
            // 多选 -> 单选
            else {
                singleSelectMode = true;
                binding.multiSelect.setText("多选");
                binding.selectdTargetLayout.setVisibility(View.GONE);
            }

            sessionListAdapter.notifyDataSetChanged();
        });

        binding.confirmButton.setOnClickListener(v -> {
            List<String> toCids = Lists.newArrayList();
            for (Conversation c : selectItemViewModel.getSelectedItems()) {
                toCids.add(c.getCid());
            }

            doTransferMessage(toCids);
        });
    }

    private void doTransferMessage(List<String> toCids) {
        String originalCid = getIntent().getStringExtra(SESSION_ID);
        boolean isMerge = getIntent().getBooleanExtra(IS_MERGE, false);

        List<Long> messageIds = new ArrayList<>();
        List<String> idStrList = Splitter.on(",").splitToList(getIntent().getStringExtra(MESSAGE_ID));
        if (CollectionUtils.isNotEmpty(idStrList)) {
            for (String id : idStrList) {
                messageIds.add(NumberUtils.toLong(id));
            }
        }

        messageViewModel.transferMessage(originalCid, messageIds, Sets.newHashSet(toCids), isMerge);
    }

    private void bindSessionViewModel() {
        // 会话列表查询结果通知
        sessionViewModel.getConversationListResult().observe(this, result -> {
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

            // 渲染待转发会话列表时，排除掉原始会话和通知类会话
            String originalCid = getIntent().getStringExtra(SESSION_ID);
            List<Conversation> conversations = result.getData().stream()
                    .filter(c -> !StringUtils.equals(c.getCid(), originalCid) && !c.isNotify())
                    .collect(Collectors.toList());

            sessionListAdapter.addAll(conversations);
            sessionListAdapter.notifyDataSetChanged();
        });
    }

    private void bindSelectSessionViewModel() {
        selectItemViewModel.getSelectedItemResult().observe(this, result -> {
            int count = result.size();
            binding.confirmButton.setEnabled(count > 0);

            long singleCount = result.stream().filter(c -> c.isSingle()).count();
            long groupCount = result.stream().filter(c -> c.isGroupConversation()).count();
            binding.selectedTargetCount.setText(String.format("已选择：%d人, %d个群", singleCount, groupCount));
        });
    }

    private void bindMessageViewModel() {
        messageViewModel.getTransferMessageResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            super.onBackPressed();
        });
    }

    private void initSessionListView() {
        this.sessionListAdapter = new SessionListAdapter(this, SessionListAdapter.SessionListTypeEnum.FOR_SELECT);
        binding.sessionListView.setAdapter(sessionListAdapter);

        binding.sessionListView.setLayoutManager(new LinearLayoutManager(this));

        customOnScrollListener = new CustomOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
                sessionViewModel.queryConversations(pageIndex);
            }

            @Override
            public void onRefresh() {
                refreshSessionList();
            }

            @Override
            public void onNoNextPage() {
                ToastUtil.makeToast(TransferMessageActivity.this, "No more chat", 1000);
            }
        };

        binding.sessionListView.addOnScrollListener(customOnScrollListener);
    }

    private void refreshSessionList() {
        refresh = true;
        customOnScrollListener.reset();
        sessionViewModel.queryConversations(1);
    }

    private void loadSessionList() {
        sessionViewModel.queryConversations(1);
    }

    private void bindTransferTargetTab() {
        resetTabStyle();
        swithToSingleTab(true);

        binding.transferToSingleTabLayout.setOnClickListener(v -> {
            resetTabStyle();
            swithToSingleTab(true);
        });

        binding.transferToGroupTabLayout.setOnClickListener(v -> {
            resetTabStyle();
            swithToGroupTab(true);
        });
    }

    private void resetTabStyle() {
        swithToSingleTab(false);
        swithToGroupTab(false);
    }

    private void swithToSingleTab(boolean checked) {
        binding.transferToSingleTabLabel.setTextColor(getTabColor(checked));
        binding.transferToSingleTabUnderline.setVisibility(checked ? View.VISIBLE : View.GONE);

        if (checked) {
            // TODO 加载联系人列表
        }
    }

    private void swithToGroupTab(boolean checked) {
        binding.transferToGroupTabLabel.setTextColor(getTabColor(checked));
        binding.transferToGroupTabUnderline.setVisibility(checked ? View.VISIBLE : View.GONE);

        if (checked) {
            // TODO 加载群聊列表
        }
    }

    private int getTabColor(boolean checked) {
        return getResources().getColor(checked ? R.color.ant_blue : R.color.black);
    }

}
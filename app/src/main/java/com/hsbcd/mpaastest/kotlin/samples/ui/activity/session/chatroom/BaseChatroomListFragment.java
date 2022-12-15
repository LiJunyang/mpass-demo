/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.model.LiveDataResult;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.chatroom.ChatroomLiveActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import java.util.List;

import cn.hsbcsd.mpaastest.databinding.FragmentChatroomListBinding;

/**
 * 聊天室列表页基类
 *
 * @author liyalong
 * @version BaseChatroomListFragment.java, v 0.1 2022年11月09日 10:35 liyalong
 */
public abstract class BaseChatroomListFragment extends Fragment {

    protected FragmentChatroomListBinding binding;

    protected ChatroomListAdapter chatroomListAdapter;

    protected ChatroomViewModel chatroomViewModel;

    private CustomOnScrollListener customOnScrollListener;

    private boolean refresh = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatroomListBinding.inflate(inflater);

        bindChatroomViewModel();

        initChatroomList();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        refreshChatroomList();
    }

    protected abstract void loadChatroomList(int pageIndex);

    protected void refreshChatroomList() {
        refresh = true;
        customOnScrollListener.reset();
        loadChatroomList(1);
    }

    private void bindChatroomViewModel() {
        chatroomViewModel = new ViewModelProvider(this).get(ChatroomViewModel.class);

        // 聊天室会话列表查询结果通知
        chatroomViewModel.getConversationListResult().observe(getViewLifecycleOwner(), result -> {
            renderChatroomList(result, result.getData());
        });

        // 单个聊天室会话查询结果通知
        chatroomViewModel.getConversationResult().observe(getViewLifecycleOwner(), result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(getActivity(), result.getMessage(), 3000);
                return;
            }

            // 设置当前会话
            AlipayCcmIMClient.getInstance().getConversationManager().setCurrentConversation(result.getData());

            // 打开会话消息页
            Intent intent = new Intent(getContext(), ChatroomLiveActivity.class);
            this.startActivity(intent);
        });
    }

    private void renderChatroomList(LiveDataResult result, List<Conversation> dataList) {
        binding.refreshLayout.setRefreshing(false);

        if (!result.isSuccess()) {
            ToastUtil.makeToast(getActivity(), result.getMessage(), 3000);
            return;
        }

        if (CollectionUtils.isEmpty(dataList)) {
            return;
        }

        // 如果是刷新则先清空列表
        if (refresh) {
            chatroomListAdapter.clearAll();
            refresh = false;
        }

        // 渲染列表
        chatroomListAdapter.addAll(dataList);
        chatroomListAdapter.notifyDataSetChanged();
    }

    private void initChatroomList() {
        chatroomListAdapter = new ChatroomListAdapter(getContext(),
                new BaseListItemHolder.OnItemActionListener<Conversation>() {
                    @Override
                    public void onClickItem(Conversation item) {
                        // 查询会话信息，等待返回查询结果后再打开会话消息页
                        chatroomViewModel.queryChatroom(item.getCid());
                    }
                });
        binding.chatroomListView.setAdapter(chatroomListAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.chatroomListView.setLayoutManager(linearLayoutManager);

        // 下拉刷新
        binding.refreshLayout.setOnRefreshListener(() -> {
            refreshChatroomList();
        });

        // 上拉加载更多
        customOnScrollListener = new CustomOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
                loadChatroomList(pageIndex);
            }

            @Override
            public void onRefresh() {
            }

            @Override
            public void onNoNextPage() {
                ToastUtil.makeToast(getActivity(), "没有更多聊天室了", 1000);
            }
        };

        binding.chatroomListView.addOnScrollListener(customOnScrollListener);
    }

}
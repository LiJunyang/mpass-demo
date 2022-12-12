/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import cn.hsbcsd.mpaastest.R;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageReadStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.MessageReadUnReadStatusVO;
import cn.hsbcsd.mpaastest.databinding.DialogMessageReadStatusBinding;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseBottomSheetDialog;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.SelectUserListAdapter;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

/**
 * 群聊消息已读/未读状态对话框
 *
 * @author liyalong
 * @version MessageReadDialog.java, v 0.1 2022年09月14日 15:42 liyalong
 */
public class MessageReadStatusDialog extends BaseBottomSheetDialog {

    private Context context;

    private Message message;

    private DialogMessageReadStatusBinding binding;

    protected SelectUserListAdapter selectUserListAdapter;

    private MessageViewModel messageViewModel;

    private MessageReadStatusEnum currentStatus;

    public MessageReadStatusDialog(Context context, Message message) {
        super(false);
        
        this.context = context;
        this.message = message;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogMessageReadStatusBinding.inflate(inflater);

        // 绑定组件动作
        bindAction();

        // 绑定聊天消息数据
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        bindMessageViewModel();

        // 初始化成员列表
        initMemberList();

        // 绑定已读/未读成员列表tab页
        bindMemberListTab();

        return binding.getRoot();
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.dismiss());
    }

    private void bindMemberListTab() {
        resetTabStyle();
        swithToUnReadUserListTab(true);

        binding.unReadUserListTabLayout.setOnClickListener(v -> {
            resetTabStyle();
            swithToUnReadUserListTab(true);
        });

        binding.readUserListTabLayout.setOnClickListener(v -> {
            resetTabStyle();
            swithToReadUserListTab(true);
        });

        renderTabLabel();
    }

    private void renderTabLabel() {
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        MessageReadUnReadStatusVO readStatusVO = message.getReadStatusVO();

        int unReadCount = 0;
        int readCount = 0;

        if (readStatusVO != null) {
            unReadCount = readStatusVO.getUnReadUserCount();
            readCount = readStatusVO.getReadUserCount();
        } else {
            unReadCount = (c.getGroup() == null) ? 0 : c.getGroup().getMemberCount();
            readCount = 0;
        }

        binding.unReadUserListTabLabel.setText(String.format("unread(%d)", unReadCount));
        binding.readUserListTabLabel.setText(String.format("read(%d)", readCount));
    }

    private void resetTabStyle() {
        swithToUnReadUserListTab(false);
        swithToReadUserListTab(false);
    }

    private void swithToUnReadUserListTab(boolean checked) {
        binding.unReadUserListTabLabel.setTextColor(getTabColor(checked));
        binding.unReadUserListTabUnderline.setVisibility(checked ? View.VISIBLE : View.GONE);

        if (checked) {
            currentStatus = MessageReadStatusEnum.UN_READ;
            messageViewModel.queryMessageUnReadUsers(message.getSid(), message.getId());
        }
    }

    private void swithToReadUserListTab(boolean checked) {
        binding.readUserListTabLabel.setTextColor(getTabColor(checked));
        binding.readUserListTabUnderline.setVisibility(checked ? View.VISIBLE : View.GONE);

        if (checked) {
            currentStatus = MessageReadStatusEnum.READ;
            messageViewModel.queryMessageReadUsers(message.getSid(), message.getId());
        }
    }

    private int getTabColor(boolean checked) {
        return getResources().getColor(checked ? R.color.ant_blue : R.color.black);
    }

    private void initMemberList() {
        this.selectUserListAdapter = new SelectUserListAdapter(context);
        binding.memberListView.setAdapter(selectUserListAdapter);

        binding.memberListView.setLayoutManager(new LinearLayoutManager(context));
    }

    private void bindMessageViewModel() {
        messageViewModel.getMemberListResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast((AppCompatActivity) context, result.getMessage(), 3000);
                return;
            }

            selectUserListAdapter.setItems(result.getData());
            selectUserListAdapter.notifyDataSetChanged();
        });
    }

}
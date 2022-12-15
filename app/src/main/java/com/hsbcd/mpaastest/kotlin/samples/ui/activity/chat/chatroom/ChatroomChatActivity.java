/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.chatroom;

import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.ChatActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom.ChatroomViewModel;
import com.hsbcd.mpaastest.kotlin.samples.util.MessageUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.UserUtil;

/**
 * 聊天室消息页
 *
 * @author liyalong
 * @version ChatroomChatActivity.java, v 0.1 2022年11月14日 14:14 liyalong
 */
public class ChatroomChatActivity extends ChatActivity {

    private ChatroomViewModel chatroomViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 绑定聊天室直播窗动作
        bindChatroomLiveLayout();

        // 绑定聊天室数据
        bindChatroomViewModel();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

//    @Override
//    public void onBackPressed() {
//          closeChatroom();
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // 注销当前activity的聊天室监听器
        chatroomViewModel.unregisterChatroomListener(c.getCid(), false);
    }

    private void bindChatroomLiveLayout() {
        // 跳转到聊天室直播页
        binding.chatroomLiveZoomOut.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatroomLiveActivity.class);
            this.startActivity(intent);
        });

        renderChatroomOnlineMemberCount();
    }

    private void renderChatroomOnlineMemberCount() {
        binding.chatroomOnlineMemberCount.setText(String.format("%d ppl", c.getChatroom().getOnlineMemberCount()));
    }

    private void bindChatroomViewModel() {
        chatroomViewModel = new ViewModelProvider(this).get(ChatroomViewModel.class);
        chatroomViewModel.registerChatroomListener(c.getCid());

        // 聊天室在线通知
        chatroomViewModel.getChatroomNotifyResult().observe(this, result -> {
            switch (result.getOperation()) {
                case JOIN_CHATROOM: {
                    UserUtil.getAndConsumeUserName(result.getUserId(), (userName) -> {
                        Message message = MessageUtil.buildNotifyMessage(String.format("%s joined the chatroom", userName));
                        insertMessage(message);
                    });

                    c.getChatroom().setOnlineMemberCount(c.getChatroom().getOnlineMemberCount() + 1);
                    renderChatroomOnlineMemberCount();
                    break;
                }
                case LEAVE_CHATROOM: {
                    UserUtil.getAndConsumeUserName(result.getUserId(), (userName) -> {
                        Message message = MessageUtil.buildNotifyMessage(String.format("%s leave the chatroom", userName));
                        insertMessage(message);
                    });

                    c.getChatroom().setOnlineMemberCount(c.getChatroom().getOnlineMemberCount() - 1);
                    renderChatroomOnlineMemberCount();
                    break;
                }
                case DELETE_CHATROOM: {
                    // 跳转回直播页，由直播页的监听器处理该通知
                    onBackPressed();
                    break;
                }
                default:
                    break;
            }
        });
    }

//    private void closeChatroom() {
//        // 退出聊天室
//        AlipayCcmIMClient.getInstance().getConversationManager().clearCurrentConversation();
//        leaveChatroom();
//
//        // 强制跳转回聊天室列表页，因为如果在消息页和直播页之间来回切换过，直接返回会默认跳转到直播页，而不是退出聊天室
//        Intent intent = new Intent(this, ChatroomListActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        this.startActivity(intent);
//    }
//
//    private void leaveChatroom() {
//        chatroomViewModel.leaveChatroom(c.getCid());
//    }

}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.chatroom;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;
import com.hsbcd.mpaastest.kotlin.samples.util.MessageUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.UserUtil;

import cn.hsbcsd.mpaastest.R;
import cn.hsbcsd.mpaastest.databinding.MessageOfChatroomLiveBinding;

/**
 * @author liyalong
 * @version ChatroomLiveMessageItemHolder.java, v 0.1 2022年11月11日 16:50 liyalong
 */
public class ChatroomLiveMessageItemHolder extends BaseListItemHolder<Message> {

    private MessageOfChatroomLiveBinding binding;

    public ChatroomLiveMessageItemHolder(Context context, @NonNull View itemView) {
        super(context, itemView);

        this.binding = MessageOfChatroomLiveBinding.bind(itemView);
    }

    @Override
    public void bind(Message item) {
        if (MessageUtil.isSystemMessage(item)) {
            binding.senderUserName.setVisibility(View.GONE);
            binding.messageContent.setTextColor(context.getColor(R.color.orange));
        } else {
            binding.senderUserName.setVisibility(View.VISIBLE);
            binding.messageContent.setTextColor(context.getColor(R.color.white));

            UserUtil.getAndConsumeUserName(item.getFrom().getUid(), (userName) -> {
                binding.senderUserName.setText(String.format("%s：", userName));
            });
        }

        binding.messageContent.setText(MessageUtil.getContentText(item));
    }

}
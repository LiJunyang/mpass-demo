/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.content.Context;
import android.view.View;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import cn.com.hsbc.hsbcchina.cert.databinding.MessageOfTopicOriginalBinding;

/**
 * 话题原始消息列表项
 *
 * @author liyalong
 * @version TopicOriginalMessageHolder.java, v 0.1 2022年09月27日 14:25 liyalong
 */
public class TopicOriginalMessageHolder extends SpecifiedMessageHolder {

    private MessageOfTopicOriginalBinding binding;

    /**
     * 构造器
     *
     * @param context
     * @param itemView
     */
    public TopicOriginalMessageHolder(Context context, View itemView) {
        super(context, itemView);

        binding = MessageOfTopicOriginalBinding.bind(itemView);
        initMessageBinding(binding.messageContainer, null);
        initBindingView(binding.senderAvatar, binding.senderUserName, binding.messageSendTime);

        setTopicMessageMode(true);
    }

    /**
     * 绑定消息
     *
     * @param message
     */
    @Override
    public void bind(Message message) {
        super.bind(message);

        // 绑定消息弹出菜单
        bindMessagePopupMenu(message);
    }

}
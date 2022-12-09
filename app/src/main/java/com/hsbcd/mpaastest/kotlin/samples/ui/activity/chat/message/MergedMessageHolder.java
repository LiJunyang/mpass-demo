/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.content.Context;
import android.view.View;

import cn.com.hsbc.hsbcchina.cert.databinding.MessageOfMergedBinding;

/**
 * 合并转发消息列表项
 *
 * @author liyalong
 * @version MergedMessageHolder.java, v 0.1 2022年08月26日 21:54 liyalong
 */
public class MergedMessageHolder extends SpecifiedMessageHolder {

    private MessageOfMergedBinding binding;

    /**
     * 构造器
     *
     * @param context
     * @param itemView
     */
    public MergedMessageHolder(Context context, View itemView) {
        super(context, itemView);

        binding = MessageOfMergedBinding.bind(itemView);
        initMessageBinding(binding.messageContainer, null);
        initBindingView(binding.senderAvatar, binding.senderUserName, binding.messageSendTime);
    }

}
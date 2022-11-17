/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.content.Context;
import android.view.View;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import cn.hsbcsd.mpaastest.databinding.MessageSecretChatNotifyBinding;

/**
 * 密聊会话提示信息
 *
 * @author liyalong
 * @version SecretChatNotifyMessageHolder.java, v 0.1 2022年10月25日 19:57 liyalong
 */
public class SecretChatNotifyMessageHolder extends AbstractMessageHolder {

    private MessageSecretChatNotifyBinding binding;

    /**
     * 构造器
     *
     * @param context
     * @param itemView
     */
    public SecretChatNotifyMessageHolder(Context context, View itemView) {
        super(context, itemView);

        binding = MessageSecretChatNotifyBinding.bind(itemView);
    }

    @Override
    protected void showNormalMessage() {
    }

    @Override
    protected void renderRecalledMessage(Message message) {
    }

    @Override
    public void bind(Message item) {
    }

    public void bind() {
        // TODO 根据用户设置，展示密聊设置信息
    }

}
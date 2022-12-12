/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.alipay.fc.ccmimplus.common.message.domain.MessageContentType;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.CustomContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import cn.hsbcsd.mpaastest.databinding.MessageOfSystemBinding;

/**
 * 系统消息holder
 *
 * @author liyalong
 * @version SystemMessageHolder.java, v 0.1 2022年08月12日 11:54 liyalong
 */
public class SystemMessageHolder extends AbstractMessageHolder {

    private MessageOfSystemBinding binding;

    /**
     * 构造器
     *
     * @param context
     * @param itemView
     */
    public SystemMessageHolder(Context context, View itemView) {
        super(context, itemView);

        binding = MessageOfSystemBinding.bind(itemView);
    }

    @Override
    protected void showNormalMessage() {

    }

    @Override
    protected void renderRecalledMessage(Message message) {

    }

    /**
     * 绑定消息
     *
     * @param message
     */
    @Override
    public void bind(Message message) {
        switch (message.getContentTypeCode()) {
            case MessageContentType.CUSTOM_VALUE: {
                rendCustomMessage(message);
                break;
            }
            default:
                break;
        }
    }

    private void rendCustomMessage(Message message) {
        CustomContent content = message.getCustomMessageContent();

        JSONObject data = JSONObject.parseObject(new String(content.getData()));
        String type = data.getString("type");
        String title = data.getString("title");

        Log.i(LoggerName.UI, String.format("收到系统消息: %s/%s", type, title));
        binding.msgOfSystem.setText(title);
    }
}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.CustomContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.hsbcd.mpaastest.kotlin.samples.model.LiveDataResult;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.connection.Connection;
import com.alipay.fc.ccmimplus.sdk.core.message.MessageListener;

/**
 * 新消息数据层，专门用于监听会话列表的新消息
 *
 * @author liyalong
 * @version NewMessageViewModel.java, v 0.1 2022年08月29日 17:25 liyalong
 */
public class NewMessageViewModel extends ViewModel implements MessageListener {

    private MutableLiveData<LiveDataResult<Message>> newMessageResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult<CustomContent>> customCommandResult = new MutableLiveData<>();

    public NewMessageViewModel() {
        AlipayCcmIMClient.getInstance().getMessageManager().registerUserListener(this);
    }

    @Override
    public void onNewMessage(MessageListener messageListener, Connection connection, Message message) {
        if (!(messageListener instanceof NewMessageViewModel)) {
            return;
        }

        LiveDataResult<Message> result = new LiveDataResult<>(true, message);
        newMessageResult.postValue(result);
    }

    @Override
    public void onMessageSendSuccess(Connection connection, Message message) {

    }

    @Override
    public void onCustomCmd(Connection connection, Message message, CustomContent customContent) {
        LiveDataResult<CustomContent> result = new LiveDataResult<>(true, customContent);
        customCommandResult.postValue(result);
    }

    public MutableLiveData<LiveDataResult<Message>> getNewMessageResult() {
        return newMessageResult;
    }

    public MutableLiveData<LiveDataResult<CustomContent>> getCustomCommandResult() {
        return customCommandResult;
    }
}
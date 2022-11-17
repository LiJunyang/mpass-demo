/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.model;


import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;

/**
 * @author liyalong
 * @version ReceiveMessageResult.java, v 0.1 2022年08月08日 10:17 liyalong
 */
public class ReceiveMessageResult {

    private Type type;

    private Message message;

    public ReceiveMessageResult(Type type, Message message) {
        this.type = type;
        this.message = message;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public enum Type {
        Normal,
        NotifyMessageReach,
        AckMessageRead,
        AckBatchMessageRead,
        AckMessageRecalled,
        CmdRecall,
        CmdSetAllMsgRead,
        QuoteReplyInfo
    }

}
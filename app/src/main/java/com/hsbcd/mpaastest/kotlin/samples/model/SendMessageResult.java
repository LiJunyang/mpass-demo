/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.model;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;

/**
 * @author liyalong
 * @version SendMessageResult.java, v 0.1 2022年08月17日 16:32 liyalong
 */
public class SendMessageResult {

    private Status status;

    private Message message;

    private String errorCode;

    private String errorMessage;

    private MediaSendingProgress progress;

    /**
     * 如果发送的是引用回复消息，则返回发送结果时带上话题id(即话题原始消息id)
     */
    private String topicId;

    public SendMessageResult(Status status, Message message) {
        this.status = status;
        this.message = message;
    }

    public SendMessageResult(Status status, Message message, String errorCode, String errorMessage) {
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
    
    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public MediaSendingProgress getProgress() {
        return progress;
    }

    public void setProgress(MediaSendingProgress progress) {
        this.progress = progress;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }

    public enum Status {
        BEGIN,
        IN_PROGRESS,
        SUCCESS,
        FAIL
    }
}
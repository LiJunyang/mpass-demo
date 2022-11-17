/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.model;

import com.alipay.fc.ccmimplus.common.message.domain.MessageContentType;
import com.alipay.fc.ccmimplus.sdk.core.message.SendingProgress;

/**
 * 多媒体发送进度
 *
 * @author liyalong
 * @version MediaSendingProgress.java, v 0.1 2022年08月19日 10:06 liyalong
 */
public class MediaSendingProgress extends SendingProgress {

    private MessageContentType type;

    public MediaSendingProgress(long total, long sent, boolean finished, MessageContentType type) {
        super(total, sent, finished);
        this.type = type;
    }

    public MessageContentType getType() {
        return type;
    }

    public void setType(MessageContentType type) {
        this.type = type;
    }
}
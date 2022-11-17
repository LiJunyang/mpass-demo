/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;

/**
 * @author liyalong
 * @version SelectSessionViewModel.java, v 0.1 2022年08月30日 14:19 liyalong
 */
public class SelectSessionViewModel extends SelectItemViewModel<Conversation> {

    @Override
    protected String getUniqueKey(Conversation item) {
        return item.getCid();
    }
}
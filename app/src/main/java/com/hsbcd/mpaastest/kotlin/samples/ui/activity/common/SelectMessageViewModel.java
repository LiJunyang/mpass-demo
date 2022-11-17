/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;

/**
 * @author liyalong
 * @version SelectMessageViewModel.java, v 0.1 2022年08月30日 14:19 liyalong
 */
public class SelectMessageViewModel extends SelectItemViewModel<Message> {

    @Override
    protected String getUniqueKey(Message item) {
        return String.format("%s|%s", item.getSid(), item.getId());
    }
}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.merged;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.com.hsbc.hsbcchina.cert.R;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.MergedMessageHolder;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListAdapter;

/**
 * 合并转发消息列表
 *
 * @author liyalong
 * @version MergedMessageListAdapter.java, v 0.1 2022年08月26日 20:10 liyalong
 */
public class MergedMessageListAdapter extends BaseListAdapter<Message, MergedMessageHolder> {

    public MergedMessageListAdapter(Context context) {
        super(context);
    }

    @Override
    protected MergedMessageHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_of_merged, parent, false);
        MergedMessageHolder holder = new MergedMessageHolder(this.context, view);
        return holder;
    }
    
}
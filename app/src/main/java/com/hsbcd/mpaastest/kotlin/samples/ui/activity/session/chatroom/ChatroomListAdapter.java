/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListAdapter;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;

import cn.hsbcsd.mpaastest.R;


/**
 * 聊天室会话列表
 *
 * @author liyalong
 * @version ChatroomListAdapter.java, v 0.1 2022年11月09日 10:40 liyalong
 */
public class ChatroomListAdapter extends BaseListAdapter<Conversation, ChatroomItemHolder> {

    public ChatroomListAdapter(Context context,
                               BaseListItemHolder.OnItemActionListener<Conversation> onItemActionListener) {
        super(context, onItemActionListener);
    }

    @Override
    protected ChatroomItemHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_chatroom_item, parent,
                false);
        return new ChatroomItemHolder(this.context, view, onItemActionListener);
    }

}
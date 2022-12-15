/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.chatroom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.MsgUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListAdapter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import cn.hsbcsd.mpaastest.R;

/**
 * @author liyalong
 * @version ChatroomLiveMessageListAdapter.java, v 0.1 2022年11月11日 16:57 liyalong
 */
public class ChatroomLiveMessageListAdapter extends BaseListAdapter<Message, ChatroomLiveMessageItemHolder> {

    public ChatroomLiveMessageListAdapter(Context context) {
        super(context);
    }

    @Override
    protected ChatroomLiveMessageItemHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_of_chatroom_live, parent, false);
        return new ChatroomLiveMessageItemHolder(context, view);
    }

    /**
     * 将集合插入到指定位置
     * <p>
     * 根据消息id去重
     *
     * @param index
     * @param list
     */
    public void addAll(int index, List<Message> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        List<Message> filteredMsgList = new ArrayList<>();
        for (Message message : list) {

            //如果已经存在，则不添加
            if (isMessageExists(message)) {
                continue;
            }

            filteredMsgList.add(message);
        }
        items.addAll(index, filteredMsgList);
    }
    
    /**
     * 新增消息
     *
     * @param message
     */
    public void addOne(Message message) {
        if (!isMessageExists(message)) {
            items.add(message);
        }
    }

    private boolean isMessageExists(Message message) {
        return CollectionUtils.findOne(items,
                (msg) -> StringUtils.equalsIgnoreCase(MsgUtils.toUniqueKey(message),
                        MsgUtils.toUniqueKey(msg))) != null;
    }

    /**
     * 从列表中删除指定消息
     *
     * @return
     */
    public void remove(Message message) {
        int index = indexOf(message.getTntInstId(), message.getSid(), message.getLocalId());
        if (index == -1) {
            return;
        }

        removeItem(index);
    }

    /**
     * 返回消息索引
     *
     * @param tntInstId 租户id
     * @param localId   本地消息id
     * @return
     */
    public int indexOf(String tntInstId, String cid, String localId) {
        int index = CollectionUtils.indexOf(items,
                msg -> StringUtils.equalsIgnoreCase(msg.getTntInstId(), tntInstId)
                        && StringUtils.equalsIgnoreCase(msg.getSid(), cid)
                        && StringUtils.equalsIgnoreCase(msg.getLocalId(), localId));
        return index;
    }

}
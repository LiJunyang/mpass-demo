/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import cn.com.hsbc.hsbcchina.cert.R;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListAdapter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liyalong
 * @version TopicMessageListAdapter.java, v 0.1 2022年09月27日 14:02 liyalong
 */
public class TopicMessageListAdapter extends BaseListAdapter<Message, AbstractMessageHolder> {

    public TopicMessageListAdapter(Context context) {
        super(context);
    }

    @Override
    protected AbstractMessageHolder getViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            // 话题原始消息
            case R.id.VIEW_TYPE_TOPIC_ORIGINAL_MESSAGE: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_of_topic_original, parent,
                        false);
                return new TopicOriginalMessageHolder(this.context, view);
            }
            // 自己发送的消息
            case R.id.VIEW_TYPE_MESSAGE_SENT: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_of_me, parent, false);
                return new SendMessageHolder(this.context, view, true);
            }
            // 别人发送的消息
            case R.id.VIEW_TYPE_MESSAGE_RECEIVED: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_of_other, parent, false);
                return new ReceivedMessageHolder(this.context, view, true);
            }
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractMessageHolder holder, int position) {
        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        Message message = items.get(position);
        holder.itemView.setTag(R.id.TAG_KEY_MESSAGE, message);

        holder.bind(message);
    }

    @Override
    public int getItemViewType(int position) {
        Message message = items.get(position);

        if (position == 0) {
            return R.id.VIEW_TYPE_TOPIC_ORIGINAL_MESSAGE;
        } else if (isSelfMessage(message)) {
            return R.id.VIEW_TYPE_MESSAGE_SENT;
        } else {
            return R.id.VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    private boolean isSelfMessage(Message message) {
        String currentUserId = AlipayCcmIMClient.getInstance().getInitConfig().getUserId();
        return StringUtils.equalsIgnoreCase(currentUserId, message.getFrom().getUid());
    }

    /**
     * 将消息列表追加到最后
     * <p>
     * 根据消息id去重
     *
     * @param list
     */
    public void addAll(List<Message> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        List<Message> filteredMsgList = new ArrayList<>();
        for (Message message : list) {

            //如果已经存在，则更新原有消息
            int index = indexOf(message.getTntInstId(), message.getSid(), message.getLocalId());
            if (index != -1) {
                items.set(index, message);
                continue;
            }

            filteredMsgList.add(message);
        }
        items.addAll(filteredMsgList);
    }

    /**
     * 新增消息
     *
     * @param message
     */
    public void addOne(Message message) {
        items.add(message);
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

    /**
     * 返回消息索引
     *
     * @param tntInstId 租户id
     * @param msgId     服务端消息id
     * @return
     */
    public int indexOfByMsgId(String tntInstId, String cid, long msgId) {
        int index = CollectionUtils.indexOf(items,
                msg -> StringUtils.equalsIgnoreCase(msg.getTntInstId(), tntInstId)
                        && StringUtils.equalsIgnoreCase(msg.getSid(), cid)
                        && StringUtils.equalsIgnoreCase(msg.getId(), msgId + ""));
        return index;
    }
    
}
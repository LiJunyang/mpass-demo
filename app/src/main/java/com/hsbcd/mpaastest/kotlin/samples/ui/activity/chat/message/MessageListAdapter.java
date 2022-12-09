/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import cn.com.hsbc.hsbcchina.cert.R;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.model.MediaSendingProgress;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.message.SendingProgress;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.MsgUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListAdapter;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author liyalong
 * @version MessageListAdapter.java, v 0.1 2022年08月03日 14:10 liyalong
 */
public class MessageListAdapter extends BaseListAdapter<Message, AbstractMessageHolder> {

    private boolean isSecretChat;

    public MessageListAdapter(Context context, boolean isSecretChat) {
        super(context);
        this.isSecretChat = isSecretChat;
    }

    @Override
    protected AbstractMessageHolder getViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            // 自己发送的消息
            case R.id.VIEW_TYPE_MESSAGE_SENT: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_of_me, parent, false);
                return new SendMessageHolder(this.context, view, false);
            }
            // 别人发送的消息
            case R.id.VIEW_TYPE_MESSAGE_RECEIVED: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_of_other, parent, false);
                return new ReceivedMessageHolder(this.context, view, false);
            }
            // 系统消息
            case R.id.VIEW_TYPE_SYSTEM: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_of_system, parent, false);
                return new SystemMessageHolder(this.context, view);
            }
            // 密聊提示信息
            case R.id.VIEW_TYPE_SECRET_CHAT_NOTIFY: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_secret_chat_notify,
                        parent, false);
                return new SecretChatNotifyMessageHolder(this.context, view);
            }
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractMessageHolder holder, int position) {
        Log.e(LoggerName.UI, "MessageListAdpater.onBindViewHolder: " + position);

        // 如果是密聊会话的第一个列表元素，则展示密聊提示信息
        if (isSecretChat && position == 0) {
            ((SecretChatNotifyMessageHolder) holder).bind();
            return;
        }

        if (CollectionUtils.isEmpty(items)) {
            return;
        }

        Message message = items.get(getRealPosition(position));
        holder.itemView.setTag(R.id.TAG_KEY_MESSAGE, message);

        holder.bind(message);
    }

    /**
     * 根据payloads更新指定项
     *
     * @param holder
     * @param position
     * @param payloads
     */
    @Override
    public void onBindViewHolder(AbstractMessageHolder holder, int position, List payloads) {
        if (CollectionUtils.isEmpty(payloads)) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }

        for (Object payload : payloads) {
            // 更新多媒体消息发送(上传)进度
            if (payload instanceof SendingProgress && holder instanceof SendMessageHolder) {
                MediaSendingProgress progress = (MediaSendingProgress) payload;
                SendMessageHolder sendMessageHolder = (SendMessageHolder) holder;

                sendMessageHolder.hideReSendButton();

                if (progress.isFinished()) {
                    sendMessageHolder.finishSendingProgress(progress.getType());
                } else {
                    sendMessageHolder.updateSendingProgress(progress);
                }
            }

//            //如果是消息定位器
//            if (payload instanceof MessageInChatWindowLocator) {
//                ((AbstractMessageHolder) holder).locateMessage((MessageInChatWindowLocator) payload);
//            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // 如果是密聊会话的第一个列表元素，则展示密聊提示信息
        if (isSecretChat && position == 0) {
            return R.id.VIEW_TYPE_SECRET_CHAT_NOTIFY;
        }

        Message message = items.get(getRealPosition(position));

        if (isSelfMessage(message)) {
            return R.id.VIEW_TYPE_MESSAGE_SENT;
        } else if (isSystemMessage(message)) {
            return R.id.VIEW_TYPE_SYSTEM;
        } else {
            return R.id.VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @Override
    public int getItemCount() {
        // 密聊会话的消息列表需要多一个元素，用于展示密聊提示信息
        if (isSecretChat) {
            return super.getItemCount() + 1;
        }

        return super.getItemCount();
    }

    private boolean isSelfMessage(Message message) {
        String currentUserId = AlipayCcmIMClient.getInstance().getInitConfig().getUserId();
        return StringUtils.equalsIgnoreCase(currentUserId, message.getFrom().getUid());
    }

    private boolean isSystemMessage(Message message) {
        return StringUtils.equalsIgnoreCase(message.getFrom().getUid(), CommonConstants.SYS_USER_ID);
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

    /**
     * 获取消息对象
     *
     * @param index
     * @return
     */
    public Message getMessage(int index) {
        if (index > items.size() || index < 0) {
            return null;
        }
        return items.get(getRealPosition(index));
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

    public void batchRemove(Collection<Message> messages) {
        messages.forEach(m -> remove(m));
    }

    private int getRealPosition(int position) {
        // 如果是密聊会话，由于在列表最前面插入了一个提示信息的item，获取消息在item列表中的位置时，需要减去偏移量
        return isSecretChat ? (position - 1) : position;
    }

}
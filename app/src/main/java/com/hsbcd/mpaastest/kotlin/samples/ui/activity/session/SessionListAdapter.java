/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.com.hsbc.hsbcchina.cert.R;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.ConversationUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListAdapter;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.ListIterator;

/**
 * 会话列表适配器
 *
 * @author liyalong
 * @version MessageFragment.java, v 0.1 2022年07月29日 15:53 liyalong
 */
public class SessionListAdapter extends BaseListAdapter<Conversation, AbstractSessionItemHolder> {

    private SessionListTypeEnum sessionListType = SessionListTypeEnum.NORMAL;

    public SessionListAdapter(Context context) {
        super(context);
        this.setHasStableIds(true);
    }

    public SessionListAdapter(Context context, SessionListTypeEnum sessionListType) {
        super(context);
        this.sessionListType = sessionListType;
    }

    public SessionListAdapter(Context context, SessionListTypeEnum sessionListType,
                              BaseListItemHolder.OnItemActionListener<Conversation> onItemActionListener) {
        super(context, onItemActionListener);
        this.sessionListType = sessionListType;
    }

    @Override
    public long getItemId(int position) {
        Conversation c = items.get(position);
        return ConversationUtils.uniqueCode(c);
    }

    @Override
    protected AbstractSessionItemHolder getViewHolder(ViewGroup parent, int viewType) {
        switch (sessionListType) {
            case NORMAL: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_session_item, parent,
                        false);
                return new SessionItemHolder(context, view);
            }
            case FOR_SELECT: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_select_session_item,
                        parent,
                        false);
                return new SelectSessionItemHolder(context, view);
            }
            case SECRET_CHAT: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_secret_chat_session_item,
                        parent,
                        false);
                return new SecretChatSessionItemHolder(context, view, onItemActionListener);
            }
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(AbstractSessionItemHolder holder, int position, List payloads) {
        if (CollectionUtils.isEmpty(payloads)) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }

        for (Object payload : payloads) {
            // 更新会话的最近一条消息
            if (payload instanceof Message) {
                Message message = (Message) payload;
                ((SessionItemHolder) holder).updateLastMessage(message);
            }
        }
    }

    /**
     * 获取index
     *
     * @param cid
     * @return
     */
    public int indexOf(String cid) {
        return CollectionUtils.indexOf(items,
                conversation -> StringUtils.equalsIgnoreCase(conversation.getCid(), cid));
    }

    /**
     * 根据cid 查找会话
     *
     * @param cid
     * @return
     */
    public Conversation getByCid(String cid) {
        return CollectionUtils.findOne(items,
                conversation -> StringUtils.equalsIgnoreCase(conversation.getCid(), cid));
    }

    /**
     * 重置
     *
     * @param list
     */
    public void resetConversations(List<Conversation> list) {
        clearAll();
        addAll(list);
    }

    public Conversation removeByCid(String cid) {
        Conversation c = null;

        ListIterator<Conversation> iterator = items.listIterator();
        while (iterator.hasNext()) {
            Conversation current = iterator.next();
            if (StringUtils.equalsIgnoreCase(current.getCid(), cid)) {
                c = current;
                iterator.remove();
            }
        }

        return c;
    }

    public void insertToTop(Conversation c) {
        if (this.items.size() == 0) {
            this.items.add(c);
            return;
        }

        // 找到最后一个置顶会话的位置(如果有的话)
        boolean hasTopSession = false;
        int lastTopPosition = 0;
        for (int i = 0; i < items.size(); i++) {
            Conversation each = items.get(i);
            if (each.isTopMode()) {
                hasTopSession = true;
                lastTopPosition = i;
            }
        }

        // 把指定会话插到置顶会话后面
        this.items.add(hasTopSession ? (lastTopPosition + 1) : 0, c);
    }

    @Override
    public void addAll(List<Conversation> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }

        CollectionUtils.addAllToList(items, list,
                (a, b) -> StringUtils.equalsIgnoreCase(a.getTntInstId(), b.getTntInstId()) &&
                        StringUtils.equalsIgnoreCase(a.getCid(), b.getCid()));
    }

    public enum SessionListTypeEnum {
        NORMAL,
        FOR_SELECT,
        SECRET_CHAT
    }

}
/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session;

import android.content.Context;
import android.view.View;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;
import com.hsbcd.mpaastest.kotlin.samples.util.DateUtil;
import com.guanaj.easyswipemenulibrary.EasySwipeMenuLayout;

import org.apache.commons.lang3.StringUtils;

import cn.com.hsbc.hsbcchina.cert.databinding.FragmentSecretChatSessionItemBinding;

/**
 * 密聊会话列表项holder
 *
 * @author liyalong
 * @version SecretChatSessionItemHolder.java, v 0.1 2022年10月21日 16:13 liyalong
 */
public class SecretChatSessionItemHolder extends AbstractSessionItemHolder {

    private FragmentSecretChatSessionItemBinding binding;

    public SecretChatSessionItemHolder(Context context, View itemView, OnItemActionListener onItemActionListener) {
        super(context, itemView, onItemActionListener);

        binding = FragmentSecretChatSessionItemBinding.bind(itemView);
    }

    /**
     * 绑定数据
     *
     * @param conversation
     */
    @Override
    public void bind(Conversation conversation) {
        super.c = conversation;

        // 绑定组件动作
        bindAction();

        // 会话标题
        renderTitle(binding.title);

        // 消息未读数
        renderUnReadCount();

        // 最近一条消息
        renderLastMessage();

        // 最近一条消息的时间戳
        renderLastMessageTime();
    }

    private void bindAction() {
        // 绑定点击列表项动作
        if (onItemActionListener != null) {
            binding.content.setOnClickListener(v -> onItemActionListener.onClickItem(c));
        }

        // 绑定侧滑菜单动作
        bindSwipeMenu();
    }

    private void bindSwipeMenu() {
        EasySwipeMenuLayout swipeMenu = binding.getRoot();

        if (onItemActionListener instanceof OnSessionItemActionListener) {
            // 点击侧滑菜单项-清空聊天记录
            binding.clearMessage.setOnClickListener(v -> {
                ((OnSessionItemActionListener) onItemActionListener).onClearMessage(c);
                swipeMenu.resetStatus();
            });

            // 点击侧滑菜单项-移除
            binding.remove.setOnClickListener(v -> {
                onItemActionListener.onRemoveItem(c, getBindingAdapterPosition());
                swipeMenu.resetStatus();
            });
        }
    }

    private void renderUnReadCount() {
        doRenderUnReadCount(c.getUnReadCount());
    }

    private void doRenderUnReadCount(int unReadCount) {
        if (unReadCount > 0) {
            binding.unReadMsgCount.setText(c.getUnReadCount() > 99 ? "99+" : String.valueOf(c.getUnReadCount()));
            binding.unReadMsgCount.setVisibility(View.VISIBLE);
        } else {
            binding.unReadMsgCount.setText(String.valueOf(0));
            binding.unReadMsgCount.setVisibility(View.GONE);
        }
    }

    private void renderLastMessage() {
        binding.lastTextMsg.setText(c.getLastMsg() == null ? StringUtils.EMPTY : "消息");
    }

    private void renderLastMessageTime() {
        if (c.getLastMsg() == null) {
            binding.msgSendTime.setText(StringUtils.EMPTY);
            return;
        }

        doRenderLastMessageTime(c.getLastMsg());
    }

    private void doRenderLastMessageTime(Message message) {
        long timestamp = message.getTimestamp();
        binding.msgSendTime.setText(DateUtil.getMessageSendTime(timestamp));
    }

    public interface OnSessionItemActionListener extends BaseListItemHolder.OnItemActionListener<Conversation> {
        void onClearMessage(Conversation c);
    }

}
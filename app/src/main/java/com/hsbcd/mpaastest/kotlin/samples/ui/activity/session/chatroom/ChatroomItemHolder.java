/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom;

import android.content.Context;
import android.view.View;

import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.guanaj.easyswipemenulibrary.EasySwipeMenuLayout;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.AbstractSessionItemHolder;

import cn.hsbcsd.mpaastest.databinding.FragmentFavoriteSessionItemBinding;

/**
 * 聊天室会话列表项
 *
 * @author liyalong
 * @version ChatroomItemHolder.java, v 0.1 2022年11月09日 10:40 liyalong
 */
public class ChatroomItemHolder extends AbstractSessionItemHolder {

    private FragmentFavoriteSessionItemBinding binding;

    public ChatroomItemHolder(Context context, View itemView, OnItemActionListener onItemActionListener) {
        super(context, itemView, onItemActionListener);

        binding = FragmentFavoriteSessionItemBinding.bind(itemView);
    }

    /**
     * 绑定数据
     *
     * @param c
     */
    @Override
    public void bind(Conversation c) {
        this.c = c;

        // 会话头像
        renderSessionLogoUrl(binding.avatar);

        // 会话标题
        renderTitle(binding.title);

        // 绑定组件动作
        bindAction(c);
    }

    private void bindAction(Conversation c) {
        if (onItemActionListener != null) {
            binding.sessionInfoLayout.setOnClickListener(v -> onItemActionListener.onClickItem(c));
        }

        // 绑定侧滑菜单
        bindSwipeMenu(c);
    }

    private void bindSwipeMenu(Conversation c) {
        EasySwipeMenuLayout swipeMenu = binding.getRoot();

        // 如果未注册列表项监听事件，则禁用侧滑菜单
        // 暂时不用侧滑菜单，默认禁用
        //if (onItemActionListener == null) {
        swipeMenu.setCanLeftSwipe(false);
        return;
        //}
    }

}
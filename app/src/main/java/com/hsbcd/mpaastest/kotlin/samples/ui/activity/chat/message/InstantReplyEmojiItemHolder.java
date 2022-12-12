/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import cn.hsbcsd.mpaastest.databinding.InstantReplyEmojiItemBinding;
import com.hsbcd.mpaastest.kotlin.samples.model.InstantReplyEmojiItem;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.ChatActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author liyalong
 * @version InstantReplyEmojiItemHolder.java, v 0.1 2022年09月22日 19:57 liyalong
 */
public class InstantReplyEmojiItemHolder extends BaseListItemHolder<InstantReplyEmojiItem> {

    private InstantReplyEmojiItemBinding binding;

    public InstantReplyEmojiItemHolder(Context context, @NonNull View itemView,
                                       BaseListItemHolder.OnItemActionListener onItemActionListener) {
        super(context, itemView, onItemActionListener);

        this.binding = InstantReplyEmojiItemBinding.bind(itemView);
    }

    @Override
    public void bind(InstantReplyEmojiItem item) {

    }

    public void bind(InstantReplyEmojiItem emojiItem, boolean lastItem) {
        // 表情图标
        Glide.with(context).load(emojiItem.getEmoji().getLocalFilePath()).diskCacheStrategy(
                DiskCacheStrategy.AUTOMATIC).into(binding.emoji);

        // 发送该快捷回复表情的用户名称列表
        List<String> senderUserNames = emojiItem.getSenderUserInfoVOs().stream()
                .map(u -> StringUtils.defaultIfBlank(u.getNickName(), u.getUserName()))
                .collect(Collectors.toList());
        binding.senderUserNameList.setText(StringUtils.join(senderUserNames, ", "));

        if (context instanceof ChatActivity) {
            // 新增表情的按钮，仅在最后一个列表项可见
            binding.addEmojiLayout.setVisibility(lastItem ? View.VISIBLE : View.GONE);
        } else {
            // 如果当前不在聊天消息页，例如查看合并转发消息或收藏消息，则隐藏新增表情按钮
            binding.addEmojiLayout.setVisibility(View.GONE);
        }

        binding.emoji.setOnClickListener(v -> {
            if (onItemActionListener != null) {
                onItemActionListener.onClickItem(emojiItem);
            }
        });

        binding.addEmojiLayout.setOnClickListener(v -> {
            if (onItemActionListener != null && onItemActionListener instanceof OnEmojiItemActionListener) {
                ((OnEmojiItemActionListener) onItemActionListener).onClickAddEmoji();
            }
        });
    }

    public interface OnEmojiItemActionListener extends OnItemActionListener<InstantReplyEmojiItem> {
        void onClickAddEmoji();
    }

}
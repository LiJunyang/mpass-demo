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

import cn.hsbcsd.mpaastest.R;
import com.hsbcd.mpaastest.kotlin.samples.model.InstantReplyEmojiItem;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListAdapter;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;

/**
 * @author liyalong
 * @version InstantReplyEmojiListAdapter.java, v 0.1 2022年09月22日 19:57 liyalong
 */
public class InstantReplyEmojiListAdapter extends BaseListAdapter<InstantReplyEmojiItem, InstantReplyEmojiItemHolder> {

    public InstantReplyEmojiListAdapter(Context context,
                                        BaseListItemHolder.OnItemActionListener onItemActionListener) {
        super(context, onItemActionListener);
    }

    @Override
    protected InstantReplyEmojiItemHolder getViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.instant_reply_emoji_item, parent, false);
        return new InstantReplyEmojiItemHolder(context, view, onItemActionListener);
    }

    @Override
    public void onBindViewHolder(@NonNull InstantReplyEmojiItemHolder holder, int position) {
        InstantReplyEmojiItem emojiItem = items.get(position);
        boolean lastItem = (position == items.size() - 1);

        holder.bind(emojiItem, lastItem);
    }
    
}
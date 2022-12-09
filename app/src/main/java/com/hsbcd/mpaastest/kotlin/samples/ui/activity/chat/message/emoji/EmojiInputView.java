/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import cn.com.hsbc.hsbcchina.cert.databinding.EmojiInputBinding;


/**
 * 表情消息输入框
 *
 * @author maping.mp
 * @version 1.0: EmojiInputView.java, v 0.1 2022年01月01日 3:58 下午 maping.mp Exp $
 */
public class EmojiInputView extends LinearLayout {

    private EmojiInputBinding binding;

    private EmojiGridAdapter adapter;

    private OnClickButtonListener onClickButtonListener;

    public EmojiInputView(Context context) {
        super(context);
        initBindComponents(context);
    }

    public EmojiInputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initBindComponents(context);
    }

    public EmojiInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initBindComponents(context);
    }

    private void initBindComponents(Context context) {
        binding = EmojiInputBinding.inflate(LayoutInflater.from(getContext()), this, true);

        this.adapter = new EmojiGridAdapter(context);
        binding.emojiGrid.setAdapter(this.adapter);

        binding.deleteEmoji.setOnClickListener(v -> {
            if (onClickButtonListener != null) {
                onClickButtonListener.onClickDeleteEmoji();
            }
        });

        binding.sendEmoji.setEnabled(false);
        binding.sendEmoji.setOnClickListener(v -> {
            if (onClickButtonListener != null) {
                onClickButtonListener.onClickSendEmoji();
            }
        });

        removeAllViews();
        addView(binding.getRoot());
    }

    public void setSendEmojiButtonEnabled(boolean enable) {
        binding.sendEmoji.setEnabled(enable);
    }

    public EmojiGridAdapter getAdapter() {
        return adapter;
    }

    public void setOnClickButtonListener(OnClickButtonListener onClickButtonListener) {
        this.onClickButtonListener = onClickButtonListener;
    }

    public interface OnClickButtonListener {
        void onClickSendEmoji();

        void onClickDeleteEmoji();
    }

}
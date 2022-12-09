/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.emoji.EmojiGridAdapter;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import java.util.Arrays;

import cn.com.hsbc.hsbcchina.cert.databinding.PopupMessageBinding;

/**
 * 单条消息长按后的弹窗菜单
 *
 * @author liyalong
 * @version MessagePopupMenu.java, v 0.1 2022年08月24日 13:42 liyalong
 */
public class MessagePopupMenu extends AppCompatDialogFragment {

    private Context context;

    private View anchorView;

    /**
     * 弹窗对应的原始消息
     */
    private Message message;

    /**
     * 消息在列表中的位置
     */
    private int position;

    private boolean topicMessageMode;

    private PopupMessageBinding binding;

    private EmojiGridAdapter adapter;

    public MessagePopupMenu(Context context, View anchorView, Message message, int position, boolean topicMessageMode) {
        this.context = context;
        this.anchorView = anchorView;
        this.message = message;
        this.position = position;
        this.topicMessageMode = topicMessageMode;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        setStyle();
        setPosition();

        binding = PopupMessageBinding.inflate(inflater);

        // 绑定表情网格视图
        bindEmojiGrid();

        // 绑定组件动作
        bindAction();

        if (topicMessageMode) {
            binding.divide.setVisibility(View.GONE);
            binding.messageProcessLayout.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }

    private void setStyle() {
        Window window = getDialog().getWindow();

        // 设置对话框原始背景为透明，便于渲染自定义背景
        window.setBackgroundDrawableResource(android.R.color.transparent);
        // 设置对话框弹出时，activity保持原有亮度
        window.setDimAmount(0);
    }

    private void setPosition() {
        // 获取锚点消息视图在屏幕中的位置
        int[] location = new int[2];
        anchorView.getLocationOnScreen(location);
        int sourceX = location[0];
        int sourceY = location[1];

        // 设置弹窗位置在锚点附近
        Window window = getDialog().getWindow();
        window.setGravity(Gravity.TOP | Gravity.LEFT);
        window.getAttributes().x = sourceX + dpToPx((sourceX == 0) ? 20 : 0);
        window.getAttributes().y = sourceY + dpToPx(50);
    }

    private int dpToPx(float valueInDp) {
        DisplayMetrics metrics = getActivity().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);
    }

    private void bindEmojiGrid() {
        this.adapter = new EmojiGridAdapter(context);
        this.adapter.setOnClickItemListener(e -> {
            ((ChatActivity) context).sendInstantReplyEmoji(message, e);
            dismiss();
        });

        binding.emojiGrid.setAdapter(this.adapter);
    }

    private void bindAction() {
        // 复制
        binding.copy.setOnClickListener(v -> {
            ((ChatActivity) context).copyMessage(message);
            dismiss();
        });

        // 转发
        binding.transmit.setOnClickListener(v -> {
            ((ChatActivity) context).transferMessage(message);
            dismiss();
        });

        // 回复
        binding.reply.setOnClickListener(v -> {
            ((ChatActivity) context).quoteReplyMessage(message);
            dismiss();
        });

        // 收藏
        binding.fav.setOnClickListener(v -> {
            ((ChatActivity) context).addFavoriteMessages(Arrays.asList(message.getId()));
            dismiss();
        });

        // TODO 稍后处理
        binding.dealLater.setOnClickListener(v -> todoToast());

        // 撤回
        binding.recall.setOnClickListener(v -> {
            ((ChatActivity) context).recallMessage(message);
            dismiss();
        });

        // 多选
        binding.multiSelect.setOnClickListener(v -> {
            ((ChatActivity) context).multiSelectMessage(message);
            dismiss();
        });

        // TODO 私发
        binding.privateSend.setOnClickListener(v -> todoToast());

        // TODO 置顶
        binding.setTop.setOnClickListener(v -> todoToast());

        // 删除
        binding.delete.setOnClickListener(v -> {
            ((ChatActivity) context).deleteMessage(message, position);
            dismiss();
        });
    }

    private void todoToast() {
        ToastUtil.makeToast((Activity) context, "敬请期待", 1000);
    }

}
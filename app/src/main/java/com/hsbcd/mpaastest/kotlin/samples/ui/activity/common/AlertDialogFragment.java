/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import org.apache.commons.lang3.StringUtils;

import cn.com.hsbc.hsbcchina.cert.databinding.DialogAlertBinding;

/**
 * 自定义样式的警告弹框
 *
 * @author liyalong
 * @version AlertDialogFragment.java, v 0.1 2022年09月21日 16:23 liyalong
 */
public class AlertDialogFragment extends AppCompatDialogFragment {

    private DialogAlertBinding binding;

    private String title;

    private String message;

    private OnClickConfirmListener listener;

    /**
     * 是否为通知模式
     * 通知模式下只展示单个确认按钮，且不需要绑定动作
     */
    private boolean notifyMode;

    public AlertDialogFragment() {
        super();
    }

    public AlertDialogFragment(String title, String message) {
        this(title, message, null);
    }

    public AlertDialogFragment(String title, String message, OnClickConfirmListener listener) {
        super();

        this.title = title;
        this.message = message;
        this.listener = listener;
        this.notifyMode = (listener == null);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 避免旋转屏幕时重新创建对话框实例，导致对话框内容丢失
        setRetainInstance(true);

        setStyle();

        binding = DialogAlertBinding.inflate(inflater);

        bindValue();
        bindAction();

        return binding.getRoot();
    }

    private void setStyle() {
        Window window = getDialog().getWindow();

        // 设置对话框原始背景为透明，便于渲染自定义背景
        window.setBackgroundDrawableResource(android.R.color.transparent);
    }

    private void bindValue() {
        binding.title.setText(title);

        if (StringUtils.isBlank(message)) {
            binding.message.setVisibility(View.GONE);
        } else {
            binding.message.setVisibility(View.VISIBLE);
            binding.message.setText(message);
        }

        binding.confirmButtonLayout.setVisibility(notifyMode ? View.GONE : View.VISIBLE);
        binding.notifyButtonLayout.setVisibility(notifyMode ? View.VISIBLE : View.GONE);
    }

    private void bindAction() {
        binding.confirmButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick();
            }

            dismiss();
        });

        binding.cancelButton.setOnClickListener(v -> dismiss());
        binding.notifyButton.setOnClickListener(v -> dismiss());
    }

    public interface OnClickConfirmListener {
        void onClick();
    }

}
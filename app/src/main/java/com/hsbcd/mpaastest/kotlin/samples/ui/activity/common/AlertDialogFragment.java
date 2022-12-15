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

import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import org.apache.commons.lang3.StringUtils;

import cn.hsbcsd.mpaastest.databinding.DialogAlertBinding;

/**
 * 自定义样式的警告弹框
 *
 * @author liyalong
 * @version AlertDialogFragment.java, v 0.1 2022年09月21日 16:23 liyalong
 */
public class AlertDialogFragment extends AppCompatDialogFragment {

    public static final String INPUT_TEXT = "inputText";

    private DialogAlertBinding binding;

    private String title;

    private String message;

    private OnClickConfirmListener listener;

    /**
     * 对话框模式
     */
    private DialogMode dialogMode;

    /**
     * 仅用于通知模式
     *
     * @param title
     * @param message
     */
    public AlertDialogFragment(String title, String message) {
        super();

        this.title = title;
        this.message = message;
        this.dialogMode = DialogMode.NOTIFY;
    }

    /**
     * 仅用于二次确认模式
     *
     * @param title
     * @param message
     * @param listener
     */
    public AlertDialogFragment(String title, String message, OnClickConfirmListener listener) {
        super();

        this.title = title;
        this.message = message;
        this.listener = listener;
        this.dialogMode = DialogMode.RE_CONFIRM;
    }

    /**
     * 可自由指定对话框模式，以及确认按钮的点击动作 (通知模式和输入模式下设置的点击动作无效)
     *
     * @param title
     * @param message
     * @param dialogMode
     * @param listener
     */
    public AlertDialogFragment(String title, String message, DialogMode dialogMode, OnClickConfirmListener listener) {
        super();

        this.title = title;
        this.message = message;
        this.listener = listener;
        this.dialogMode = dialogMode;
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

        // 提示信息为空则不展示
        if (StringUtils.isBlank(message)) {
            binding.message.setVisibility(View.GONE);
            binding.inputText.setVisibility(View.GONE);
        }
        // 输入模式下，展示文本输入框
        else if (dialogMode == DialogMode.INPUT) {
            binding.message.setVisibility(View.GONE);
            binding.inputText.setVisibility(View.VISIBLE);
            binding.inputText.setHint(message);
        }
        // 其他模式下，展示提示信息
        else {
            binding.message.setVisibility(View.VISIBLE);
            binding.inputText.setVisibility(View.GONE);
            binding.message.setText(message);
        }

        // 通知模式和带动作的通知模式下，展示单个确认按钮
        binding.notifyButtonLayout.setVisibility(
                (dialogMode == DialogMode.NOTIFY || dialogMode == DialogMode.NOTIFY_WITH_ACTION) ? View.VISIBLE : View.GONE);

        // 二次确认模式和输入模式下，展示取消和确认按钮
        binding.confirmButtonLayout.setVisibility(
                (dialogMode == DialogMode.RE_CONFIRM || dialogMode == DialogMode.INPUT) ? View.VISIBLE : View.GONE);
    }

    private void bindAction() {
        binding.cancelButton.setOnClickListener(v -> dismiss());

        // 确认按钮的点击动作
        binding.confirmButton.setOnClickListener(v -> {
            // 输入模式下，向宿主页面返回输入文本内容
            if (dialogMode == DialogMode.INPUT) {
                String inputText = binding.inputText.getText().toString();
                if (StringUtils.isBlank(inputText)) {
                    ToastUtil.makeToast(getActivity(), "Input can not be null", 1000);
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString(INPUT_TEXT, inputText);
                getParentFragmentManager().setFragmentResult(INPUT_TEXT, bundle);
            }
            // 二次确认模式下，触发点击事件
            else if (dialogMode == DialogMode.RE_CONFIRM && listener != null) {
                listener.onClick();
            }

            dismiss();
        });

        // 通知确认按钮的点击动作
        binding.notifyButton.setOnClickListener(v -> {
            // 带动作的通知模式下，触发点击事件
            if (dialogMode == DialogMode.NOTIFY_WITH_ACTION && listener != null) {
                listener.onClick();
            }

            dismiss();
        });
    }

    public interface OnClickConfirmListener {
        void onClick();
    }

    /**
     * 对话框模式
     */
    public enum DialogMode {
        /**
         * 通知模式，展示单个确认按钮，点击确认后仅关闭对话框，无其他动作，用于纯提示信息
         */
        NOTIFY,

        /**
         * 带动作的通知模式，展示单个确认按钮，需要设置确认按钮的点击动作，用于带后续动作的提示信息 (例如提示直播已结束，点击确认后返回上一页)
         */
        NOTIFY_WITH_ACTION,

        /**
         * 二次确认模式，展示取消和确认按钮，需要设置确认按钮的点击动作，用于高风险操作的二次确认
         */
        RE_CONFIRM,

        /**
         * 输入模式，展示文本输入框，以及取消和确认按钮，需要设置确认按钮的点击动作，用于输入文本
         */
        INPUT
    }

}
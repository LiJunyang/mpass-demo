/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.voice;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import cn.com.hsbc.hsbcchina.cert.R;
import cn.com.hsbc.hsbcchina.cert.databinding.DialogVoiceRecordingBinding;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 录音弹窗管理器
 *
 * @author liyalong
 * @version VideoPlayerActivity.java, v 0.1 2022年08月19日 15:48 liyalong
 */
public class VoiceDialogManager {

    private Context context;

    private Dialog dialog;

    private DialogVoiceRecordingBinding binding;

    public VoiceDialogManager(Context context) {
        this.context = context;
    }

    private void initDialog() {
        dialog = new Dialog(context);

        // 设置对话框原始背景为透明，便于渲染自定义背景
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        // 设置对话框弹出时，activity保持原有亮度
        dialog.getWindow().setDimAmount(0);

        binding = DialogVoiceRecordingBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());
    }

    public void showRecordingDialog() {
        initDialog();
        dialog.show();
    }

    public void showRecording() {
        if (dialog != null && dialog.isShowing()) {
            binding.background.setBackground(context.getDrawable(R.drawable.ic_recording));
            binding.desc.setText("Release to send, swipe up to cancel");
        }
    }

    public void showReadyToCancel() {
        if (dialog != null && dialog.isShowing()) {
            binding.background.setBackground(context.getDrawable(R.drawable.ic_recording_cancel));
            binding.desc.setText("Release finger, swipe up to cancel");
        }
    }

    public void showTooShort() {
        initDialog();

        binding.background.setBackground(context.getDrawable(R.drawable.ic_recording_too_short));
        binding.desc.setText("Too short");

        dialog.show();

        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                dismissDialog();
            }
        }, 1000);
    }

    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
    }
}
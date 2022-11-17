/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.voice;

import android.content.Context;
import android.media.MediaRecorder;

import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.constants.Constants;
import com.alipay.fc.ccmimplus.sdk.core.util.FileUtils;
import com.github.piasy.rxandroidaudio.AudioRecorder;

import java.io.File;

/**
 * 语音录制器
 *
 * @author maping.mp
 * @version 1.0: VoiceRecorder.java, v 0.1 2021年07月22日 5:22 下午 maping.mp Exp $
 */
public class VoiceRecorder {

    private Context context;

    private VoiceDialogManager dialogManager;

    private AudioRecorder audioRecorder;

    private File voiceFile;

    /**
     * 默认构造器
     *
     * @param context
     */
    public VoiceRecorder(Context context) {
        String storageBaseDir = AlipayCcmIMClient.getInstance().getInitConfig().getStorageDir();
        String voiceType = AlipayCcmIMClient.getInstance().getInitConfig().getVoiceOptions().getVoiceFileFormat();
        String voiceDir = storageBaseDir + Constants.STORAGE_VOICE;
        FileUtils.mkdirDirs(voiceDir);
        File tmpFile = new File(voiceDir + File.separator + System.nanoTime() + "." + voiceType);
        this.context = context;
        this.voiceFile = tmpFile;
        this.dialogManager = new VoiceDialogManager(context);
        this.audioRecorder = AudioRecorder.getInstance();
    }

    /**
     * 开始录制
     */
    public void startRecord() {
        dialogManager.showRecordingDialog();
        audioRecorder.prepareRecord(MediaRecorder.AudioSource.MIC, MediaRecorder.OutputFormat.MPEG_4,
                MediaRecorder.AudioEncoder.AAC, voiceFile);
        audioRecorder.startRecord();
    }

    /**
     * 结束录制
     */
    public void stopRecord() {
        audioRecorder.stopRecord();
        dialogManager.dismissDialog();
    }

    /**
     * 获取录制的文件
     *
     * @return
     */
    public File getVoiceFile() {
        return voiceFile;
    }

    public VoiceDialogManager getDialogManager() {
        return dialogManager;
    }
}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.chatroom;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceView;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushError;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushStatsInfo;
import com.alivc.live.pusher.AlivcLivePusher;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.model.LiveDataResult;

/**
 * 直播推流数据层
 * 参考阿里云直播sdk开发文档：https://help.aliyun.com/document_detail/94844.html
 *
 * @author liyalong
 * @version LivePushViewModel.java, v 0.1 2022年12月05日 10:58 liyalong
 */
public class LivePushViewModel extends ViewModel implements AlivcLivePushInfoListener, AlivcLivePushErrorListener {

    private AlivcLivePusher alivcLivePusher = new AlivcLivePusher();

    /**
     * 直播预览通知结果
     */
    private MutableLiveData<LiveDataResult> previewResult = new MutableLiveData<>();

    public LivePushViewModel() {
        // 直播sdk版本>=4.4.2时需要先注册sdk，验证license有效性
//        AlivcLiveBase.setListener(this);
//        AlivcLiveBase.registerSDK();
    }

    public void init(Context context) {
        AlivcLivePushConfig config = new AlivcLivePushConfig();
        alivcLivePusher.init(context, config);

        alivcLivePusher.setLivePushInfoListener(this);
        alivcLivePusher.setLivePushErrorListener(this);
    }

    public void startPreview(SurfaceView surfaceView) {
        alivcLivePusher.startPreview(surfaceView);
    }

    public void startPush(String pushUrl) {
        alivcLivePusher.startPush(pushUrl);
    }

    public void switchCamera() {
        alivcLivePusher.switchCamera();
    }

    public void destroy() {
        alivcLivePusher.stopPush();
        alivcLivePusher.stopPreview();
        alivcLivePusher.destroy();
    }

    // ~~~ AlivcLiveBaseListener start

//    @Override
//    public void onLicenceCheck(AlivcLivePushConstants.AlivcLiveLicenseCheckResultCode alivcLiveLicenseCheckResultCode,
//                               String s) {
//        Log.i(LoggerName.VIEW_MODEL, String.format("onLicenceCheck: %s / %s", alivcLiveLicenseCheckResultCode, s));
//
//        if (alivcLiveLicenseCheckResultCode == AlivcLivePushConstants.AlivcLiveLicenseCheckResultCode.AlivcLiveLicenseCheckResultCodeSuccess) {
//
//        } else {
//
//        }
//    }

    // ~~~ AlivcLiveBaseListener end

    // ~~~ AlivcLivePushInfoListener start

    @Override
    public void onPreviewStarted(AlivcLivePusher alivcLivePusher) {
        Log.i(LoggerName.VIEW_MODEL, "onPreviewStarted");

        LiveDataResult result = new LiveDataResult(true);
        previewResult.postValue(result);
    }

    @Override
    public void onPreviewStoped(AlivcLivePusher alivcLivePusher) {
        Log.i(LoggerName.VIEW_MODEL, "onPreviewStoped");
    }

    @Override
    public void onPushStarted(AlivcLivePusher alivcLivePusher) {
        Log.i(LoggerName.VIEW_MODEL, "onPushStarted");
    }

    @Override
    public void onFirstAVFramePushed(AlivcLivePusher alivcLivePusher) {
        Log.i(LoggerName.VIEW_MODEL, "onFirstAVFramePushed");
    }

    @Override
    public void onPushPauesed(AlivcLivePusher alivcLivePusher) {

    }

    @Override
    public void onPushResumed(AlivcLivePusher alivcLivePusher) {

    }

    @Override
    public void onPushStoped(AlivcLivePusher alivcLivePusher) {

    }

    @Override
    public void onPushRestarted(AlivcLivePusher alivcLivePusher) {

    }

    @Override
    public void onFirstFramePreviewed(AlivcLivePusher alivcLivePusher) {

    }

    @Override
    public void onDropFrame(AlivcLivePusher alivcLivePusher, int i, int i1) {

    }

    @Override
    public void onAdjustBitRate(AlivcLivePusher alivcLivePusher, int i, int i1) {

    }

    @Override
    public void onAdjustFps(AlivcLivePusher alivcLivePusher, int i, int i1) {

    }

    @Override
    public void onPushStatistics(AlivcLivePusher alivcLivePusher,
                                 AlivcLivePushStatsInfo alivcLivePushStatsInfo) {

    }

//    @Override
//    public void onSetLiveMixTranscodingConfig(AlivcLivePusher alivcLivePusher, boolean b, String s) {
//
//    }

    // ~~~ AlivcLivePushInfoListener end

    // ~~~ AlivcLivePushErrorListener start

    @Override
    public void onSystemError(AlivcLivePusher alivcLivePusher, AlivcLivePushError alivcLivePushError) {

    }

    @Override
    public void onSDKError(AlivcLivePusher alivcLivePusher, AlivcLivePushError alivcLivePushError) {

    }

    // ~~~ AlivcLivePushErrorListener end


    public MutableLiveData<LiveDataResult> getPreviewResult() {
        return previewResult;
    }

}
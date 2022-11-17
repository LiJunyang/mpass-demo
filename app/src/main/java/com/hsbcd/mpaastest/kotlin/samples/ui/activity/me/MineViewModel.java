/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.me;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSONObject;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.model.LiveDataResult;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.alipay.fc.ccmimplus.sdk.core.executor.AsyncExecutorService;
import com.alipay.fc.ccmimplus.sdk.core.user.UserManager;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.UserCacheManager;

/**
 * @author liyalong
 * @version MineViewModel.java, v 0.1 2022年08月01日 13:37 liyalong
 */
public class MineViewModel extends ViewModel {

    private MutableLiveData<UserInfoVO> mineUserInfo = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult> updateUserInfoResult = new MutableLiveData<>();

    private ImCallback<Boolean> updateCallback = new ImCallback<Boolean>() {
        @Override
        public void onSuccess(Boolean data) {
            LiveDataResult result = new LiveDataResult(data);
            updateUserInfoResult.postValue(result);
        }

        @Override
        public void onError(String errorCode, String message, Throwable t) {
            String msg = String.format("%s/%s/%s", errorCode, message, t.getMessage());
            LiveDataResult result = new LiveDataResult(false, msg);
            updateUserInfoResult.postValue(result);
        }
    };

    public void loadUserInfo() {
        AsyncExecutorService.getInstance().execute(() -> {
            UserCacheManager.getInstance().queryCurrentUser(true, new ImCallback<UserInfoVO>() {
                @Override
                public void onSuccess(UserInfoVO data) {
                    mineUserInfo.postValue(data);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {

                }
            });
        });
    }

    public void updateUserName(String userName) {
        AsyncExecutorService.getInstance().execute(() -> {
            JSONObject userInfo = new JSONObject();
            userInfo.put("userName", userName);
            userInfo.put("nickName", userName);
            UserManager.getInstance().updateUserInfo(userInfo, updateCallback);
        });
    }

    public void updateUserMark(String userMark) {
        AsyncExecutorService.getInstance().execute(() -> {
            UserManager.getInstance().updateUserMark(userMark, updateCallback);
        });
    }

    public MutableLiveData<UserInfoVO> getMineUserInfo() {
        return mineUserInfo;
    }

    public MutableLiveData<LiveDataResult> getUpdateUserInfoResult() {
        return updateUserInfoResult;
    }
}
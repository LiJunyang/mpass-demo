/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import android.os.Handler;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.UserCacheManager;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;

/**
 * @author liyalong
 * @version UserUtil.java, v 0.1 2022年11月09日 11:51 liyalong
 */
public class UserUtil {

    private static final Handler HANDLER = new Handler();

    public static String getCurrentUserName() {
        UserInfoVO currentUser = UserCacheManager.getInstance().getCurrentUserInfo();
        return StringUtils.defaultIfBlank(currentUser.getNickName(), currentUser.getUserName());
    }

    public static void getAndConsumeUserName(String userId, Consumer<String> consumer) {
        UserCacheManager.getInstance().queryUser(userId, false, new ImCallback<UserInfoVO>() {
            @Override
            public void onSuccess(UserInfoVO data) {
                String userName = StringUtils.defaultIfBlank(data.getNickName(), data.getUserName());

                if (ThreadUtil.isUiThread()) {
                    consumer.accept(userName);
                } else {
                    HANDLER.post(() -> consumer.accept(userName));
                }
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                if (ThreadUtil.isUiThread()) {
                    consumer.accept(userId);
                } else {
                    HANDLER.post(() -> consumer.accept(userId));
                }
            }
        });
    }

    public static void getAndConsumeUserInfo(String userId, Consumer<UserInfoVO> consumer) {
        UserCacheManager.getInstance().queryUser(userId, false, new ImCallback<UserInfoVO>() {
            @Override
            public void onSuccess(UserInfoVO data) {
                if (ThreadUtil.isUiThread()) {
                    consumer.accept(data);
                } else {
                    HANDLER.post(() -> consumer.accept(data));
                }
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                UserInfoVO userInfoVO = new UserInfoVO();
                userInfoVO.setUserId(userId);

                if (ThreadUtil.isUiThread()) {
                    consumer.accept(userInfoVO);
                } else {
                    HANDLER.post(() -> consumer.accept(userInfoVO));
                }
            }
        });
    }

}
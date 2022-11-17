/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alipay.fc.ccmimplus.common.service.facade.enums.UserSessionGroupTypeEnum;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserSessionGroupVO;
import com.hsbcd.mpaastest.kotlin.samples.model.LiveDataResult;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.alipay.fc.ccmimplus.sdk.core.conversation.ConversationGroupManager;
import com.alipay.fc.ccmimplus.sdk.core.executor.AsyncExecutorService;
import com.alipay.fc.ccmimplus.sdk.core.message.MessageManager;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;

/**
 * 聊天会话设置数据
 *
 * @author liyalong
 * @version ChatSettingViewModel.java, v 0.1 2022年08月10日 14:47 liyalong
 */
public class ChatSettingViewModel extends ViewModel {

    protected MutableLiveData<LiveDataResult> updateSettingResult = new MutableLiveData<>();

    protected void notifyUpdateSettingSuccess(boolean success) {
        LiveDataResult result = new LiveDataResult(success);
        updateSettingResult.postValue(result);
    }

    protected void notifyUpdateSettingFail(String errorCode, String message, Throwable t) {
        String msg = String.format("更新失败: %s/%s/%s", errorCode, message, t.getMessage());
        LiveDataResult result = new LiveDataResult(false, msg);
        updateSettingResult.postValue(result);
    }

    /**
     * 会话置顶
     *
     * @param c
     * @param checked
     */
    public void setToTop(Conversation c, boolean checked) {
        ConversationGroupManager cgm = AlipayCcmIMClient.getInstance().getConversationGroupManager();

        // 先查置顶分组信息
        getSessionGroup(UserSessionGroupTypeEnum.TOP, group -> {
            // 再把会话加入置顶分组
            AsyncExecutorService.getInstance().execute(() -> {
                cgm.setConversationTop(c.getCid(), group.getId(), checked, new ImCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        // 更新内存数据
                        c.setTopMode(checked);

                        notifyUpdateSettingSuccess(data);
                    }

                    @Override
                    public void onError(String errorCode, String message, Throwable t) {
                        notifyUpdateSettingFail(errorCode, message, t);
                    }
                });
            });
        });
    }

    /**
     * 会话设置为免打扰
     *
     * @param c
     * @param checked
     */
    public void setNoDisturb(Conversation c, boolean checked) {
        ConversationGroupManager cgm = AlipayCcmIMClient.getInstance().getConversationGroupManager();

        // 先查免打扰分组信息
        getSessionGroup(UserSessionGroupTypeEnum.SHIELD, (group) -> {
            // 再把会话加入免打扰分组
            AsyncExecutorService.getInstance().execute(() -> {
                cgm.setConversationNonDisturb(c.getCid(), group.getId(), checked, new ImCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        // 更新内存数据
                        c.setShieldMode(checked);

                        notifyUpdateSettingSuccess(data);
                    }

                    @Override
                    public void onError(String errorCode, String message, Throwable t) {
                        notifyUpdateSettingFail(errorCode, message, t);
                    }
                });
            });
        });
    }

    /**
     * 查询会话分组信息
     *
     * @param type
     * @param callback
     */
    private void getSessionGroup(UserSessionGroupTypeEnum type, UserSessionGroupCallback callback) {
        ConversationGroupManager cgm = AlipayCcmIMClient.getInstance().getConversationGroupManager();

        AsyncExecutorService.getInstance().execute(() -> {
            // 先查置顶分组信息
            cgm.getGroupByType(type.getValue(), new ImCallback<UserSessionGroupVO>() {
                @Override
                public void onSuccess(UserSessionGroupVO userSessionGroupVO) {
                    callback.onSuccess(userSessionGroupVO);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("查询会话分组失败: %s/%s/%s", errorCode, message, t.getMessage());
                    LiveDataResult result = new LiveDataResult(false, msg);
                    updateSettingResult.postValue(result);
                }
            });
        });
    }

    public void clearAllMessage(Conversation c) {
        AsyncExecutorService.getInstance().execute(() -> {
            MessageManager messageManager = MessageManager.getInstance();
            messageManager.clearMessage(c.getCid(), c.getLastMsgId(), new ImCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    LiveDataResult result = new LiveDataResult(data);
                    updateSettingResult.postValue(result);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("清空聊天记录失败: %s/%s/%s", errorCode, message, t.getMessage());
                    LiveDataResult result = new LiveDataResult(false, msg);
                    updateSettingResult.postValue(result);
                }
            });
        });
    }

    public MutableLiveData<LiveDataResult> getUpdateSettingResult() {
        return updateSettingResult;
    }

    private interface UserSessionGroupCallback {
        void onSuccess(UserSessionGroupVO group);
    }
}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSONObject;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.notify.NotifyDeleteConversation;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.notify.NotifyJoinChatroom;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.notify.NotifyLeaveChatroom;
import com.alipay.fc.ccmimplus.common.service.facade.enums.ChatroomUpdateTypeEnum;
import com.alipay.fc.ccmimplus.sdk.core.chatroom.ChatroomListener;
import com.alipay.fc.ccmimplus.sdk.core.chatroom.ChatroomManager;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.alipay.fc.ccmimplus.sdk.core.client.ImQueryCallback;
import com.alipay.fc.ccmimplus.sdk.core.conversation.ConversationManager;
import com.alipay.fc.ccmimplus.sdk.core.executor.AsyncExecutorService;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Chatroom;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.model.ChatroomNotifyResult;
import com.hsbcd.mpaastest.kotlin.samples.model.LiveDataResult;
import com.hsbcd.mpaastest.kotlin.samples.model.PageDataResult;



import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 聊天室数据模型
 *
 * @author liyalong
 * @version ChatroomViewModel.java, v 0.1 2022年11月08日 19:00 liyalong
 */
public class ChatroomViewModel extends ViewModel implements ChatroomListener {

    private static final int QUERY_PAGE_SIZE = 20;

    /**
     * 会话结果
     */
    private MutableLiveData<LiveDataResult<Conversation>> conversationResult = new MutableLiveData<>();

    /**
     * 会话列表结果
     */
    private MutableLiveData<PageDataResult<List<Conversation>>> conversationListResult = new MutableLiveData<>();

    /**
     * 聊天室在线通知结果
     */
    private MutableLiveData<ChatroomNotifyResult> chatroomNotifyResult = new MutableLiveData<>();

    /**
     * 聊天室设置更新结果
     */
    private MutableLiveData<LiveDataResult> updateSettingResult = new MutableLiveData<>();

    public void queryChatroom(String cid) {
        AsyncExecutorService.getInstance().execute(() -> {
            ChatroomManager cm = AlipayCcmIMClient.getInstance().getChatroomManager();
            cm.queryChatroom(cid, new ImCallback<Conversation>() {
                @Override
                public void onSuccess(Conversation data) {
                    LiveDataResult result = new LiveDataResult(true, data);
                    conversationResult.postValue(result);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("查询聊天室会话失败: %s/%s/%s", errorCode, message, t.getMessage());
                    LiveDataResult result = new LiveDataResult(false, msg);
                    conversationResult.postValue(result);
                }
            });
        });
    }

    public void queryAllChatroomList(int pageIndex) {
        AsyncExecutorService.getInstance().execute(() -> {
            ChatroomManager cm = AlipayCcmIMClient.getInstance().getChatroomManager();
            cm.queryAllChatroomList(pageIndex, QUERY_PAGE_SIZE, new QueryChatroomListCallback(pageIndex));
        });
    }

    public void queryRecentJoinedChatroomList(int pageIndex) {
        AsyncExecutorService.getInstance().execute(() -> {
            ChatroomManager cm = AlipayCcmIMClient.getInstance().getChatroomManager();
            cm.queryRecentJoinedChatroomList(pageIndex, QUERY_PAGE_SIZE, new QueryChatroomListCallback(pageIndex));
        });
    }

    public void queryAdminChatroomList(int pageIndex) {
        AsyncExecutorService.getInstance().execute(() -> {
            ChatroomManager cm = AlipayCcmIMClient.getInstance().getChatroomManager();
            cm.queryHasAdminAuthorityChatroomList(pageIndex, QUERY_PAGE_SIZE, new QueryChatroomListCallback(pageIndex));
        });
    }

    public void registerChatroomListener(String cid) {
        ChatroomManager cm = AlipayCcmIMClient.getInstance().getChatroomManager();
        cm.registerChatroomListener(cid, this);
    }

    public void unregisterChatroomListener(String cid, boolean unregisterAll) {
        ChatroomManager cm = AlipayCcmIMClient.getInstance().getChatroomManager();

        if (unregisterAll) {
            cm.unregisterChatroomListener(cid);
        } else {
            cm.unregisterChatroomListener(cid, this);
        }
    }

    public void joinChatroom(String cid) {
        AsyncExecutorService.getInstance().execute(() -> {
            ChatroomManager cm = AlipayCcmIMClient.getInstance().getChatroomManager();
            cm.joinChatroom(cid, StringUtils.EMPTY);
        });
    }

    public void leaveChatroom(String cid) {
        AsyncExecutorService.getInstance().execute(() -> {
            ChatroomManager cm = AlipayCcmIMClient.getInstance().getChatroomManager();
            cm.leaveChatroom(cid);
        });
    }

    public void createChatroom(String chatroomName) {
        AsyncExecutorService.getInstance().execute(() -> {
            ConversationManager cm = AlipayCcmIMClient.getInstance().getConversationManager();
            cm.createChatroomConversation(null, chatroomName, CommonConstants.DEFAULT_GROUP_LOGO, null,
                    new ImCallback<Conversation>() {
                        @Override
                        public void onSuccess(Conversation data) {
                            // 创建成功后，再查一遍会话
                            queryChatroom(data.getCid());
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            String msg = String.format("创建聊天室会话失败: %s/%s/%s", errorCode, message, t.getMessage());
                            LiveDataResult result = new LiveDataResult(false, msg);
                            conversationResult.postValue(result);
                        }
                    });
        });
    }

    public void muteAllMember(Conversation c, boolean muteAll) {
        Chatroom condition = new Chatroom();
        condition.setSilenceAll(muteAll);

        updateChatroom(c, ChatroomUpdateTypeEnum.SILENCE_ALL_FLAG, condition);
    }

    public void updateChatroom(Conversation c, ChatroomUpdateTypeEnum updateType, Chatroom condition) {
        AsyncExecutorService.getInstance().execute(() -> {
            ChatroomManager.getInstance().updateChatroom(c.getCid(), updateType, condition, new ImCallback<Chatroom>() {
                @Override
                public void onSuccess(Chatroom data) {
                    // 更新内存模型
                    switch (updateType) {
                        case SILENCE_ALL_FLAG: {
                            c.getChatroom().setSilenceAll(condition.isSilenceAll());
                            break;
                        }
                        default:
                            break;
                    }

                    LiveDataResult result = new LiveDataResult(true);
                    updateSettingResult.postValue(result);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("更新失败: %s/%s/%s", errorCode, message, t.getMessage());
                    LiveDataResult result = new LiveDataResult(false, msg);
                    updateSettingResult.postValue(result);
                }
            });
        });
    }

    public void updateLiveStreamInfo(Conversation c) {
        AsyncExecutorService.getInstance().execute(() -> {
            ChatroomManager.getInstance().updateLiveStreamInfo(c.getCid(), new ImCallback<Chatroom>() {
                @Override
                public void onSuccess(Chatroom data) {
                    // 更新内存模型
                    c.getChatroom().setExtInfo(data.getExtInfo());

                    LiveDataResult result = new LiveDataResult(true);
                    updateSettingResult.postValue(result);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("更新直播流信息失败: %s/%s/%s", errorCode, message, t.getMessage());
                    LiveDataResult result = new LiveDataResult(false, msg);
                    updateSettingResult.postValue(result);
                }
            });
        });
    }

    @Override
    public void onNotifyJoinChatroom(NotifyJoinChatroom notify) {
        Log.i(LoggerName.VIEW_MODEL, String.format("onNotifyJoinChatroom: %s", JSONObject.toJSONString(notify)));

        if (needPostNotify(notify.getCid())) {
            ChatroomNotifyResult result = new ChatroomNotifyResult(ChatroomNotifyResult.Operation.JOIN_CHATROOM,
                    notify.getJoinUserId());
            chatroomNotifyResult.postValue(result);
        }
    }

    @Override
    public void onNotifyLeaveChatroom(NotifyLeaveChatroom notify) {
        Log.i(LoggerName.VIEW_MODEL, String.format("onNotifyLeaveChatroom: %s", JSONObject.toJSONString(notify)));

        // 如果是自己离开聊天室，则注销监听器
        if (StringUtils.equals(notify.getLeaveUserId(), AlipayCcmIMClient.getInstance().getCurrentUserId())) {
            unregisterChatroomListener(notify.getCid(), true);
        }

        if (needPostNotify(notify.getCid())) {
            ChatroomNotifyResult result = new ChatroomNotifyResult(ChatroomNotifyResult.Operation.LEAVE_CHATROOM,
                    notify.getLeaveUserId());
            chatroomNotifyResult.postValue(result);
        }
    }

    @Override
    public void onNotifyDeleteChatroom(NotifyDeleteConversation notify) {
        Log.i(LoggerName.VIEW_MODEL, String.format("onNotifyDeleteChatroom: %s", JSONObject.toJSONString(notify)));

        // 注销监听器
        unregisterChatroomListener(notify.getCid(), true);

        if (needPostNotify(notify.getCid())) {
            ChatroomNotifyResult result = new ChatroomNotifyResult(ChatroomNotifyResult.Operation.DELETE_CHATROOM,
                    notify.getDeleteUserId());
            chatroomNotifyResult.postValue(result);
        }
    }

    private boolean needPostNotify(String cid) {
//        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
//
//        if (c == null) {
//            return false;
//        }
//
//        if (!StringUtils.equalsIgnoreCase(cid, c.getCid())) {
//            Log.i(LoggerName.VIEW_MODEL, String.format("忽略其他聊天室会话的通知: %s", cid));
//            return false;
//        }

        // TODO 暂时默认都投递通知
        return true;
    }

    public MutableLiveData<LiveDataResult<Conversation>> getConversationResult() {
        return conversationResult;
    }

    public MutableLiveData<PageDataResult<List<Conversation>>> getConversationListResult() {
        return conversationListResult;
    }

    public MutableLiveData<ChatroomNotifyResult> getChatroomNotifyResult() {
        return chatroomNotifyResult;
    }

    public MutableLiveData<LiveDataResult> getUpdateSettingResult() {
        return updateSettingResult;
    }

    private class QueryChatroomListCallback implements ImQueryCallback<List<Conversation>> {

        private int pageIndex;

        public QueryChatroomListCallback(int pageIndex) {
            this.pageIndex = pageIndex;
        }

        @Override
        public void onSuccess(List<Conversation> data) {
        }

        @Override
        public void onQueryResult(boolean hasNextPage, int nextPageIndex, List<Conversation> data) {
            Log.i(LoggerName.VIEW_MODEL, String.format("查询聊天室会话列表成功: %d/%d/%s/%d", pageIndex, data.size(), hasNextPage,
                    nextPageIndex));

            StringBuilder sb = new StringBuilder();
            data.forEach(c -> {
                String str = String.format("[%s,%s]", c.getCid(), c.getConversationName());
                sb.append(str).append("\n");
            });
            Log.d(LoggerName.VIEW_MODEL, sb.toString());

            PageDataResult result = new PageDataResult(true, data, hasNextPage);
            conversationListResult.postValue(result);
        }

        @Override
        public void onError(String errorCode, String message, Throwable t) {
            String msg = String.format("查询聊天室会话列表失败: %s/%s/%s", errorCode, message, t.getMessage());
            PageDataResult result = new PageDataResult(false, msg);
            conversationListResult.postValue(result);
        }
    }

}
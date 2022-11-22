/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alibaba.fastjson.JSONObject;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.enums.ChannelEnum;
import com.alipay.fc.ccmimplus.common.service.facade.model.User;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.enums.CustomMessageTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.model.LiveDataResult;
import com.hsbcd.mpaastest.kotlin.samples.model.PageDataResult;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.alipay.fc.ccmimplus.sdk.core.client.ImConversationQueryCallback;
import com.alipay.fc.ccmimplus.sdk.core.client.ImQueryCallback;
import com.alipay.fc.ccmimplus.sdk.core.constants.Constants;
import com.alipay.fc.ccmimplus.sdk.core.conversation.ConversationGroupManager;
import com.alipay.fc.ccmimplus.sdk.core.conversation.ConversationManager;
import com.alipay.fc.ccmimplus.sdk.core.executor.AsyncExecutorService;
import com.alipay.fc.ccmimplus.sdk.core.message.MessageManager;
import com.alipay.fc.ccmimplus.sdk.core.message.SendMessageCallback;
import com.alipay.fc.ccmimplus.sdk.core.message.content.CustomMessageContent;
import com.alipay.fc.ccmimplus.sdk.core.message.content.TextMessageContent;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.CreateGroupRequest;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.UserCacheManager;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liyalong
 * @version SessionViewModel.java, v 0.1 2022年08月01日 16:25 liyalong
 */
public class SessionViewModel extends ViewModel {

    private static final int QUERY_PAGE_SIZE = 20;

    /**
     * 会话结果
     */
    private MutableLiveData<LiveDataResult<Conversation>> conversationResult = new MutableLiveData<>();

    /**
     * 新的会话结果，用于收到新消息后展示在原有会话列表里不存在的新会话
     */
    private MutableLiveData<LiveDataResult<Conversation>> newConversationResult = new MutableLiveData<>();

    /**
     * 会话列表结果
     */
    private MutableLiveData<PageDataResult<List<Conversation>>> conversationListResult = new MutableLiveData<>();

    /**
     * 删除会话结果
     */
    private MutableLiveData<LiveDataResult> deleteConversationResult = new MutableLiveData<>();

    /**
     * 查询会话列表
     *
     * @param pageIndex
     */
    public void queryConversations(int pageIndex) {
        queryConversations(pageIndex, QUERY_PAGE_SIZE);
    }

    /**
     * 查询会话列表
     *
     * @param pageIndex
     * @param pageSize
     */
    public void queryConversations(int pageIndex, int pageSize) {
        Log.i(LoggerName.UI, String.format("start to refresh chat(%d)", pageIndex));

        ConversationManager cm = AlipayCcmIMClient.getInstance().getConversationManager();
        ConversationGroupManager cgm = AlipayCcmIMClient.getInstance().getConversationGroupManager();

        AsyncExecutorService.getInstance().execute(() -> {
            //cm.queryConversations(pageIndex, pageSize, new ImQueryCallback<List<Conversation>>() {
            cgm.queryAllUserConversations(pageIndex, pageSize, new ImQueryCallback<List<Conversation>>() {
                @Override
                public void onSuccess(List<Conversation> data) {

                }

                @Override
                public void onQueryResult(boolean hasNextPage, int nextPageIndex, List<Conversation> data) {
                    Log.i(LoggerName.UI,
                            String.format("search chat list success: %d/%d/%s/%d", pageIndex, data.size(), hasNextPage, nextPageIndex));

                    StringBuilder sb = new StringBuilder();
                    data.forEach(c -> {
                        String str = String.format("[%s,%s,%s,%s]", c.getCid(), c.isTopMode(), c.isShieldMode(),
                                c.getConversationName());
                        sb.append(str).append("\n");
                    });
                    Log.d(LoggerName.UI, sb.toString());

                    PageDataResult result = new PageDataResult(true, data, hasNextPage);
                    conversationListResult.postValue(result);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("search chat list failed: %s/%s/%s", errorCode, message, t.getMessage());
                    PageDataResult result = new PageDataResult(false, msg);
                    conversationListResult.postValue(result);
                }
            });
        });
    }

    /**
     * 查询密聊会话列表
     *
     * @param pageIndex
     */
    public void querySecretChatConversations(int pageIndex) {
        Log.i(LoggerName.UI, String.format("start to refresh new chat(%d)", pageIndex));

        ConversationManager cm = AlipayCcmIMClient.getInstance().getConversationManager();
        AsyncExecutorService.getInstance().execute(() -> {
            cm.querySecretChatConversations(pageIndex, QUERY_PAGE_SIZE, new ImQueryCallback<List<Conversation>>() {
                @Override
                public void onSuccess(List<Conversation> data) {
                }

                @Override
                public void onQueryResult(boolean hasNextPage, int nextPageIndex, List<Conversation> data) {
                    Log.i(LoggerName.UI,
                            String.format("search secret chat list success: %d/%d/%s/%d", pageIndex, data.size(), hasNextPage,
                                    nextPageIndex));

                    PageDataResult result = new PageDataResult(true, data, hasNextPage);
                    conversationListResult.postValue(result);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("search session list failed: %s/%s/%s", errorCode, message, t.getMessage());
                    PageDataResult result = new PageDataResult(false, msg);
                    conversationListResult.postValue(result);
                }
            });
        });
    }

    /**
     * 查询单个会话
     *
     * @param cid
     */
    public void querySingleConversation(String cid) {
        ConversationManager cm = AlipayCcmIMClient.getInstance().getConversationManager();
        cm.querySingleConversation(cid, new ImConversationQueryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation data) {
                LiveDataResult result = new LiveDataResult(true, data);
                conversationResult.postValue(result);
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                String msg = String.format("search 1to1 chat failed: %s/%s/%s", errorCode, message, t.getMessage());
                LiveDataResult result = new LiveDataResult(false, msg);
                conversationResult.postValue(result);
            }
        });
    }

    public void queryNewConversation(String cid) {
        ConversationManager cm = AlipayCcmIMClient.getInstance().getConversationManager();
        cm.querySingleConversation(cid, new ImConversationQueryCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation data) {
                LiveDataResult result = new LiveDataResult(true, data);
                newConversationResult.postValue(result);
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                String msg = String.format("search 1to1 chat failed: %s/%s/%s", errorCode, message, t.getMessage());
                LiveDataResult result = new LiveDataResult(false, msg);
                newConversationResult.postValue(result);
            }
        });
    }

    public void createSingleConversation(String targetUserId) {
        AsyncExecutorService.getInstance().execute(() -> {
            ConversationManager cm = AlipayCcmIMClient.getInstance().getConversationManager();
            cm.createSingleConversation(targetUserId, new ImCallback<Conversation>() {
                @Override
                public void onSuccess(Conversation data) {
                    sayHelloToUser(data);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("create 1to1 chat failed: %s/%s/%s", errorCode, message, t.getMessage());
                    LiveDataResult result = new LiveDataResult(false, msg);
                    conversationResult.postValue(result);
                }
            });
        });
    }

    private void sayHelloToUser(Conversation c) {
        MessageManager mm = MessageManager.getInstance();
        UserCacheManager cacheManager = UserCacheManager.getInstance();
        UserInfoVO currentUser = cacheManager.getCurrentUserInfo();
        String userNickName = StringUtils.defaultIfBlank(currentUser.getNickName(), currentUser.getUserName());
        String helloTxt = String.format("%s said hi", userNickName);
        TextMessageContent content = new TextMessageContent(helloTxt);
        content.setConversation(c);
        
        mm.sendSingleMessage(content, new SendMessageCallback<Message>() {
            @Override
            public void onSuccess(Message data) {
                querySingleConversation(c.getCid());
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                Log.e(Constants.LOG_CONVERSATION, "said hi but msg exception");
            }
        });
    }

    public void createSecretChatConversation(String targetUserId) {
        AsyncExecutorService.getInstance().execute(() -> {
            ConversationManager cm = AlipayCcmIMClient.getInstance().getConversationManager();
            cm.createSingleSecretConversation(targetUserId, new ImCallback<Conversation>() {
                @Override
                public void onSuccess(Conversation data) {
                    // 创建成功后，再查一遍
                    querySingleConversation(data.getCid());
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("create secret chat failed: %s/%s/%s", errorCode, message, t.getMessage());
                    LiveDataResult result = new LiveDataResult(false, msg);
                    conversationResult.postValue(result);
                }
            });
        });
    }

    public void createGroupConversation(Collection<UserInfoVO> groupUsers) {
        CreateGroupRequest request = buildCreateGroupRequest(groupUsers);

        ConversationManager cm = AlipayCcmIMClient.getInstance().getConversationManager();
        cm.createGroupConversation(request, new ImCallback<Conversation>() {
            @Override
            public void onSuccess(Conversation data) {
                sendHelloMsgToAllMembers(data);
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                String msg = String.format("create group chat failed: %s/%s/%s", errorCode, message, t.getMessage());
                LiveDataResult result = new LiveDataResult(false, msg);
                conversationResult.postValue(result);
            }
        });
    }

    private void sendHelloMsgToAllMembers(Conversation c) {
        MessageManager mm = MessageManager.getInstance();
        UserCacheManager cacheManager = UserCacheManager.getInstance();
        UserInfoVO currentUser = cacheManager.getCurrentUserInfo();
        String userNickName = StringUtils.defaultIfBlank(currentUser.getNickName(), currentUser.getUserName());
        String title = String.format("%s create the group", userNickName);

        JSONObject content = new JSONObject();
        content.put("title", title);
        CustomMessageContent notifyContent = new CustomMessageContent(CustomMessageTypeEnum.CREATE_GROUP_NOTIFY.name(),
                "1.0.0", content.toJSONString().getBytes());
        notifyContent.setConversation(c);

        mm.sendSingleMessage(notifyContent, new SendMessageCallback<Message>() {
            @Override
            public void beginSend(Message message) {
                message.getFrom().setUid(CommonConstants.SYS_USER_ID);
            }

            @Override
            public void onSuccess(Message data) {
                querySingleConversation(c.getCid());
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                Log.e(Constants.LOG_CONVERSATION, "msg exception after group created", t);
                querySingleConversation(c.getCid());
            }
        });
    }

    private CreateGroupRequest buildCreateGroupRequest(Collection<UserInfoVO> groupUsers) {
        CreateGroupRequest request = new CreateGroupRequest();

        // TODO 群成员头像拼接
        request.setLogoUrl(CommonConstants.DEFAULT_GROUP_LOGO);

        // 管理员列表(默认只有自己)
        String[] admins = new String[]{AlipayCcmIMClient.getInstance().getCurrentUserId()};
        request.setAdminUsers(admins);

        // 群成员列表(注：需要包括自己)
        List<User> memberUsers = new ArrayList<>(groupUsers.size());
        UserInfoVO currentUserInfo = UserCacheManager.getInstance().getCurrentUserInfo();
        memberUsers.add(convertUser(currentUserInfo));
        for (UserInfoVO userInfoVO : groupUsers) {
            memberUsers.add(convertUser(userInfoVO));
        }
        request.setMembers(memberUsers.toArray(new User[]{}));

        // 群名：由群成员名称拼接，最多拼接4个名称
        List<String> memberUserNames = Lists.newArrayList();
        List<User> subMemberUsers = CollectionUtils.subList(memberUsers, 0, 4);
        for (User user : subMemberUsers) {
            memberUserNames.add(user.getUserName());
        }
        String groupName = StringUtils.join(memberUserNames.toArray(new String[]{}),
                CommonConstants.COMMA);
        request.setName(groupName);

        // ext info
        Map<String, String> extInfo = new HashMap<>();
        //extInfo.put("type", "ok");
        request.setExtInfo(extInfo);

        return request;
    }

    private User convertUser(UserInfoVO userInfoVO) {
        User user = new User(ChannelEnum.IMPLUS, userInfoVO.getUserId(), userInfoVO.getNickName());
        user.setUserName(userInfoVO.getUserName());
        return user;
    }

    public void deleteConversation(String cid) {
        ConversationManager cm = AlipayCcmIMClient.getInstance().getConversationManager();
        cm.deleteConversation(cid, new ImCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                LiveDataResult result = new LiveDataResult(true, data);
                deleteConversationResult.postValue(result);
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                String msg = String.format("del session failed: %s/%s/%s", errorCode, message, t.getMessage());
                LiveDataResult result = new LiveDataResult(false, msg);
                deleteConversationResult.postValue(result);
            }
        });
    }

    public MutableLiveData<LiveDataResult<Conversation>> getConversationResult() {
        return conversationResult;
    }

    public MutableLiveData<LiveDataResult<Conversation>> getNewConversationResult() {
        return newConversationResult;
    }

    public MutableLiveData<PageDataResult<List<Conversation>>> getConversationListResult() {
        return conversationListResult;
    }

    public MutableLiveData<LiveDataResult> getDeleteConversationResult() {
        return deleteConversationResult;
    }
}
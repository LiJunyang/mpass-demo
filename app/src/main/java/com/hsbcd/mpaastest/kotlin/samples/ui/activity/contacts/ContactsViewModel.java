/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.GoodFriendVO;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.model.LiveDataResult;
import com.hsbcd.mpaastest.kotlin.samples.model.PageDataResult;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.alipay.fc.ccmimplus.sdk.core.client.ImQueryCallback;
import com.alipay.fc.ccmimplus.sdk.core.executor.AsyncExecutorService;
import com.alipay.fc.ccmimplus.sdk.core.group.GroupManager;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Group;
import com.alipay.fc.ccmimplus.sdk.core.user.UserManager;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author liyalong
 * @version MessageViewModel.java, v 0.1 2022年08月01日 16:25 liyalong
 */
public class ContactsViewModel extends ViewModel {

    private static final int QUERY_PAGE_SIZE = 20;

    private MutableLiveData<PageDataResult<List<UserInfoVO>>> userInfoListResult = new MutableLiveData<>();

    private MutableLiveData<PageDataResult<List<Group>>> groupListResult = new MutableLiveData<>();

    private MutableLiveData<PageDataResult<List<GoodFriendVO>>> friendListResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult<UserInfoVO>> userInfoResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult> addFriendResult = new MutableLiveData<>();

    /**
     * 查询新的朋友
     */
    public void queryNewFriends(int pageIndex) {
        AsyncExecutorService.getInstance().execute(() -> {
            UserManager userManager = AlipayCcmIMClient.getInstance().getUserManager();
            userManager.queryNewGoodFriends(pageIndex, QUERY_PAGE_SIZE, new ImQueryCallback<List<GoodFriendVO>>() {
                @Override
                public void onQueryResult(boolean hasNextPage, int nextPageIndex, List<GoodFriendVO> data) {
                    PageDataResult result = new PageDataResult(true, data, hasNextPage);
                    friendListResult.postValue(result);
                }

                @Override
                public void onSuccess(List<GoodFriendVO> data) {
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("查询新的朋友列表失败: %s/%s/%s", errorCode, message, t.getMessage());
                    PageDataResult result = new PageDataResult(false, msg);
                    friendListResult.postValue(result);
                }
            });
        });
    }

    /**
     * 查询我的好友
     */
    public void queryMyFriends(int pageIndex) {
        AsyncExecutorService.getInstance().execute(() -> {
            UserManager userManager = AlipayCcmIMClient.getInstance().getUserManager();
            userManager.queryMyGoodFriends(pageIndex, QUERY_PAGE_SIZE, new ImQueryCallback<List<UserInfoVO>>() {
                @Override
                public void onQueryResult(boolean hasNextPage, int nextPageIndex, List<UserInfoVO> data) {
                    PageDataResult result = new PageDataResult(true, data, hasNextPage);
                    userInfoListResult.postValue(result);
                }

                @Override
                public void onSuccess(List<UserInfoVO> data) {
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("查询我的好友列表失败: %s/%s/%s", errorCode, message, t.getMessage());
                    PageDataResult result = new PageDataResult(false, msg);
                    userInfoListResult.postValue(result);
                }
            });
        });
    }

    /**
     * 查询我的群组
     */
    public void queryMyGroups(int pageIndex) {
        AsyncExecutorService.getInstance().execute(() -> {
            GroupManager groupManager = AlipayCcmIMClient.getInstance().getGroupManager();
            groupManager.queryMyGroups(pageIndex, QUERY_PAGE_SIZE, new ImQueryCallback<List<Conversation>>() {
                @Override
                public void onQueryResult(boolean hasNextPage, int nextPageIndex, List<Conversation> data) {
                    List<Group> groups = new ArrayList<>(data.size());
                    for (Conversation groupSession : data) {
                        groups.add(groupSession.getGroup());
                    }

                    PageDataResult result = new PageDataResult(true, groups, hasNextPage);
                    groupListResult.postValue(result);
                }

                @Override
                public void onSuccess(List<Conversation> data) {
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("查询我的群组失败: %s/%s/%s", errorCode, message, t.getMessage());
                    PageDataResult result = new PageDataResult(false, msg);
                    groupListResult.postValue(result);
                }
            });
        });
    }

    /**
     * 查询租户内的全部联系人
     */
    public void queryAllContacts(int pageIndex) {
        AsyncExecutorService.getInstance().execute(() -> {
            UserManager userManager = AlipayCcmIMClient.getInstance().getUserManager();
            userManager.pageQueryUsers(pageIndex, QUERY_PAGE_SIZE, new ImQueryCallback<List<UserInfoVO>>() {
                @Override
                public void onQueryResult(boolean hasNextPage, int nextPageIndex, List<UserInfoVO> data) {
                    PageDataResult result = new PageDataResult(true, data, hasNextPage);
                    userInfoListResult.postValue(result);
                }

                @Override
                public void onSuccess(List<UserInfoVO> data) {
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("查询全部联系人失败: %s/%s/%s", errorCode, message, t.getMessage());
                    PageDataResult result = new PageDataResult(false, msg);
                    userInfoListResult.postValue(result);
                }
            });
        });
    }

    /**
     * 查询常用联系人
     */
    public void queryCommonlyUsedContacts() {
        AsyncExecutorService.getInstance().execute(() -> {
            UserManager userManager = AlipayCcmIMClient.getInstance().getUserManager();
            userManager.queryCommonlyUsedContact(new ImCallback<List<UserInfoVO>>() {
                @Override
                public void onSuccess(List<UserInfoVO> data) {
                    PageDataResult result = new PageDataResult(true, data, false);
                    userInfoListResult.postValue(result);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("查询常用联系人列表失败: %s/%s/%s", errorCode, message, t.getMessage());
                    PageDataResult result = new PageDataResult(false, msg);
                    userInfoListResult.postValue(result);
                }
            });
        });
    }

    /**
     * 查询用户详情
     */
    public void queryUserInfo(String userId) {
        AsyncExecutorService.getInstance().execute(() -> {
            UserManager userManager = UserManager.getInstance();
            userManager.querySingleUser(userId, new ImCallback<UserInfoVO>() {
                @Override
                public void onSuccess(UserInfoVO data) {
                    LiveDataResult result = new LiveDataResult(true, data);
                    userInfoResult.postValue(result);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("查询用户详情失败: %s/%s/%s", errorCode, message, t.getMessage());
                    LiveDataResult result = new LiveDataResult(false, msg);
                    userInfoResult.postValue(result);
                }
            });
        });
    }

    public void addFriend(String userId) {
        Map<String, String> extInfo = new HashMap<>();
        extInfo.put("inviteUserId", AlipayCcmIMClient.getInstance().getCurrentUserId());

        AsyncExecutorService.getInstance().execute(() -> {
            UserManager userManager = AlipayCcmIMClient.getInstance().getUserManager();
            userManager.addGoodFriend(userId, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, extInfo,
                    new ImCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean data) {
                            String msg = "发送好友邀请" + (data ? "成功" : "失败");
                            LiveDataResult result = new LiveDataResult(data, msg);
                            addFriendResult.postValue(result);
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            String msg = String.format("发送好友邀请失败: %s/%s/%s", errorCode, message, t.getMessage());
                            LiveDataResult result = new LiveDataResult(false, msg);
                            addFriendResult.postValue(result);
                        }
                    });
        });
    }

    public void replyAddFriend(String inviteBizNo, String inviteUserId, boolean agree) {
        UserManager userManager = AlipayCcmIMClient.getInstance().getUserManager();
        userManager.replyAddGoodFriend(inviteBizNo, inviteUserId, agree, new ImCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                String msg = "回复好友邀请" + (data ? "成功" : "失败");
                LiveDataResult result = new LiveDataResult(data, msg);
                addFriendResult.postValue(result);
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                String msg = String.format("回复好友邀请失败: %s/%s/%s", errorCode, message, t.getMessage());
                LiveDataResult result = new LiveDataResult(false, msg);
                addFriendResult.postValue(result);
            }
        });
    }

    public MutableLiveData<PageDataResult<List<UserInfoVO>>> getUserInfoListResult() {
        return userInfoListResult;
    }

    public MutableLiveData<PageDataResult<List<Group>>> getGroupListResult() {
        return groupListResult;
    }

    public MutableLiveData<PageDataResult<List<GoodFriendVO>>> getFriendListResult() {
        return friendListResult;
    }

    public MutableLiveData<LiveDataResult<UserInfoVO>> getUserInfoResult() {
        return userInfoResult;
    }

    public MutableLiveData<LiveDataResult> getAddFriendResult() {
        return addFriendResult;
    }
}
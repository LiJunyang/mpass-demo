/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.user;

import android.util.Log;

import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserCustomSettingVO;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.alipay.fc.ccmimplus.sdk.core.client.ImQueryCallback;
import com.alipay.fc.ccmimplus.sdk.core.executor.AsyncExecutorService;
import com.alipay.fc.ccmimplus.sdk.core.group.GroupManager;
import com.alipay.fc.ccmimplus.sdk.core.group.GroupMemberQueryRequest;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.GroupRelation;
import com.alipay.fc.ccmimplus.sdk.core.user.UserManager;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.MapUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户缓存管理器
 *
 * @author maping.mp
 * @version 1.0: UserCacheManager.java, v 0.1 2022年02月23日 5:50 下午 maping.mp Exp $
 */
public class UserCacheManager {

    private static final UserCacheManager INSTANCE = new UserCacheManager();

    /**
     * 当前登录用户自定义配置
     */
    private UserCustomSettingVO customSettingVO;

    /**
     * 用户信息的本地缓存
     */
    private Cache<String, UserInfoVO> userInfoCache = CacheBuilder.newBuilder().expireAfterWrite(1,
            TimeUnit.HOURS).build();

    /**
     * 群成员信息的本地缓存
     */
    private Cache<String, GroupRelation> groupRelationCache = CacheBuilder.newBuilder().expireAfterWrite(1,
            TimeUnit.HOURS).build();

    public static UserCacheManager getInstance() {
        return INSTANCE;
    }

    /**
     * 获取当前登录的用户
     *
     * @return
     */
    public UserInfoVO getCurrentUserInfo() {
        String userId = AlipayCcmIMClient.getInstance().getCurrentUserId();
        return syncGetUserInfo(userId);
    }

    public UserInfoVO syncGetUserInfo(String userId) {
        // 直接从缓存取，同步返回
        return userInfoCache.getIfPresent(userId);
    }

    public GroupRelation syncGetGroupMember(String cid, String userId) {
        // 直接从缓存取，同步返回
        String cacheKey = buildGroupRelationCacheKey(cid, userId);
        return groupRelationCache.getIfPresent(cacheKey);
    }

    /**
     * 查询我的自定义配置
     *
     * @param callback
     */
    public void queryCustomSetting(ImCallback<UserCustomSettingVO> callback) {
        synchronized (this) {
            if (customSettingVO != null && MapUtils.isNotEmpty(customSettingVO.getCustomConfig())) {
                callback.onSuccess(customSettingVO);
            } else {
                AsyncExecutorService.getInstance().execute(() -> {
                    UserManager userManager = UserManager.getInstance();
                    userManager.queryMyUserCustomSetting(new ImCallback<UserCustomSettingVO>() {
                        @Override
                        public void onSuccess(UserCustomSettingVO data) {
                            customSettingVO = data;
                            callback.onSuccess(data);
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            callback.onError(errorCode, message, t);
                        }
                    });
                });
            }
        }
    }

    public void resetUserCustomSetting() {
        synchronized (this) {
            this.customSettingVO = null;
        }
    }

    /**
     * 查询当前登录用户
     */
    public void queryCurrentUser(boolean forceUpdate, ImCallback<UserInfoVO> callback) {
        String userId = AlipayCcmIMClient.getInstance().getCurrentUserId();
        queryUser(userId, forceUpdate, callback);
    }

    /**
     * 查询单个用户
     */
    public void queryUser(String userId, boolean forceUpdate, ImCallback<UserInfoVO> callback) {
        // 如果非强制更新，则先查本地缓存，否则直接调服务端获取最新数据，并更新本地缓存
        if (!forceUpdate) {
            UserInfoVO user = userInfoCache.getIfPresent(userId);
            if (user != null) {
                callback.onSuccess(user);
                return;
            }
        }

        AsyncExecutorService.getInstance().execute(() -> {
            UserManager.getInstance().querySingleUser(userId, new ImCallback<UserInfoVO>() {
                @Override
                public void onSuccess(UserInfoVO data) {
                    userInfoCache.put(userId, data);
                    callback.onSuccess(data);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    Log.i("ui", "查询当前登录用户信息异常");
                    callback.onError(errorCode, message, t);
                }
            });
        });
    }

    /**
     * 批量查询多个用户
     */
    public void batchQueryUsers(List<String> userIds, ImCallback<List<UserInfoVO>> callback) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        List<String> notExistsUsers = new ArrayList<>();
        List<UserInfoVO> users = new ArrayList<>();
        for (String userId : userIds) {
            UserInfoVO user = userInfoCache.getIfPresent(userId);
            if (user != null) {
                users.add(user);
            } else {
                notExistsUsers.add(userId);
            }
        }

        if (CollectionUtils.isEmpty(notExistsUsers)) {
            callback.onSuccess(users);
            return;
        }

        AsyncExecutorService.getInstance().execute(() -> {
            UserManager userManager = UserManager.getInstance();
            userManager.batchQueryUsers(notExistsUsers, new ImCallback<List<UserInfoVO>>() {
                @Override
                public void onSuccess(List<UserInfoVO> userList) {
                    if (CollectionUtils.isNotEmpty(userList)) {
                        users.addAll(userList);
                        for (UserInfoVO userInfoVO : userList) {
                            userInfoCache.put(userInfoVO.getUserId(), userInfoVO);
                        }
                    }
                    callback.onSuccess(users);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    callback.onError(errorCode, message, t);
                }
            });
        });
    }

    public void batchQueryGroupMembers(String cid, List<String> userIds, ImCallback<List<GroupRelation>> callback) {
        if (StringUtils.isBlank(cid) || CollectionUtils.isEmpty(userIds)) {
            return;
        }

        List<String> notCachedMembers = new ArrayList<>();
        List<GroupRelation> members = new ArrayList<>();

        for (String userId : userIds) {
            String cacheKey = buildGroupRelationCacheKey(cid, userId);
            GroupRelation member = groupRelationCache.getIfPresent(cacheKey);

            if (member != null) {
                members.add(member);
            } else {
                notCachedMembers.add(userId);
            }
        }

        if (CollectionUtils.isEmpty(notCachedMembers)) {
            callback.onSuccess(members);
            return;
        }

        GroupMemberQueryRequest request = new GroupMemberQueryRequest();
        request.setCid(cid);
        request.setUserIds(userIds);

        AsyncExecutorService.getInstance().execute(() -> {
            GroupManager.getInstance().queryGroupMembers(request, new ImQueryCallback<List<GroupRelation>>() {
                @Override
                public void onQueryResult(boolean hasNextPage, int nextPageIndex, List<GroupRelation> data) {
                    if (CollectionUtils.isNotEmpty(data)) {
                        members.addAll(data);

                        data.forEach(m -> {
                            String cacheKey = buildGroupRelationCacheKey(cid, m.getUserId());
                            groupRelationCache.put(cacheKey, m);
                        });
                    }

                    callback.onSuccess(members);
                }

                @Override
                public void onSuccess(List<GroupRelation> data) {
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    callback.onError(errorCode, message, t);
                }
            });
        });
    }

    private String buildGroupRelationCacheKey(String cid, String userId) {
        return String.format("%s|%s", cid, userId);
    }

}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting;

import androidx.lifecycle.MutableLiveData;

import com.alipay.fc.ccmimplus.common.service.facade.enums.GroupJoinApplySourceEnum;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.GroupRelationVO;
import com.hsbcd.mpaastest.kotlin.samples.model.LiveDataResult;
import com.hsbcd.mpaastest.kotlin.samples.model.PageDataResult;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.alipay.fc.ccmimplus.sdk.core.client.ImQueryCallback;
import com.alipay.fc.ccmimplus.sdk.core.enums.GroupMemberRoleEnum;
import com.alipay.fc.ccmimplus.sdk.core.executor.AsyncExecutorService;
import com.alipay.fc.ccmimplus.sdk.core.group.GroupManager;
import com.alipay.fc.ccmimplus.sdk.core.group.GroupMemberQueryRequest;
import com.alipay.fc.ccmimplus.sdk.core.group.GroupSetting;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Group;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.GroupJoinApply;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.GroupRelation;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author liyalong
 * @version GroupChatSettingViewModel.java, v 0.1 2022年08月10日 21:31 liyalong
 */
public class GroupChatSettingViewModel extends ChatSettingViewModel {

    private static final int QUERY_PAGE_SIZE = 100;

    private MutableLiveData<PageDataResult<List<GroupRelation>>> groupRelationListResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult> leaveGroupResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult> dismissGroupResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult> transferOwnerResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult<GroupSetting>> groupSettingResult = new MutableLiveData<>();

    /**
     * 查询群成员列表
     *
     * @param c
     * @param pageIndex
     */
    public void queryGroupMembers(Conversation c, int pageIndex) {
        queryGroupMembers(c, pageIndex, QUERY_PAGE_SIZE);
    }

    /**
     * 查询群成员列表
     *
     * @param c
     * @param pageIndex
     * @param pageSize
     */
    public void queryGroupMembers(Conversation c, int pageIndex, int pageSize) {
        GroupMemberQueryRequest request = new GroupMemberQueryRequest();
        request.setCid(c.getCid());
        request.setPageIndex(pageIndex);
        request.setPageSize(pageSize);

        queryGroupMembers(request);
    }

    /**
     * 查询群成员列表
     *
     * @param request
     */
    public void queryGroupMembers(GroupMemberQueryRequest request) {
        AsyncExecutorService.getInstance().execute(() -> {
            GroupManager gm = AlipayCcmIMClient.getInstance().getGroupManager();
            gm.queryGroupMembers(request, new ImQueryCallback<List<GroupRelation>>() {
                @Override
                public void onQueryResult(boolean hasNextPage, int nextPageIndex, List<GroupRelation> data) {
                    PageDataResult<List<GroupRelation>> result = new PageDataResult(true, data, hasNextPage);
                    groupRelationListResult.postValue(result);
                }

                @Override
                public void onSuccess(List<GroupRelation> data) {
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("Search group member list failed: %s/%s/%s", errorCode, message, t.getMessage());
                    PageDataResult<List<GroupRelation>> result = new PageDataResult(false, msg);
                    groupRelationListResult.postValue(result);
                }
            });
        });
    }

    /**
     * 查询禁言成员列表
     *
     * @param c
     */
    public void queryGroupMuteMembers(Conversation c, int pageIndex) {
        GroupMemberQueryRequest request = new GroupMemberQueryRequest();
        request.setCid(c.getCid());
        request.setPageIndex(pageIndex);
        request.setPageSize(QUERY_PAGE_SIZE);
        request.setQueryOnlySilence(true);

        queryGroupMembers(request);
    }

    /**
     * 查询未禁言成员列表
     *
     * @param c
     */
    public void queryGroupUnMuteMembers(Conversation c, int pageIndex) {
        GroupMemberQueryRequest request = new GroupMemberQueryRequest();
        request.setCid(c.getCid());
        request.setPageIndex(pageIndex);
        request.setPageSize(QUERY_PAGE_SIZE);
        request.setQueryOnlyNormal(true);

        queryGroupMembers(request);
    }

    /**
     * 更新用户在群里的昵称
     *
     * @param c
     * @param nickName
     */
    public void updateGroupNickName(Conversation c, String nickName) {
        AsyncExecutorService.getInstance().execute(() -> {
            GroupManager.getInstance().updateUserNickName(c.getCid(), nickName, new ImCallback<GroupRelationVO>() {
                @Override
                public void onSuccess(GroupRelationVO data) {
                    // 更新内存数据
                    c.getGroup().getRelationOfMe().setUserNick(nickName);

                    notifyUpdateSettingSuccess(true);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    notifyUpdateSettingFail(errorCode, message, t);
                }
            });
        });
    }

    /**
     * 更新用户对群的备注
     *
     * @param c
     * @param memo
     */
    public void updateGroupMemo(Conversation c, String memo) {
        AsyncExecutorService.getInstance().execute(() -> {
            GroupManager.getInstance().updateUserRemark(c.getCid(), memo, new ImCallback<GroupRelationVO>() {
                @Override
                public void onSuccess(GroupRelationVO data) {
                    // 更新内存数据
                    c.getGroup().getRelationOfMe().setUserMark(memo);

                    notifyUpdateSettingSuccess(true);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    notifyUpdateSettingFail(errorCode, message, t);
                }
            });
        });
    }

    /**
     * 更新群名称
     *
     * @param c
     * @param groupName
     */
    public void updateGroupName(Conversation c, String groupName) {
        AsyncExecutorService.getInstance().execute(() -> {
            GroupManager.getInstance().updateGroupName(c.getCid(), groupName, new ImCallback<Group>() {
                @Override
                public void onSuccess(Group data) {
                    // 更新内存数据
                    c.setSessionName(groupName);
                    c.getGroup().setName(groupName);

                    notifyUpdateSettingSuccess(true);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    notifyUpdateSettingFail(errorCode, message, t);
                }
            });
        });
    }

    /**
     * 更新群介绍
     *
     * @param c
     * @param desc
     */
    public void updateGroupDesc(Conversation c, String desc) {
        AsyncExecutorService.getInstance().execute(() -> {
            GroupManager.getInstance().updateGroupRemark(c.getCid(), desc, new ImCallback<Group>() {
                @Override
                public void onSuccess(Group data) {
                    // 更新内存数据
                    c.getGroup().setRemark(desc);

                    notifyUpdateSettingSuccess(true);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    notifyUpdateSettingFail(errorCode, message, t);
                }
            });
        });
    }

    /**
     * 更新群公告
     *
     * @param c
     * @param notice
     */
    public void updateGroupNotice(Conversation c, String notice) {
        AsyncExecutorService.getInstance().execute(() -> {
            GroupManager.getInstance().updateGroupNotice(c.getCid(), notice, new ImCallback<Group>() {
                @Override
                public void onSuccess(Group data) {
                    // 更新内存数据
                    c.getGroup().setNotice(notice);

                    notifyUpdateSettingSuccess(true);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    notifyUpdateSettingFail(errorCode, message, t);
                }
            });
        });
    }

    /**
     * 把指定用户加入群聊
     *
     * @param c
     * @param userIds
     */
    public void addGroupMember(Conversation c, List<String> userIds) {
        String currentUserId = AlipayCcmIMClient.getInstance().getCurrentUserId();
        GroupManager.getInstance().addGroupMember(c.getCid(), currentUserId, GroupJoinApplySourceEnum.INVITED, true,
                userIds.toArray(new String[]{}),
                new ImCallback<Pair<List<GroupRelation>, Map<String, List<GroupJoinApply>>>>() {
                    @Override
                    public void onSuccess(Pair<List<GroupRelation>, Map<String, List<GroupJoinApply>>> data) {
                        // 更新内存数据-群成员数量
                        // 添加群成员的回调数据包括两部分：直接加入成功的，以及等待入群审批的，这里只统计前者
                        int count = data.getLeft().size();
                        c.getGroup().setMemberCount(c.getGroup().getMemberCount() + count);

                        notifyUpdateSettingSuccess(true);
                    }

                    @Override
                    public void onError(String errorCode, String message, Throwable t) {
                        notifyUpdateSettingFail(errorCode, message, t);
                    }
                });
    }

    /**
     * 退出群聊
     *
     * @param c
     */
    public void leaveGroup(Conversation c) {
        String currentUserId = AlipayCcmIMClient.getInstance().getCurrentUserId();
        removeGroupMember(c, Arrays.asList(currentUserId));
    }

    /**
     * 把指定用户移出群聊
     *
     * @param c
     * @param userIds
     */
    public void removeGroupMember(Conversation c, List<String> userIds) {
        String currentUserId = AlipayCcmIMClient.getInstance().getCurrentUserId();
        boolean removeSelf = userIds.contains(currentUserId);

        AsyncExecutorService.getInstance().execute(() -> {
            GroupManager.getInstance().removeGroupMember(c.getCid(), userIds.toArray(new String[]{}),
                    new ImCallback<List<GroupRelation>>() {
                        @Override
                        public void onSuccess(List<GroupRelation> data) {
                            // 更新内存数据-群成员数量
                            int count = data.size();
                            c.getGroup().setMemberCount(c.getGroup().getMemberCount() - count);

                            LiveDataResult result = new LiveDataResult(true);
                            if (removeSelf) {
                                leaveGroupResult.postValue(result);
                            } else {
                                updateSettingResult.postValue(result);
                            }
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            String msg = String.format("Remove group member failed: %s/%s/%s", errorCode, message, t.getMessage());
                            LiveDataResult result = new LiveDataResult(false, msg);
                            if (removeSelf) {
                                leaveGroupResult.postValue(result);
                            } else {
                                updateSettingResult.postValue(result);
                            }
                        }
                    });
        });
    }

    /**
     * 解散群聊
     */
    public void dismissGroup(Conversation c) {
        AsyncExecutorService.getInstance().execute(() -> {
            GroupManager.getInstance().dismissGroup(c.getCid(), new ImCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    LiveDataResult result = new LiveDataResult(data);
                    dismissGroupResult.postValue(result);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("Destroy Group Failed: %s/%s/%s", errorCode, message, t.getMessage());
                    LiveDataResult result = new LiveDataResult(false, msg);
                    dismissGroupResult.postValue(result);
                }
            });
        });
    }

    /**
     * 转让群主
     *
     * @param c
     * @param newOwnerUserId
     */
    public void transferOwner(Conversation c, String newOwnerUserId) {
        AsyncExecutorService.getInstance().execute(() -> {
            GroupManager.getInstance().updateGroupOwner(c.getCid(), newOwnerUserId, new ImCallback<Group>() {
                @Override
                public void onSuccess(Group data) {
                    // 更新内存数据-群主
                    c.getGroup().setOwnerId(newOwnerUserId);
                    c.getGroup().getRelationOfMe().setRole(GroupMemberRoleEnum.NORMAL);

                    LiveDataResult result = new LiveDataResult(true);
                    transferOwnerResult.postValue(result);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("Transfer Owner Failed: %s/%s/%s", errorCode, message, t.getMessage());
                    LiveDataResult result = new LiveDataResult(false, msg);
                    transferOwnerResult.postValue(result);
                }
            });
        });
    }

    /**
     * 新增群管理员
     *
     * @param c
     * @param adminUserIds
     */
    public void addGroupAdmin(Conversation c, List<String> adminUserIds) {
        GroupManager.getInstance().addGroupAdmin(c.getCid(), adminUserIds.toArray(new String[]{}),
                new ImCallback<Group>() {
                    @Override
                    public void onSuccess(Group data) {
                        // 更新内存数据-群管理员列表
                        List<String> newAdminUserIds = Lists.newArrayList(c.getGroup().getAdminUsers());
                        newAdminUserIds.addAll(adminUserIds);
                        c.getGroup().setAdminUsers(newAdminUserIds);

                        notifyUpdateSettingSuccess(true);
                    }

                    @Override
                    public void onError(String errorCode, String message, Throwable t) {
                        notifyUpdateSettingFail(errorCode, message, t);
                    }
                });
    }

    /**
     * 删除群管理员
     *
     * @param c
     * @param adminUserIds
     */
    public void removeGroupAdmin(Conversation c, List<String> adminUserIds) {
        GroupManager.getInstance().removeGroupAdmin(c.getCid(), adminUserIds.toArray(new String[]{}),
                new ImCallback<Group>() {
                    @Override
                    public void onSuccess(Group data) {
                        // 更新内存数据-群管理员列表
                        List<String> newAdminUserIds = Lists.newArrayList(c.getGroup().getAdminUsers());
                        newAdminUserIds.removeAll(adminUserIds);
                        c.getGroup().setAdminUsers(newAdminUserIds);

                        notifyUpdateSettingSuccess(true);
                    }

                    @Override
                    public void onError(String errorCode, String message, Throwable t) {
                        notifyUpdateSettingFail(errorCode, message, t);
                    }
                });
    }

    /**
     * 新增禁言成员
     *
     * @param c
     * @param memberUserIds
     */
    public void addGroupMuteMember(Conversation c, List<String> memberUserIds) {
        GroupManager.getInstance().silenceGroupMember(c.getCid(), memberUserIds, new ImCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                notifyUpdateSettingSuccess(true);
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                notifyUpdateSettingFail(errorCode, message, t);
            }
        });
    }

    /**
     * 取消禁言成员
     *
     * @param c
     * @param memberUserIds
     */
    public void cancelGroupMuteMember(Conversation c, List<String> memberUserIds) {
        GroupManager.getInstance().cancelSilenceGroupMember(c.getCid(), memberUserIds, new ImCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                notifyUpdateSettingSuccess(true);
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                notifyUpdateSettingFail(errorCode, message, t);
            }
        });
    }

    /**
     * 全员禁言
     *
     * @param c
     */
    public void muteAll(Conversation c) {
        GroupManager.getInstance().silenceGroup(c.getCid(), new ImCallback<Group>() {
            @Override
            public void onSuccess(Group data) {
                // 更新内存数据-全体禁言标记
                c.getGroup().setSilenceAll(true);

                notifyUpdateSettingSuccess(true);
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                notifyUpdateSettingFail(errorCode, message, t);
            }
        });
    }

    /**
     * 取消全员禁言
     *
     * @param c
     */
    public void cancelMuteAll(Conversation c) {
        GroupManager.getInstance().cancelSilenceGroup(c.getCid(), new ImCallback<Group>() {
            @Override
            public void onSuccess(Group data) {
                // 更新内存数据-全体禁言标记
                c.getGroup().setSilenceAll(false);

                notifyUpdateSettingSuccess(true);
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                notifyUpdateSettingFail(errorCode, message, t);
            }
        });
    }

    /**
     * 查询基于打标的群设置数据
     *
     * @param c
     */
    public void queryGroupSetting(Conversation c) {
        GroupManager.getInstance().queryGroupSetting(c.getCid(), new ImCallback<GroupSetting>() {
            @Override
            public void onSuccess(GroupSetting data) {
                LiveDataResult<GroupSetting> result = new LiveDataResult<GroupSetting>(true, data);
                groupSettingResult.postValue(result);
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                String msg = String.format("Query Group Setting Failed: %s/%s/%s", errorCode, message, t.getMessage());
                LiveDataResult<GroupSetting> result = new LiveDataResult<GroupSetting>(false, msg);
                groupSettingResult.postValue(result);
            }
        });
    }

    /**
     * 更新群设置数据
     *
     * @param c
     * @param groupSetting
     */
    public void updateGroupSetting(Conversation c, GroupSetting groupSetting) {
        GroupManager.getInstance().saveGroupSetting(c.getCid(), groupSetting, new ImCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                notifyUpdateSettingSuccess(true);
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                notifyUpdateSettingFail(errorCode, message, t);
            }
        });
    }

    public MutableLiveData<PageDataResult<List<GroupRelation>>> getGroupRelationListResult() {
        return groupRelationListResult;
    }

    public MutableLiveData<LiveDataResult> getLeaveGroupResult() {
        return leaveGroupResult;
    }

    public MutableLiveData<LiveDataResult> getDismissGroupResult() {
        return dismissGroupResult;
    }

    public MutableLiveData<LiveDataResult> getTransferOwnerResult() {
        return transferOwnerResult;
    }

    public MutableLiveData<LiveDataResult<GroupSetting>> getGroupSettingResult() {
        return groupSettingResult;
    }

}
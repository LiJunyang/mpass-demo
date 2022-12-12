/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.common.service.facade.enums.SessionMetaTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.converter.SessionMemberConverter;
import cn.hsbcsd.mpaastest.databinding.ActivityGroupChatSettingBinding;
import com.hsbcd.mpaastest.kotlin.samples.enums.UpdateTextSettingTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.model.SessionMemberItem;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Group;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.AlertDialogFragment;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.home.HomeActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.GroupUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 群聊设置页
 *
 * @author liyalong
 * @version GroupChatSettingActivity.java, v 0.1 2022年08月10日 20:14 liyalong
 */
public class GroupChatSettingActivity extends AppCompatActivity {

    private static final String DEFAULT_VALUE = "unset";

    private ActivityGroupChatSettingBinding binding;

    private SessionMemberGridAdapter sessionMemberGridAdapter;

    private GroupChatSettingViewModel groupChatSettingViewModel;

    //private FavoriteViewModel favoriteViewModel;

    private Conversation c;

    private boolean initSettingFinish = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGroupChatSettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 绑定组件动作
        bindAction();

        // 绑定聊天设置数据
        groupChatSettingViewModel = new ViewModelProvider(this).get(GroupChatSettingViewModel.class);
        bindGroupChatSettingViewModel();

//        // 绑定收藏数据
//        favoriteViewModel = new ViewModelProvider(this).get(FavoriteViewModel.class);
//        bindFavoriteViewModel();
    }

    /**
     * 每次进入页面时
     */
    @Override
    protected void onStart() {
        super.onStart();

        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c == null) {
            Log.e(LoggerName.UI, "unset session，cannot go to setting");
            super.onBackPressed();
            return;
        }
        this.c = c;

        // 绑定各组件的初始值
        bindSettingValue();

        // 加载成员网格视图
        loadMemberGrid();

        // 加载群会话标签
        loadFavoriteTag();
    }

    private void bindSettingValue() {
        Group group = c.getGroup();

        Glide.with(this).load(group.getLogoUrl()).diskCacheStrategy(DiskCacheStrategy.ALL).into(binding.groupAvatar);
        binding.groupNameLabel.setText(group.getName());
        binding.groupDescLabel.setText(StringUtils.defaultIfBlank(group.getRemark(), DEFAULT_VALUE));

        binding.showAllMemberLabel.setText(String.format("See All Members(%d)", c.getGroup().getMemberCount()));

        binding.groupNickname.setText(StringUtils.defaultIfBlank(group.getRelationOfMe().getUserNick(), DEFAULT_VALUE));
        binding.groupMemo.setText(StringUtils.defaultIfBlank(group.getRelationOfMe().getUserMark(), DEFAULT_VALUE));
        binding.setTopSwitch.setChecked(c.isTopMode());
        binding.setNoDisturbSwitch.setChecked(c.isShieldMode());

        binding.groupName.setText(group.getName());
        binding.groupDesc.setText(StringUtils.defaultIfBlank(group.getRemark(), DEFAULT_VALUE));
        binding.groupNotice.setText(StringUtils.defaultIfBlank(group.getNotice(), DEFAULT_VALUE));

        if (GroupUtil.isGroupAdmin(c)) {
            binding.groupAdminLayout.setVisibility(View.VISIBLE);
        }

        // 对群主隐藏退出群聊按钮
        binding.leaveGroup.setVisibility(GroupUtil.isGroupOwner(c) ? View.GONE : View.VISIBLE);

        initSettingFinish = true;
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());

        // 跳转到群成员列表页
        binding.showAllMemberLabel.setOnClickListener(v -> goToSessionMemberListActivity());

        // 跳转到设置项更新页
        binding.groupNickname.setOnClickListener(v -> goToUpdateActivity(UpdateTextSettingTypeEnum.GROUP_NICK_NAME));
        binding.groupMemo.setOnClickListener(v -> goToUpdateActivity(UpdateTextSettingTypeEnum.GROUP_MEMO));
        binding.groupName.setOnClickListener(v -> goToUpdateActivity(UpdateTextSettingTypeEnum.GROUP_NAME));
        binding.groupDesc.setOnClickListener(v -> goToUpdateActivity(UpdateTextSettingTypeEnum.GROUP_DESC));
        binding.groupNotice.setOnClickListener(v -> goToUpdateActivity(UpdateTextSettingTypeEnum.GROUP_NOTICE));
        binding.groupTag.setOnClickListener(v -> goToAddFavoriteActivity());

        // 更新设置项开关
        binding.setTopSwitch.setOnCheckedChangeListener((v, checked) -> {
            if (initSettingFinish) {
                groupChatSettingViewModel.setToTop(c, checked);
            }
        });

        binding.setNoDisturbSwitch.setOnCheckedChangeListener((v, checked) -> {
            if (initSettingFinish) {
                groupChatSettingViewModel.setNoDisturb(c, checked);
            }
        });

        // 跳转到群管理页
        binding.groupAdminLayout.setOnClickListener(v -> goToAdminSettingActivity());

        // 清空聊天记录
        binding.clearMessage.setOnClickListener(v -> {
            AlertDialogFragment dialog = new AlertDialogFragment("是否清空聊天记录", null,
                    () -> groupChatSettingViewModel.clearAllMessage(c));
            dialog.show(getSupportFragmentManager(), "");
        });

        // 退出群聊
        binding.leaveGroup.setOnClickListener(v -> {
            AlertDialogFragment dialog = new AlertDialogFragment("是否退出群聊", null,
                    () -> groupChatSettingViewModel.leaveGroup(c));
            dialog.show(getSupportFragmentManager(), "");
        });
    }

    private void goToUpdateActivity(UpdateTextSettingTypeEnum type) {
        Intent intent = new Intent(this, UpdateTextSettingActivity.class);

        intent.putExtra(UpdateTextSettingActivity.TYPE_KEY, type.name());

        // 修改群名称/群介绍/群公告时，检查用户是否有修改权限
        if (type == UpdateTextSettingTypeEnum.GROUP_NAME || type == UpdateTextSettingTypeEnum.GROUP_DESC ||
                type == UpdateTextSettingTypeEnum.GROUP_NOTICE) {
            // 如果当前设置为仅群主和管理员有修改权限，则普通成员只读
            if (!GroupUtil.canModifyGroup(c)) {
                intent.putExtra(UpdateTextSettingActivity.READ_ONLY_KEY, true);
            }
        }

        this.startActivity(intent);
    }

    private void goToAddFavoriteActivity() {
//        // 打开新增收藏页
//        Intent intent = new Intent(this, AddFavoriteContentActivity.class);
//        intent.putExtra(AddFavoriteContentActivity.TARGET_TYPE, CollectType.SESSION.getCode());
//        intent.putExtra(AddFavoriteContentActivity.TARGET_VALUE, c.getCid());
//        this.startActivity(intent);
    }

    private void goToAdminSettingActivity() {
        Intent intent = new Intent(this, GroupAdminSettingActivity.class);
        this.startActivity(intent);
    }

    private void goToSessionMemberListActivity() {
        Intent intent = new Intent(this, SessionMemberListActivity.class);
        this.startActivity(intent);
    }

    private void bindGroupChatSettingViewModel() {
        // 设置项更新完成后的通知
        groupChatSettingViewModel.getUpdateSettingResult().observe(this, result -> {
            if (result.isSuccess()) {
                ToastUtil.makeToast(this, "Update Success", 1000);
            } else {
                ToastUtil.makeToast(this, "Update Failed: " + result.getMessage(), 3000);
            }
        });

        // 返回成员列表查询结果后的通知
        groupChatSettingViewModel.getGroupRelationListResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 1000);
                return;
            }

            // 每次都重新初始化适配器，避免删除群成员后视图存在脏数据
            initMemberGrid();

            // 绑定成员列表数据，并立即渲染
            List<SessionMemberItem> members = SessionMemberConverter.convertGroupRelations(result.getData());
            sessionMemberGridAdapter.setMembers(members);
            sessionMemberGridAdapter.notifyDataSetChanged();
        });

        // 退出群聊后的通知
        groupChatSettingViewModel.getLeaveGroupResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // 跳转回首页
            Intent intent = new Intent(this, HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void bindFavoriteViewModel() {
//        // 会话收藏标签名称列表查询结果通知
//        favoriteViewModel.getFavoriteTagNameListResult().observe(this, result -> {
//            if (!result.isSuccess()) {
//                //ToastUtil.makeToast(this, result.getMessage(), 3000);
//                binding.groupTag.setText(DEFAULT_VALUE);
//                return;
//            }
//
//            List<String> tagNames = result.getData();
//            binding.groupTag.setText(CollectionUtils.isEmpty(tagNames) ? DEFAULT_VALUE : StringUtils.join(tagNames,
//                    CommonConstants.COMMA));
//        });
    }

    private void initMemberGrid() {
        this.sessionMemberGridAdapter = new SessionMemberGridAdapter(this, SessionMetaTypeEnum.GROUP);
        binding.memberGridView.setAdapter(sessionMemberGridAdapter);
    }

    private void loadMemberGrid() {
        // 查询群成员列表，等待返回查询结果后再绑定到视图
        // 注：成员网格视图容量上限为5列*3排=15个元素，去掉新增成员按钮，最多只能展示14个成员
        groupChatSettingViewModel.queryGroupMembers(c, 1, 14);
    }

    private void loadFavoriteTag() {
//        favoriteViewModel.queryTargetFavoriteTags(MarkTargetEnum.SESSION, c.getCid());
    }

}
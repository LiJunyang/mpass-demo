/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import cn.hsbcsd.mpaastest.R;
import cn.hsbcsd.mpaastest.databinding.FragmentSessionListBinding;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.CustomContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.RtcContent;
import com.alipay.fc.ccmimplus.common.service.facade.enums.SessionModeEnum;
import com.alipay.fc.ccmimplus.sdk.core.conversation.ConversationManager;
import com.alipay.fc.ccmimplus.sdk.core.enums.RtcTypeEnum;
import com.alipay.fc.ccmimplus.sdk.core.rtc.RtcRoomManager;
import com.hsbcd.mpaastest.kotlin.samples.app.ImplusApplication;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.enums.ConversationTypeEnum;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.ChatActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting.ChatSettingViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.AlertDialogFragment;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts.AddNewFriendActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts.NewFriendActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.liveshow.PlayLiveShowActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.liveshow.StartLiveShowActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom.ChatroomListActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom.CreateChatroomActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.CreateConversationActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.MessageUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * 会话列表tab页
 *
 * @author liyalong
 * @version SessionFragment.java, v 0.1 2022年07月29日 15:53 liyalong
 */

public class SessionFragment extends Fragment {

    private FragmentSessionListBinding binding;

    private SessionListAdapter sessionListAdapter;

    private SessionViewModel sessionViewModel;

    private ChatSettingViewModel chatSettingViewModel;

    private NewMessageViewModel newMessageViewModel;

    private CustomOnScrollListener customOnScrollListener;

    private boolean refresh = false;

    private Message rtcInviteMessage;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSessionListBinding.inflate(inflater);

        bindAction();

        // 绑定会话数据
        sessionViewModel = new ViewModelProvider(getActivity()).get(SessionViewModel.class);
        bindSessionViewModel();

        // 绑定会话设置数据
        chatSettingViewModel = new ViewModelProvider(getActivity()).get(ChatSettingViewModel.class);
        bindChatSettingViewModel();


        // 绑定消息数据
        newMessageViewModel = new ViewModelProvider(getActivity()).get(NewMessageViewModel.class);
        bindNewMessageViewModel();

        // 初始化会话列表
        initSessionList();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        // 每次进入会话列表页时，都重新查询一次会话列表，确保数据是最新的
        refreshSessionList();
    }

    private void refreshSessionList() {
        refresh = true;
        customOnScrollListener.reset();
        sessionViewModel.queryConversations(1);
    }

    private void bindAction() {

        // 绑定弹出菜单
        bindPopupMenu();

        // 绑定密聊入口
        bindSecretChat();
    }

    @SuppressLint("RestrictedApi")
    private void bindPopupMenu() {

        ActivityResultLauncher livePermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    if (result.isEmpty()) {
                        ToastUtil.makeToast(getActivity(), "no live show permission", 1000);
                        return;
                    }

                    // 打开创建聊天室页
                    Intent intent = new Intent(getContext(), CreateChatroomActivity.class);
                    this.startActivity(intent);
                });

        ActivityResultLauncher liveShowPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    if (result.isEmpty()) {
                        ToastUtil.makeToast(getActivity(), "no live show permission", 1000);
                        return;
                    }

                    Intent intent = new Intent(getContext(), StartLiveShowActivity.class);
                    this.startActivity(intent);
                });

        binding.popupMenu.setOnClickListener(v -> {
            // 设置弹出菜单项的样式
            ContextThemeWrapper wrapper = new ContextThemeWrapper(getContext(), R.style.MyPopupMenuStyle);
            PopupMenu menu = new PopupMenu(wrapper, v);
            menu.getMenuInflater().inflate(R.menu.menu_session, menu.getMenu());
            menu.setGravity(Gravity.RIGHT);

            try {
                // 弹出菜单项的图标置为可见
                Field field = menu.getClass().getDeclaredField("mPopup");
                field.setAccessible(true);
                MenuPopupHelper mHelper = (MenuPopupHelper) field.get(menu);
                mHelper.setForceShowIcon(true);
//                MenuPopupHelper menuHelper = new MenuPopupHelper(wrapper, (MenuBuilder) menu.getMenu(), v);
//                menuHelper.setForceShowIcon(true);

                // 设置不测量item宽度
                Class standardMenuClass = Class.forName("androidx.appcompat.view.menu.StandardMenuPopup");
                Field mHasContentWidth = standardMenuClass.getDeclaredField("mHasContentWidth");
                mHasContentWidth.setAccessible(true);
                mHasContentWidth.setBoolean(mHelper.getPopup(), true);

                // 设置弹出菜单宽度
                Field mContentWidth = standardMenuClass.getDeclaredField("mContentWidth");
                mContentWidth.setAccessible(true);
                mContentWidth.setInt(mHelper.getPopup(), 500);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 设置弹出菜单项的点击动作
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.add_friend: {
                        Intent intent = new Intent(getContext(), AddNewFriendActivity.class);
                        this.startActivity(intent);
                        break;
                    }
                    case R.id.create_single_conversation: {
                        Intent intent = new Intent(getContext(), CreateConversationActivity.class);
                        intent.putExtra(CreateConversationActivity.SESSION_TYPE_KEY,
                                ConversationTypeEnum.SINGLE.getCode());
                        this.startActivity(intent);
                        break;
                    }
                    case R.id.create_group_conversation: {
                        Intent intent = new Intent(getContext(), CreateConversationActivity.class);
                        intent.putExtra(CreateConversationActivity.SESSION_TYPE_KEY,
                                ConversationTypeEnum.GROUP.getCode());
                        this.startActivity(intent);
                        break;
                    }
                    case R.id.create_chatroom: {
                        // 先请求系统权限，等用户同意后再打开创建聊天室页
                        livePermissionLauncher.launch(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                // mpaas在部分机型需要蓝牙连接权限
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN});
                        break;
                    }
                    case R.id.create_secret_chat: {
                        Intent intent = new Intent(getContext(), CreateConversationActivity.class);
                        intent.putExtra(CreateConversationActivity.SESSION_TYPE_KEY,
                                ConversationTypeEnum.SINGLE.getCode());
                        intent.putExtra(CreateConversationActivity.SESSION_MODE_KEY,
                                SessionModeEnum.SECRET_CHAT.getCode());
                        this.startActivity(intent);
                        break;
                    }
                    case R.id.start_live_show: {
                        ToastUtil.makeToast(getActivity(), "Test only, contact Leo to start server, only 1 liveshow accepted", 3000);
                        liveShowPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                        break;
                    }
                    case R.id.play_live_show: {
                        ToastUtil.makeToast(getActivity(), "You can see it only after someone start show.", 3000);
                        Intent intent = new Intent(getContext(), PlayLiveShowActivity.class);
                        this.startActivity(intent);
                        break;
                    }
                    case R.id.review_new_friend: {
                        Intent intent = new Intent(getContext(), NewFriendActivity.class);
                        this.startActivity(intent);
                        break;
                    }
                    case R.id.show_chatroom: {
                        Intent intent = new Intent(getContext(), ChatroomListActivity.class);
                        this.startActivity(intent);
                        break;
                    }
                    default:
                        break;
                }
                return true;
            });

            menu.show();
        });
    }

    private void bindSecretChat() {
        binding.secretChat.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SecretChatSessionListActivity.class);
            getContext().startActivity(intent);
        });
    }

    private void bindSessionViewModel() {
        // 会话列表查询结果通知
        sessionViewModel.getConversationListResult().observe(getViewLifecycleOwner(), result -> {
            binding.refreshLayout.setRefreshing(false);

            if (!result.isSuccess()) {
                ToastUtil.makeToast(getActivity(), result.getMessage(), 3000);
                return;
            }

            customOnScrollListener.setHasNextPage(result.hasNextPage());

            if (CollectionUtils.isEmpty(result.getData())) {
                return;
            }

            // 渲染会话列表
            if (refresh) {
                sessionListAdapter.clearAll();
                refresh = false;
            }

            sessionListAdapter.addAll(result.getData());
            sessionListAdapter.notifyDataSetChanged();
        });

        // 单个会话查询结果通知
        sessionViewModel.getConversationResult().observe(getViewLifecycleOwner(), result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(getActivity(), result.getMessage(), 3000);
                return;
            }

            // 设置当前会话
            AlipayCcmIMClient.getInstance().getConversationManager().setCurrentConversation(result.getData());

            // 打开聊天消息页
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            getActivity().startActivity(intent);
        });

        // 新会话查询结果通知，注意是全局监听，以便处理音视频邀请
        sessionViewModel.getNewConversationResult().observeForever(result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(getActivity(), result.getMessage(), 3000);
                return;
            }

            Conversation c = result.getData();

            // 把新会话插到非置顶会话的最前面
            // 注：群会议除外，群会议为临时会话，仅用于多人音视频会议，不需要展示在会话列表
            if (!c.isGroupMeeting()) {
                sessionListAdapter.insertToTop(c);
                sessionListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void bindChatSettingViewModel() {
        chatSettingViewModel.getUpdateSettingResult().observe(getViewLifecycleOwner(), result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(getActivity(), "更新失败: " + result.getMessage(), 3000);
            }

            // 重新加载会话列表，展示更新后的会话设置项(置顶/免打扰等)
            refreshSessionList();
        });
    }

    private void bindNewMessageViewModel() {
        // 收到新消息后的动作，注意是全局监听，下同
        newMessageViewModel.getNewMessageResult().observeForever(result -> {
            Message message = result.getData();

            int position = sessionListAdapter.indexOf(message.getSid());
            Conversation c = sessionListAdapter.getByCid(message.getSid());

            // 如果该会话不在现有列表，则先从服务端查询新会话，等待返回查询结果后再处理
            if (position == -1) {
                // 如果是实时音视频邀请，暂存邀请消息
                if (message.isRtcMessage() && !MessageUtil.isSendByMe(message)) {
                    rtcInviteMessage = message;
                }

                sessionViewModel.queryNewConversation(message.getSid());
                return;
            }

            // 如果新消息所属会话非置顶，则把会话挪到所有非置顶会话的最前面，否则保持不动
            if (!c.isTopMode()) {
                // 先从现有列表里删掉
                sessionListAdapter.removeByCid(message.getSid());

                // 再挪到非置顶会话的最前面
                sessionListAdapter.insertToTop(c);
            }

            // 使用新消息内容重新渲染该会话的最近一条消息，注意传的是该会话在列表中原有位置的下标
            sessionListAdapter.notifyItemChanged(position, message);
        });

        // 收到自定义命令消息
        newMessageViewModel.getCustomCommandResult().observeForever(result -> {
            CustomContent content = result.getData();
            String str = String.format("[%s] %s", content.getDataType(), new String(content.getData()));

            Activity currentActivity = ImplusApplication.Companion.getCurrentActivity();

            if (currentActivity == null) {
                Log.e(LoggerName.UI, "current activity is null");
                return;
            }

            // 弹窗显示
            if (currentActivity instanceof FragmentActivity) {
                AlertDialogFragment dialog = new AlertDialogFragment("命令消息", str);
                dialog.show(((FragmentActivity) currentActivity).getSupportFragmentManager(), "");
            }
        });
    }

    private void initSessionList() {
        this.sessionListAdapter = new SessionListAdapter(getContext());
        binding.sessionListView.setAdapter(sessionListAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.sessionListView.setLayoutManager(linearLayoutManager);

        // 下拉刷新
        binding.refreshLayout.setOnRefreshListener(() -> {
            refreshSessionList();
        });

        // 上拉加载更多
        customOnScrollListener = new CustomOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
                sessionViewModel.queryConversations(pageIndex);
            }

            @Override
            public void onRefresh() {
                //refreshSessionList();
            }

            @Override
            public void onNoNextPage() {
                ToastUtil.makeToast(getActivity(), "No more chat", 1000);
            }
        };

        binding.sessionListView.addOnScrollListener(customOnScrollListener);
    }

}
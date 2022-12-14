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
 * ????????????tab???
 *
 * @author liyalong
 * @version SessionFragment.java, v 0.1 2022???07???29??? 15:53 liyalong
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

        // ??????????????????
        sessionViewModel = new ViewModelProvider(getActivity()).get(SessionViewModel.class);
        bindSessionViewModel();

        // ????????????????????????
        chatSettingViewModel = new ViewModelProvider(getActivity()).get(ChatSettingViewModel.class);
        bindChatSettingViewModel();


        // ??????????????????
        newMessageViewModel = new ViewModelProvider(getActivity()).get(NewMessageViewModel.class);
        bindNewMessageViewModel();

        // ?????????????????????
        initSessionList();

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ?????????????????????????????????????????????????????????????????????????????????????????????
        refreshSessionList();
    }

    private void refreshSessionList() {
        refresh = true;
        customOnScrollListener.reset();
        sessionViewModel.queryConversations(1);
    }

    private void bindAction() {

        // ??????????????????
        bindPopupMenu();

        // ??????????????????
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

                    // ????????????????????????
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
            // ??????????????????????????????
            ContextThemeWrapper wrapper = new ContextThemeWrapper(getContext(), R.style.MyPopupMenuStyle);
            PopupMenu menu = new PopupMenu(wrapper, v);
            menu.getMenuInflater().inflate(R.menu.menu_session, menu.getMenu());
            menu.setGravity(Gravity.RIGHT);

            try {
                // ????????????????????????????????????
                Field field = menu.getClass().getDeclaredField("mPopup");
                field.setAccessible(true);
                MenuPopupHelper mHelper = (MenuPopupHelper) field.get(menu);
                mHelper.setForceShowIcon(true);
//                MenuPopupHelper menuHelper = new MenuPopupHelper(wrapper, (MenuBuilder) menu.getMenu(), v);
//                menuHelper.setForceShowIcon(true);

                // ???????????????item??????
                Class standardMenuClass = Class.forName("androidx.appcompat.view.menu.StandardMenuPopup");
                Field mHasContentWidth = standardMenuClass.getDeclaredField("mHasContentWidth");
                mHasContentWidth.setAccessible(true);
                mHasContentWidth.setBoolean(mHelper.getPopup(), true);

                // ????????????????????????
                Field mContentWidth = standardMenuClass.getDeclaredField("mContentWidth");
                mContentWidth.setAccessible(true);
                mContentWidth.setInt(mHelper.getPopup(), 500);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // ????????????????????????????????????
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
                        // ?????????????????????????????????????????????????????????????????????
                        livePermissionLauncher.launch(new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO,
                                // mpaas???????????????????????????????????????
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
//                    case R.id.start_live_show: {
//                        ToastUtil.makeToast(getActivity(), "Test only, contact Leo to start server, only 1 liveshow accepted", 3000);
//                        liveShowPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA,
//                                Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE});
//                        break;
//                    }
//                    case R.id.play_live_show: {
//                        ToastUtil.makeToast(getActivity(), "You can see it only after someone start show.", 3000);
//                        Intent intent = new Intent(getContext(), PlayLiveShowActivity.class);
//                        this.startActivity(intent);
//                        break;
//                    }
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
        // ??????????????????????????????
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

            // ??????????????????
            if (refresh) {
                sessionListAdapter.clearAll();
                refresh = false;
            }

            sessionListAdapter.addAll(result.getData());
            sessionListAdapter.notifyDataSetChanged();
        });

        // ??????????????????????????????
        sessionViewModel.getConversationResult().observe(getViewLifecycleOwner(), result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(getActivity(), result.getMessage(), 3000);
                return;
            }

            // ??????????????????
            AlipayCcmIMClient.getInstance().getConversationManager().setCurrentConversation(result.getData());

            // ?????????????????????
            Intent intent = new Intent(getActivity(), ChatActivity.class);
            getActivity().startActivity(intent);
        });

        // ?????????????????????????????????????????????????????????????????????????????????
        sessionViewModel.getNewConversationResult().observeForever(result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(getActivity(), result.getMessage(), 3000);
                return;
            }

            Conversation c = result.getData();

            // ?????????????????????????????????????????????
            // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (!c.isGroupMeeting()) {
                sessionListAdapter.insertToTop(c);
                sessionListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void bindChatSettingViewModel() {
        chatSettingViewModel.getUpdateSettingResult().observe(getViewLifecycleOwner(), result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(getActivity(), "????????????: " + result.getMessage(), 3000);
            }

            // ????????????????????????????????????????????????????????????(??????/????????????)
            refreshSessionList();
        });
    }

    private void bindNewMessageViewModel() {
        // ????????????????????????????????????????????????????????????
        newMessageViewModel.getNewMessageResult().observeForever(result -> {
            Message message = result.getData();

            int position = sessionListAdapter.indexOf(message.getSid());
            Conversation c = sessionListAdapter.getByCid(message.getSid());

            // ????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (position == -1) {
                // ???????????????????????????????????????????????????
                if (message.isRtcMessage() && !MessageUtil.isSendByMe(message)) {
                    rtcInviteMessage = message;
                }

                sessionViewModel.queryNewConversation(message.getSid());
                return;
            }

            // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????
            if (!c.isTopMode()) {
                // ???????????????????????????
                sessionListAdapter.removeByCid(message.getSid());

                // ????????????????????????????????????
                sessionListAdapter.insertToTop(c);
            }

            // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
            sessionListAdapter.notifyItemChanged(position, message);
        });

        // ???????????????????????????
        newMessageViewModel.getCustomCommandResult().observeForever(result -> {
            CustomContent content = result.getData();
            String str = String.format("[%s] %s", content.getDataType(), new String(content.getData()));

            Activity currentActivity = ImplusApplication.Companion.getCurrentActivity();

            if (currentActivity == null) {
                Log.e(LoggerName.UI, "current activity is null");
                return;
            }

            // ????????????
            if (currentActivity instanceof FragmentActivity) {
                AlertDialogFragment dialog = new AlertDialogFragment("????????????", str);
                dialog.show(((FragmentActivity) currentActivity).getSupportFragmentManager(), "");
            }
        });
    }

    private void initSessionList() {
        this.sessionListAdapter = new SessionListAdapter(getContext());
        binding.sessionListView.setAdapter(sessionListAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        binding.sessionListView.setLayoutManager(linearLayoutManager);

        // ????????????
        binding.refreshLayout.setOnRefreshListener(() -> {
            refreshSessionList();
        });

        // ??????????????????
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
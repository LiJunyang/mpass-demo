/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.chatroom;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageDeleteStatusEnum;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;


import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.MessageViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.AlertDialogFragment;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.SessionViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.chatroom.ChatroomViewModel;
import com.hsbcd.mpaastest.kotlin.samples.util.ChatroomUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.LiveStreamUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.MessageUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.UserUtil;

import cn.hsbcsd.mpaastest.R;
import cn.hsbcsd.mpaastest.databinding.ActivityChatroomLiveBinding;

/**
 * ??????????????????
 *
 * @author liyalong
 * @version ChatroomLiveActivity.java, v 0.1 2022???11???11??? 14:55 liyalong
 */
public class ChatroomLiveActivity extends AppCompatActivity {

    private ActivityChatroomLiveBinding binding;

    private ChatroomViewModel chatroomViewModel;

    private SessionViewModel sessionViewModel;

    private MessageViewModel messageViewModel;

    private LivePushViewModel livePushViewModel;

    private Conversation c;

    private ChatroomLiveMessageListAdapter messageListAdapter;

    private CustomOnScrollListener customOnScrollListener;

    private ExoPlayer player;

    private boolean livePushInitFinish;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle();

        binding = ActivityChatroomLiveBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // ??????????????????
        setCurrentSession();

        // ??????????????????
        bindAction();

        // ?????????????????????
        bindChatroomViewModel();

        // ??????????????????
        bindSessionViewModel();

        // ??????????????????
        bindMessageViewModel();

        // ?????????????????????
        initMessageList();

        // ???????????????
        joinChatroom();

        if (ChatroomUtil.isChatroomOwner(c)) {
            // ????????????????????????
            bindLivePushViewModel();

            // ?????????????????????
            initLiveStreamPush();
        } else {
            // ?????????????????????
            initLiveStreamPull();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO ????????????????????????????????????????????????????????????
        if (livePushInitFinish && livePushViewModel != null) {

        }
    }

    /**
     * ?????????????????????
     */
    @Override
    protected void onStop() {
        super.onStop();

        // ??????????????????
        // ???????????????????????????activity??????????????????????????????finished=false???????????????
        if (isFinishing()) {
            AlipayCcmIMClient.getInstance().getConversationManager().clearCurrentConversation();
            leaveChatroom();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (livePushViewModel != null) {
            livePushViewModel.destroy();
        }

        if (player != null) {
            player.release();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

//    @Override
//    public void onBackPressed() {
//        // ??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
//        Intent intent = new Intent(this, ChatroomListActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        this.startActivity(intent);
//    }

    private void setStyle() {
        // ???????????????????????????????????????????????????????????????
        getWindow().setStatusBarColor(getResources().getColor(R.color.dark_gray3));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_gray3));

        // ??????????????????????????????
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void setCurrentSession() {
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c == null) {
            Log.e(LoggerName.UI, "chat unset, cannot go to message page");
            onBackPressed();
            return;
        }
        this.c = c;
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> onBackPressed());

        binding.chatroomName.setText(c.getSessionName());
        renderChatroomOnlineMemberCount();

        // ?????????owner??????
        if (ChatroomUtil.isChatroomOwner(c)) {
            binding.chatroomLiveDuration.setVisibility(View.VISIBLE);
            binding.chatroomLiveDurationDivide.setVisibility(View.VISIBLE);
            binding.closeChatroom.setVisibility(View.VISIBLE);
            binding.chatroomLiveZoomIn.setVisibility(View.GONE);
            binding.reverseCamera.setVisibility(View.VISIBLE);
            binding.chatroomSetting.setVisibility(View.VISIBLE);

            // ??????????????????????????????????????????????????????(??????????????????)??????????????????????????????
            long baseTime = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - c.getChatroom().getGmtCreate());
            binding.chatroomLiveDuration.setBase(baseTime);
            binding.chatroomLiveDuration.start();

            // ?????????????????????????????????????????????
            binding.closeChatroom.setOnClickListener(v -> {
                AlertDialogFragment dialog = new AlertDialogFragment("End show?", null,
                        () -> sessionViewModel.deleteConversation(c.getCid()));
                dialog.show(getSupportFragmentManager(), "");
            });

            binding.reverseCamera.setOnClickListener(v -> livePushViewModel.switchCamera());

            // ???????????????????????????
            binding.chatroomSetting.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatroomAdminSettingActivity.class);
                this.startActivity(intent);
            });
        }
        // ???????????????????????????
        else {
            binding.chatroomLiveDuration.setVisibility(View.GONE);
            binding.chatroomLiveDurationDivide.setVisibility(View.GONE);
            binding.closeChatroom.setVisibility(View.GONE);
            binding.chatroomLiveZoomIn.setVisibility(View.VISIBLE);
            binding.reverseCamera.setVisibility(View.GONE);
            binding.chatroomSetting.setVisibility(View.GONE);

            // ??????????????????
            binding.chatroomLiveZoomIn.setOnClickListener(v -> {
                todoToast();

                // TODO ?????????????????????
//                Intent intent = new Intent(this, ChatroomChatActivity.class);
//                this.startActivity(intent);
            });
        }

        // ??????/???????????????????????????
        binding.setMessageVisible.setOnClickListener(v -> {
            binding.setMessageVisible.setVisibility(View.GONE);
            binding.setMessageInvisible.setVisibility(View.VISIBLE);
            binding.messageListView.setVisibility(View.GONE);
            binding.inputText.setVisibility(View.INVISIBLE);
        });
        binding.setMessageInvisible.setOnClickListener(v -> {
            binding.setMessageVisible.setVisibility(View.VISIBLE);
            binding.setMessageInvisible.setVisibility(View.GONE);
            binding.messageListView.setVisibility(View.VISIBLE);
            binding.inputText.setVisibility(View.VISIBLE);
        });

        // ????????????????????????
        bindInputText();
    }

    private void bindInputText() {
        binding.inputText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                    || actionId == EditorInfo.IME_ACTION_SEND
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                // ??????????????????????????????????????????ACTION_UP???ACTION_DOWN??????????????????ACTION_UP?????????????????????????????????
                if (event == null || event.getAction() == KeyEvent.ACTION_UP) {
                    sendTextMessage();
                }
            }
            return true;
        });
    }

    private void renderChatroomOnlineMemberCount() {
        binding.chatroomOnlineMemberCount.setText(String.format("%d ppl in the chatroom", c.getChatroom().getOnlineMemberCount()));
    }

    private void todoToast() {
        ToastUtil.makeToast(this, "to be added", 1000);
    }

    private void bindChatroomViewModel() {
        chatroomViewModel = new ViewModelProvider(this).get(ChatroomViewModel.class);
        chatroomViewModel.registerChatroomListener(c.getCid());

        // ?????????????????????
        chatroomViewModel.getChatroomNotifyResult().observe(this, result -> {
            switch (result.getOperation()) {
                case JOIN_CHATROOM: {
                    UserUtil.getAndConsumeUserName(result.getUserId(), (userName) -> {
                        Message message = MessageUtil.buildNotifyMessage(String.format("%s joined the chatroom", userName));
                        insertMessage(message);
                    });

                    c.getChatroom().setOnlineMemberCount(c.getChatroom().getOnlineMemberCount() + 1);
                    renderChatroomOnlineMemberCount();

                    // ??????????????????owner?????????????????????????????????????????????????????????????????????????????????
                    if (!ChatroomUtil.isChatroomOwner(c) && StringUtils.equals(result.getUserId(),
                            c.getChatroom().getOwnerId())) {
                        initLiveStreamPull();
                    }
                    break;
                }
                case LEAVE_CHATROOM: {
                    UserUtil.getAndConsumeUserName(result.getUserId(), (userName) -> {
                        Message message = MessageUtil.buildNotifyMessage(String.format("%s leave the chatroom", userName));
                        insertMessage(message);
                    });

                    c.getChatroom().setOnlineMemberCount(c.getChatroom().getOnlineMemberCount() - 1);
                    renderChatroomOnlineMemberCount();

                    // ??????????????????owner??????????????????????????????
                    if (!ChatroomUtil.isChatroomOwner(c) && StringUtils.equals(result.getUserId(),
                            c.getChatroom().getOwnerId())) {
                        stopLiveStreamPull();
                    }
                    break;
                }
                case DELETE_CHATROOM: {
                    UserUtil.getAndConsumeUserName(result.getUserId(), (userName) -> {
                        AlertDialogFragment dialog = new AlertDialogFragment("Live show is ended",
                                String.format("%s close chatroom", userName), AlertDialogFragment.DialogMode.NOTIFY_WITH_ACTION,
                                () -> onBackPressed());
                        dialog.show(getSupportFragmentManager(), "");
                    });
                    break;
                }
                default:
                    break;
            }
        });

        // ????????????????????????????????????
        chatroomViewModel.getUpdateSettingResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            String pushUrl = (String) c.getChatroom().getExtInfo().get("liveStreamPushUrl");
            Log.i(LoggerName.UI, "updated liveStreamPushUrl: " + pushUrl);

            if (StringUtils.isBlank(pushUrl)) {
                ToastUtil.makeToast(this, "cannot find the live stream", 1000);
                return;
            }

            if (LiveStreamUtil.isUrlExpired(pushUrl)) {
                ToastUtil.makeToast(this, "live stream is out date", 1000);
                return;
            }

            // ??????????????????
            livePushViewModel.startPush(pushUrl);
        });
    }

    private void bindSessionViewModel() {
        sessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);

        // ????????????????????????
        sessionViewModel.getDeleteConversationResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            onBackPressed();
        });
    }

    private void bindMessageViewModel() {
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);

        // ???????????????????????????????????????
        messageViewModel.getMessageListResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            if (CollectionUtils.isEmpty(result.getData())) {
                if (messageListAdapter.getItemCount() > 0) {
                    ToastUtil.makeToast(this, "no more message", 1000);
                }
                return;
            }

            // ????????????????????????
            List<Message> messages = result.getData().stream()
                    .filter(m -> m.getDeleteStatus() == MessageDeleteStatusEnum.NORMAL)
                    .collect(Collectors.toList());

            // ??????????????????????????????????????????????????????????????????????????????
            messageListAdapter.addAll(0, messages);
            messageListAdapter.notifyDataSetChanged();

            // ?????????????????????????????????
            binding.messageListView.scrollToTop(messages.size());
        });

        // ????????????????????????/??????????????????
        messageViewModel.getReceiveMessageResult().observe(this, result -> {
            Message message = result.getMessage();

            switch (result.getType()) {
                // ????????????
                case Normal: {
                    // ??????????????????
                    insertMessage(message);
                    break;
                }
                // ??????????????????
                case CmdRecall: {
                    deleteMessage(message);
                    break;
                }
                default:
                    break;
            }
        });

        // ?????????????????????
        messageViewModel.getSendMessageResult().observe(this, result -> {
            Message message = result.getMessage();

            switch (result.getStatus()) {
                // ????????????????????????
                case BEGIN: {
                    // ??????????????????
                    insertMessage(message);

                    // ?????????????????????
                    binding.inputText.getText().clear();
                    break;
                }
                // ??????????????????
                case FAIL: {
                    ToastUtil.makeToast(this, result.getErrorMessage(), 3000);
                    break;
                }
                default:
                    break;
            }
        });
    }

    private void bindLivePushViewModel() {
        livePushViewModel = new ViewModelProvider(this).get(LivePushViewModel.class);

        // ????????????????????????
        livePushViewModel.getPreviewResult().observe(this, result -> {
            String pushUrl = (String) c.getChatroom().getExtInfo().get("liveStreamPushUrl");
            Log.i(LoggerName.UI, "liveStreamPushUrl: " + pushUrl);

            if (StringUtils.isBlank(pushUrl)) {
                ToastUtil.makeToast(this, "can not find the live stream", 1000);
                return;
            }

            // ????????????????????????????????????????????????????????????????????????????????????
            if (LiveStreamUtil.isUrlExpired(pushUrl)) {
                chatroomViewModel.updateLiveStreamInfo(c);
                return;
            }

            // ??????????????????
            livePushViewModel.startPush(pushUrl);
        });
    }

    private void initMessageList() {
        this.messageListAdapter = new ChatroomLiveMessageListAdapter(this);
        binding.messageListView.setAdapter(messageListAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.messageListView.setLayoutManager(linearLayoutManager);

        customOnScrollListener = new CustomOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
            }

            @Override
            public void onRefresh() {
                messageViewModel.queryHistoryMessages(c);
            }

            @Override
            public void onNoNextPage() {
            }
        };

        binding.messageListView.addOnScrollListener(customOnScrollListener);

        // ???????????????????????????????????????????????????????????????????????????????????????????????????
        binding.messageListView.setOnClickListListener(() -> {
            hideSoftKeyboard();
        });
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void joinChatroom() {
        chatroomViewModel.joinChatroom(c.getCid());
    }

    private void leaveChatroom() {
        chatroomViewModel.leaveChatroom(c.getCid());
    }

    /**
     * ??????????????????
     */
    public void sendTextMessage() {
        String textMsg = binding.inputText.getText().toString();
        if (StringUtils.isBlank(textMsg)) {
            return;
        }

        messageViewModel.sendTextMessage(textMsg, null);
    }

    private void insertMessage(Message message) {
        messageListAdapter.addOne(message);
        messageListAdapter.notifyItemInserted(messageListAdapter.getItemCount() - 1);

        binding.messageListView.scrollToBottom();
    }

    /**
     * ????????????
     *
     * @param message
     */
    public void deleteMessage(Message message) {
        messageListAdapter.remove(message);
    }

    private void initLiveStreamPush() {
        livePushViewModel.init(this);

        binding.chatroomLiveStreamImage.setVisibility(View.GONE);
        binding.chatroomLiveStreamPushView.setVisibility(View.VISIBLE);
        binding.chatroomLiveStreamPullView.setVisibility(View.GONE);

        binding.chatroomLiveStreamPushView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(@NonNull SurfaceHolder holder) {
                livePushViewModel.startPreview(binding.chatroomLiveStreamPushView);
                livePushInitFinish = true;
            }

            @Override
            public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
            }
        });
    }

    private void initLiveStreamPull() {
        String pullUrl = (String) c.getChatroom().getExtInfo().get("liveStreamPullUrl");
        Log.i(LoggerName.UI, "liveStreamPullUrl: " + pullUrl);

        if (StringUtils.isBlank(pullUrl)) {
            ToastUtil.makeToast(this, "can not find the live stream", 1000);
            return;
        }

        if (LiveStreamUtil.isUrlExpired(pullUrl)) {
            ToastUtil.makeToast(this, "live stream is out date", 1000);
            return;
        }

        binding.chatroomLiveStreamImage.setVisibility(View.GONE);
        binding.chatroomLiveStreamPushView.setVisibility(View.GONE);
        binding.chatroomLiveStreamPullView.setVisibility(View.VISIBLE);

        if (player != null) {
            player.release();
        }

        player = new ExoPlayer.Builder(this).build();

        // ?????????????????????
        binding.chatroomLiveStreamPullView.setPlayer(player);

        // ????????????????????????
        RtmpDataSource.Factory rtmpDataSource = new RtmpDataSource.Factory();
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(rtmpDataSource).createMediaSource(
                MediaItem.fromUri(pullUrl));
        player.setMediaSource(mediaSource);

        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                ToastUtil.makeToast(ChatroomLiveActivity.this, "pull live stream failed", 1000);

                binding.chatroomLiveStreamImage.setVisibility(View.VISIBLE);
                binding.chatroomLiveStreamPushView.setVisibility(View.GONE);
                binding.chatroomLiveStreamPullView.setVisibility(View.GONE);
            }
        });

        // ?????????????????????
        player.prepare();
        player.setPlayWhenReady(true);
    }

    private void stopLiveStreamPull() {
        binding.chatroomLiveStreamImage.setVisibility(View.VISIBLE);
        binding.chatroomLiveStreamPushView.setVisibility(View.GONE);
        binding.chatroomLiveStreamPullView.setVisibility(View.GONE);

        if (player != null) {
            player.release();
        }
    }

}
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
 * 聊天室直播页
 *
 * @author liyalong
 * @version ChatroomLiveActivity.java, v 0.1 2022年11月11日 14:55 liyalong
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

        // 设置当前会话
        setCurrentSession();

        // 绑定组件动作
        bindAction();

        // 绑定聊天室数据
        bindChatroomViewModel();

        // 绑定会话数据
        bindSessionViewModel();

        // 绑定消息数据
        bindMessageViewModel();

        // 初始化消息列表
        initMessageList();

        // 加入聊天室
        joinChatroom();

        if (ChatroomUtil.isChatroomOwner(c)) {
            // 绑定直播推流数据
            bindLivePushViewModel();

            // 初始化直播推流
            initLiveStreamPush();
        } else {
            // 初始化直播拉流
            initLiveStreamPull();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // TODO 从后台或其他页面返回时，恢复直播推流预览
        if (livePushInitFinish && livePushViewModel != null) {

        }
    }

    /**
     * 每次退出页面时
     */
    @Override
    protected void onStop() {
        super.onStop();

        // 清除当前会话
        // 注：如果只是被其他activity覆盖而不是退出，此时finished=false，不能清除
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
//        // 强制跳转回聊天室列表页，因为如果在消息页和直播页之间来回切换过，直接返回会默认跳转到消息页，而不是退出聊天室
//        Intent intent = new Intent(this, ChatroomListActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        this.startActivity(intent);
//    }

    private void setStyle() {
        // 顶部状态栏和底部导航栏设置为跟页面背景同色
        getWindow().setStatusBarColor(getResources().getColor(R.color.dark_gray3));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.dark_gray3));

        // 状态栏文字设置为浅色
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

        // 聊天室owner动作
        if (ChatroomUtil.isChatroomOwner(c)) {
            binding.chatroomLiveDuration.setVisibility(View.VISIBLE);
            binding.chatroomLiveDurationDivide.setVisibility(View.VISIBLE);
            binding.closeChatroom.setVisibility(View.VISIBLE);
            binding.chatroomLiveZoomIn.setVisibility(View.GONE);
            binding.reverseCamera.setVisibility(View.VISIBLE);
            binding.chatroomSetting.setVisibility(View.VISIBLE);

            // 直播计时器，起时时间为系统已启动时间(相对时间原点)减去聊天室已创建时间
            long baseTime = SystemClock.elapsedRealtime() - (System.currentTimeMillis() - c.getChatroom().getGmtCreate());
            binding.chatroomLiveDuration.setBase(baseTime);
            binding.chatroomLiveDuration.start();

            // 结束按钮，点击后删除聊天室会话
            binding.closeChatroom.setOnClickListener(v -> {
                AlertDialogFragment dialog = new AlertDialogFragment("End show?", null,
                        () -> sessionViewModel.deleteConversation(c.getCid()));
                dialog.show(getSupportFragmentManager(), "");
            });

            binding.reverseCamera.setOnClickListener(v -> livePushViewModel.switchCamera());

            // 聊天室管理员设置页
            binding.chatroomSetting.setOnClickListener(v -> {
                Intent intent = new Intent(this, ChatroomAdminSettingActivity.class);
                this.startActivity(intent);
            });
        }
        // 聊天室普通成员动作
        else {
            binding.chatroomLiveDuration.setVisibility(View.GONE);
            binding.chatroomLiveDurationDivide.setVisibility(View.GONE);
            binding.closeChatroom.setVisibility(View.GONE);
            binding.chatroomLiveZoomIn.setVisibility(View.VISIBLE);
            binding.reverseCamera.setVisibility(View.GONE);
            binding.chatroomSetting.setVisibility(View.GONE);

            // 跳转到消息页
            binding.chatroomLiveZoomIn.setOnClickListener(v -> {
                todoToast();

                // TODO 跳转逻辑待优化
//                Intent intent = new Intent(this, ChatroomChatActivity.class);
//                this.startActivity(intent);
            });
        }

        // 显示/隐藏聊天室消息列表
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

        // 绑定消息输入组件
        bindInputText();
    }

    private void bindInputText() {
        binding.inputText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                    || actionId == EditorInfo.IME_ACTION_SEND
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                // 注：点击发送消息后会先后触发ACTION_UP和ACTION_DOWN，这里只处理ACTION_UP，避免重复发送两次消息
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

        // 聊天室在线通知
        chatroomViewModel.getChatroomNotifyResult().observe(this, result -> {
            switch (result.getOperation()) {
                case JOIN_CHATROOM: {
                    UserUtil.getAndConsumeUserName(result.getUserId(), (userName) -> {
                        Message message = MessageUtil.buildNotifyMessage(String.format("%s joined the chatroom", userName));
                        insertMessage(message);
                    });

                    c.getChatroom().setOnlineMemberCount(c.getChatroom().getOnlineMemberCount() + 1);
                    renderChatroomOnlineMemberCount();

                    // 如果是聊天室owner进入，则初始化直播拉流，这是主播比观众晚进入房间的情况
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

                    // 如果是聊天室owner离开，则停止直播拉流
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

        // 聊天室直播流信息更新通知
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

            // 开始直播推流
            livePushViewModel.startPush(pushUrl);
        });
    }

    private void bindSessionViewModel() {
        sessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);

        // 删除会话结果通知
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

        // 拉取到历史消息列表后的动作
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

            // 过滤掉已删除消息
            List<Message> messages = result.getData().stream()
                    .filter(m -> m.getDeleteStatus() == MessageDeleteStatusEnum.NORMAL)
                    .collect(Collectors.toList());

            // 新拉取到的消息插到消息列表的头部，并重新渲染消息列表
            messageListAdapter.addAll(0, messages);
            messageListAdapter.notifyDataSetChanged();

            // 消息列表滚动到指定位置
            binding.messageListView.scrollToTop(messages.size());
        });

        // 收到在线推送消息/指令后的动作
        messageViewModel.getReceiveMessageResult().observe(this, result -> {
            Message message = result.getMessage();

            switch (result.getType()) {
                // 普通消息
                case Normal: {
                    // 插入消息列表
                    insertMessage(message);
                    break;
                }
                // 消息撤回通知
                case CmdRecall: {
                    deleteMessage(message);
                    break;
                }
                default:
                    break;
            }
        });

        // 发消息后的动作
        messageViewModel.getSendMessageResult().observe(this, result -> {
            Message message = result.getMessage();

            switch (result.getStatus()) {
                // 准备开始发送消息
                case BEGIN: {
                    // 插入消息列表
                    insertMessage(message);

                    // 清空文本输入框
                    binding.inputText.getText().clear();
                    break;
                }
                // 消息发送失败
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

        // 直播预览通知结果
        livePushViewModel.getPreviewResult().observe(this, result -> {
            String pushUrl = (String) c.getChatroom().getExtInfo().get("liveStreamPushUrl");
            Log.i(LoggerName.UI, "liveStreamPushUrl: " + pushUrl);

            if (StringUtils.isBlank(pushUrl)) {
                ToastUtil.makeToast(this, "can not find the live stream", 1000);
                return;
            }

            // 如果直播流已过期，则更新直播流信息，等待完成后再开始推流
            if (LiveStreamUtil.isUrlExpired(pushUrl)) {
                chatroomViewModel.updateLiveStreamInfo(c);
                return;
            }

            // 开始直播推流
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

        // 点击消息列表的空白处时，隐藏所有展开的输入组件，只保留基础输入组件
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
     * 发送文本消息
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
     * 删除消息
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

        // 绑定播放器视图
        binding.chatroomLiveStreamPullView.setPlayer(player);

        // 绑定直播拉流地址
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

        // 开始加载直播流
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
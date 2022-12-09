/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSONObject;
import cn.com.hsbc.hsbcchina.cert.R;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.InstantReplyFaceContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.InstantReplyFaceInfo;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.QuotedInfo;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.ack.AckBatchMessageRead;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.ack.AckMessageRead;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.ack.AckMessageRecalled;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.cmd.CmdRecall;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.cmd.CmdRecallInstantReplyFace;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageDeleteStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageReadStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageRecallStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageSendStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.model.User;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import cn.com.hsbc.hsbcchina.cert.databinding.ActivityChatBinding;
import com.hsbcd.mpaastest.kotlin.samples.enums.CustomCommandMessageTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.enums.CustomMessageTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.model.MediaSendingProgress;
import com.hsbcd.mpaastest.kotlin.samples.model.VisitingCardData;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.config.face.FaceIconEmoji;
import com.alipay.fc.ccmimplus.sdk.core.constants.Constants;
import com.alipay.fc.ccmimplus.sdk.core.enums.ErrorCodeEnum;
import com.alipay.fc.ccmimplus.sdk.core.message.MessageConverter;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.DateUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.MessageListAdapter;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.command.InputCustomCommandActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.emoji.EmojiInputView;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.location.LocationActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.richtext.RichTextInputActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.url.InputUrlActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.voice.VoiceRecorder;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting.GroupChatSettingActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting.SingleChatSettingActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.SelectMessageViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.SessionViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.SendVisitingCardActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.CopyUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.FileUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.MessageUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
//import com.amap.api.location.AMapLocation;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 聊天消息页
 *
 * @author liyalong
 * @version ChatActivity.java, v 0.1 2022年08月02日 14:45 liyalong
 */
public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;

    private MessageListAdapter messageListAdapter;

    private MessageViewModel messageViewModel;

    private SessionViewModel sessionViewModel;

    private SelectMessageViewModel selectItemViewModel;

    private Conversation c;

    private CustomOnScrollListener customOnScrollListener;

    private boolean refreshHistoryMessage = false;

    private Handler handler = new Handler();

    private Uri currentMediaUri;

    private ActivityResultLauncher takePictureLauncher;

    private ActivityResultLauncher captureVideoLauncher;

    private VoiceRecorder voiceRecorder = new VoiceRecorder(this);

    /**
     * 是否进入消息多选模式
     */
    private boolean multiSelectMode = false;

    /**
     * 进入页面时是否需要拉取历史消息
     */
    private boolean needRefreshMessage = true;

    /**
     * 当前选中待回复的引用消息
     */
    private Message currentQuotedMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setCurrentSession();

        // 绑定组件动作
        bindAction();

        // 绑定各类消息输入组件
        bindInputText();
        bindEmojiGrid();
        bindRecordVoice();
        bindPickMedia();
        bindTakePicture();
        bindCaptureVideo();
        bindPickFile();
        bindLocation();
        bindRichText();
        bindInputUrl();
        bindVisitingCard();
        bindCard();
        bindCustomCommand();

        // 绑定聊天消息数据层
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        bindMessageViewModel();

        // 绑定会话数据层
        sessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        bindSessionViewModel();

        // 绑定选择消息数据
        selectItemViewModel = new ViewModelProvider(this).get(SelectMessageViewModel.class);
        bindSelectItemViewModel();

        // 初始化消息列表
        initMessageList();
    }

    /**
     * 每次进入页面时
     */
    @Override
    protected void onStart() {
        super.onStart();

        // 注：每次进入页面前，需要先设置会话
        setCurrentSession();
        
        // 设置页面标题，从设置页退回到消息页时，需要刷新一次
        setTitle();

        // 如果是app从后台唤起/从设置页返回等情况，需要重新拉取历史消息
        if (needRefreshMessage) {
            refreshHistoryMessage();
        }
        // 如果是从拍照/拍视频/发文件/发定位等输入类组件页返回，会把新发送的消息插到现有消息列表的末尾，不需要重新拉取历史消息
        else {
            needRefreshMessage = true;
        }
    }

    /**
     * 每次退出页面时
     */
    @Override
    protected void onStop() {
        super.onStop();

        // 清除当前会话
        // 注：如果只是被其他activity覆盖而不是退出(例如打开拍照页面)，此时finished=false，不能清除
        if (isFinishing()) {
            AlipayCcmIMClient.getInstance().getConversationManager().clearCurrentConversation();
        }
    }

    private void setCurrentSession() {
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c == null) {
            Log.e(LoggerName.UI, "unset session，cannot go to setting");
            super.onBackPressed();
            return;
        }
        this.c = c;
    }

    /**
     * 是否进入消息多选模式
     *
     * @return
     */
    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    /**
     * 设置下次进入当前页面时是否需要拉取历史消息
     * 例如跳转到图片/视频/文件预览页、定位地图页等页面之后，再返回当前页面时，不需要重新拉取历史消息
     *
     * @param needRefreshMessage
     */
    public void setNeedRefreshMessage(boolean needRefreshMessage) {
        this.needRefreshMessage = needRefreshMessage;
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.onBackPressed());

        // 跳转到会话设置页(密聊不需要)
        if (c.isSecretChat()) {
            binding.chatSettingView.setVisibility(View.GONE);
        } else {
            binding.chatSettingView.setVisibility(View.VISIBLE);
            binding.chatSettingView.setOnClickListener(v -> {
                // 查询会话信息，等待返回查询结果后再打开设置页
                sessionViewModel.querySingleConversation(c.getCid());
            });
        }

        // 消息输入视图的切换动作
        bindInputViewSwitch();

        // 消息多选的批量操作
        bindMultiSelect();

        // 引用消息的操作
        bindQuotedMessage();
    }

    private void bindInputViewSwitch() {
        // 输入文本/录音切换
        binding.switchToMic.setOnClickListener(v -> {
            hideSoftKeyboard();
            hideInputMoreLayout();

            binding.switchToMic.setVisibility(View.GONE);
            binding.inputText.setVisibility(View.GONE);

            binding.switchMicToText.setVisibility(View.VISIBLE);
            binding.recordVoice.setVisibility(View.VISIBLE);
        });

        binding.switchMicToText.setOnClickListener(v -> {
            binding.switchToMic.setVisibility(View.VISIBLE);
            binding.inputText.setVisibility(View.VISIBLE);

            binding.switchMicToText.setVisibility(View.GONE);
            binding.recordVoice.setVisibility(View.GONE);
        });

        // 点击文本输入框时，隐藏表情和多媒体输入视图
        binding.inputText.setOnClickListener(v -> {
            hideInputMoreLayout();

            binding.switchToEmoji.setVisibility(View.VISIBLE);
            binding.switchEmojiToText.setVisibility(View.GONE);

            binding.switchToMultimedia.setVisibility(View.VISIBLE);
            binding.switchMultimediaToText.setVisibility(View.GONE);
        });

        // 表情输入视图展开/隐藏
        binding.switchToEmoji.setOnClickListener(v -> {
            showInputEmojiView();

            binding.switchToEmoji.setVisibility(View.GONE);
            binding.switchEmojiToText.setVisibility(View.VISIBLE);
        });

        binding.switchEmojiToText.setOnClickListener(v -> {
            hideInputMoreLayout();

            binding.switchToEmoji.setVisibility(View.VISIBLE);
            binding.switchEmojiToText.setVisibility(View.GONE);
        });

        // 多媒体输入视图展开/隐藏
        binding.switchToMultimedia.setOnClickListener(v -> {
            showInputMultimediaLayout();

            binding.switchToMultimedia.setVisibility(View.GONE);
            binding.switchMultimediaToText.setVisibility(View.VISIBLE);
        });

        binding.switchMultimediaToText.setOnClickListener(v -> {
            hideInputMoreLayout();

            binding.switchToMultimedia.setVisibility(View.VISIBLE);
            binding.switchMultimediaToText.setVisibility(View.GONE);
        });

        // 多媒体输入视图点击翻页
        binding.inputMultimediaPage1.setOnClickListener(v -> binding.inputMultimediaFlipper.goToNextPage());
        binding.inputMultimediaPage2.setOnClickListener(v -> binding.inputMultimediaFlipper.goToPreviousPage());
    }

    private void bindMultiSelect() {
        // 多选消息菜单的取消按钮
        binding.cancelMultiSelect.setOnClickListener(v -> {
            quitMultiSelectMode();
        });

        // 逐一转发
        binding.batchTransferSingle.setOnClickListener(v -> {
            Collection<Message> selectedMessages = selectItemViewModel.getSelectedItems();
            doTransferMessage(selectedMessages, false);
            quitMultiSelectMode();
        });

        // 合并转发
        binding.batchTransferMerge.setOnClickListener(v -> {
            Collection<Message> selectedMessages = selectItemViewModel.getSelectedItems();
            doTransferMessage(selectedMessages, true);
            quitMultiSelectMode();
        });

        // 批量收藏
        binding.batchFav.setOnClickListener(v -> {
            List<String> selectedMessageIds = selectItemViewModel.getSelectedItems().stream().map(
                    Message::getId).collect(Collectors.toList());
            addFavoriteMessages(selectedMessageIds);
            quitMultiSelectMode();
        });

        // 批量删除
        binding.batchDelete.setOnClickListener(v -> {
            Collection<Message> selectedMessages = selectItemViewModel.getSelectedItems();

            messageListAdapter.batchRemove(selectedMessages);
            messageViewModel.batchDeleteMessage(c, Lists.newArrayList(selectedMessages));

            quitMultiSelectMode();
        });
    }

    private void quitMultiSelectMode() {
        binding.goBack.setVisibility(View.VISIBLE);
        binding.cancelMultiSelect.setVisibility(View.GONE);

        hideMultiSelectMenu();
        showInputLayout();

        multiSelectMode = false;

        // 重新渲染消息列表，隐藏消息复选框
        messageListAdapter.notifyDataSetChanged();
    }

    private void bindQuotedMessage() {
        binding.quotedMessageClose.setOnClickListener(v -> {
            binding.quotedMessageLayout.setVisibility(View.GONE);
            currentQuotedMessage = null;
        });
    }

    private void bindInputText() {
        binding.inputText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEND
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                // 注：点击发送消息后会先后触发ACTION_UP和ACTION_DOWN，这里只处理ACTION_UP，避免重复发送两次消息
                if (event == null || event.getAction() == KeyEvent.ACTION_UP) {
                    sendTextMessage();
                }
            }
            return true;
        });

        binding.inputText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.inputEmojiView.setSendEmojiButtonEnabled(charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 如果输入的是@字符，打开选择要@的人对话框，仅对群聊会话有效
                if (c.isGroupConversation()) {
                    int position = binding.inputText.getSelectionStart();
                    if (position > 0) {
                        CharSequence ch = editable.subSequence(position - 1, position);
                        if (StringUtils.equals(ch, "@")) {
                            renderSelectAtUserDialog(editable, position);
                        }
                    }
                }
            }
        });
    }

    private void renderSelectAtUserDialog(Editable editable, int position) {
        SelectAtUserDialog dialog = new SelectAtUserDialog(ChatActivity.this, c, userInfos -> {
            Map<String, String> atUserMap = userInfos.stream().collect(Collectors.toMap(
                    UserInfoVO::getUserId, UserInfoVO::getUserName, (k1, k2) -> k1, LinkedHashMap::new));

            // 删除最后输入的"@"字符
            if (position > 0) {
                editable.delete(position - 1, position);
            }

            // 再追加@用户信息
            appendAtUsers(atUserMap);

            // 自动弹出软键盘
            showSoftKeyboard();
        });

        dialog.show(((AppCompatActivity) ChatActivity.this).getSupportFragmentManager(), "");
    }

    private void bindEmojiGrid() {
        // 点击表情后添加到文本输入框中
        binding.inputEmojiView.getAdapter().setOnClickItemListener(e -> appendEmoji(e));

        binding.inputEmojiView.setOnClickButtonListener(new EmojiInputView.OnClickButtonListener() {
            @Override
            public void onClickSendEmoji() {
                sendTextMessage();
            }

            @Override
            public void onClickDeleteEmoji() {
                deleteEmoji();
            }
        });
    }

    private void bindRecordVoice() {
        ActivityResultLauncher permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), result -> {
                    if (!result) {
                        ToastUtil.makeToast(ChatActivity.this, "无录音权限", 1000);
                        return;
                    }

                    // 开始录音
                    startRecordVoice();
                });

        binding.recordVoice.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    // 先请求权限
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    // 显式设置按钮状态为pressed，渲染按下状态的样式
                    v.setPressed(true);

                    // 如果当前触摸位置在按钮内部
                    if (event.getY() >= binding.recordVoice.getY()) {
                        voiceRecorder.getDialogManager().showRecording();
                    }
                    // 如果当前触摸位置在按钮上方
                    else {
                        voiceRecorder.getDialogManager().showReadyToCancel();
                    }
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    v.setPressed(false);

                    boolean tooShort = (event.getEventTime() - event.getDownTime()) < 1000;
                    boolean canceled = event.getY() < binding.recordVoice.getY();
                    boolean needSend = !tooShort && !canceled;
                    Log.i(LoggerName.UI,
                            String.format("voice recording finish, tooShort=%s, canceled=%s, needSend=%s", tooShort,
                                    canceled, needSend));

                    stopRecordVoice(needSend);

                    if (tooShort) {
                        voiceRecorder.getDialogManager().showTooShort();
                    }
                    break;
                }
                default:
                    break;
            }

            return false;
        });
    }

    private void startRecordVoice() {
        try {
            voiceRecorder.startRecord();
        } catch (Exception e) {
            Log.e(LoggerName.UI, "record voice error", e);
            ToastUtil.makeToast(this, e.getMessage(), 3000);
        }
    }

    private void stopRecordVoice(boolean needSend) {
        try {
            voiceRecorder.stopRecord();

            if (needSend) {
                File file = voiceRecorder.getVoiceFile();
                Log.i(LoggerName.UI, "voice file path is " + file.getAbsolutePath() + ", size is " + file.length());
                messageViewModel.sendVoiceMessage(file);
            }
        } catch (Exception e) {
            Log.e(LoggerName.UI, "stop record voice error", e);
            ToastUtil.makeToast(this, e.getMessage(), 3000);
        }
    }

    private void bindPickMedia() {
        ActivityResultLauncher pickMediaLauncher = registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(), result -> {
                    if (CollectionUtils.isEmpty(result)) {
                        return;
                    }

                    Log.i(LoggerName.UI, "pick media count: " + result.size());

                    for (Uri uri : result) {
                        Log.i(LoggerName.UI, "pick media uri: " + uri);

                        if (FileUtil.isImage(uri)) {
                            onTakePictureResult(uri);
                        } else if (FileUtil.isVideo(uri)) {
                            onCaptureVideoResult(uri);
                        }
                    }
                });

        binding.inputAlbum.setOnClickListener(v -> {
            pickMediaLauncher.launch("image/*;video/*");
            needRefreshMessage = false;
        });
    }

    private void bindTakePicture() {
        ActivityResultLauncher permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), result -> {
                    if (!result) {
                        ToastUtil.makeToast(ChatActivity.this, "无相机权限", 1000);
                        return;
                    }

                    doTakePicture();
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), result -> {
                    if (result) {
                        Log.i(LoggerName.UI, "take picture uri: " + currentMediaUri);
                        if (currentMediaUri == null) {
                            ToastUtil.makeToast(this, "拍照失败", 1000);
                        } else {
                            onTakePictureResult(currentMediaUri);
                        }
                    }

                    currentMediaUri = null;
                });

        binding.inputCamera.setOnClickListener(v -> permissionLauncher.launch(Manifest.permission.CAMERA));
    }

    private void doTakePicture() {
        currentMediaUri = FileUtil.generateFileUri(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        takePictureLauncher.launch(currentMediaUri);
        needRefreshMessage = false;
    }

    private void onTakePictureResult(Uri uri) {
        File imageFile = FileUtil.uriToFile(uri, this, false);
        messageViewModel.sendImageMessage(imageFile);
    }

    private void bindCaptureVideo() {
        ActivityResultLauncher permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), result -> {
                    if (!result) {
                        ToastUtil.makeToast(ChatActivity.this, "无视频拍摄权限", 1000);
                        return;
                    }

                    doCaptureVideo();
                });

        captureVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.CaptureVideo() {
                    /**
                     * 重写该方法，设置视频拍摄参数
                     */
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, @NonNull Uri input) {
                        Intent intent = super.createIntent(context, input);
                        // 最大时长，单位秒
                        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
                        // 画质，0-最低，1-最高，暂不支持设置中间值
                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                        return intent;
                    }
                }, result -> {
                    if (result) {
                        Log.i(LoggerName.UI, "capture video uri: " + currentMediaUri);
                        if (currentMediaUri == null) {
                            ToastUtil.makeToast(this, "视频拍摄失败", 1000);
                        } else {
                            onCaptureVideoResult(currentMediaUri);
                        }
                    }

                    currentMediaUri = null;
                });

        binding.inputRtc.setOnClickListener(v -> permissionLauncher.launch(Manifest.permission.CAMERA));
    }

    private void doCaptureVideo() {
        currentMediaUri = FileUtil.generateFileUri(this, MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Video.Media.INTERNAL_CONTENT_URI);
        captureVideoLauncher.launch(currentMediaUri);
        needRefreshMessage = false;
    }

    private void onCaptureVideoResult(Uri uri) {
        File videoFile = FileUtil.uriToVideoFile(uri, "mp4", this);
        Log.i(LoggerName.UI, "captured video file length: " + videoFile.length());
        messageViewModel.sendVideoMessage(videoFile);
    }

    private void bindPickFile() {
        ActivityResultLauncher pickFileLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(), result -> {
                    if (result == null) {
                        return;
                    }

                    Log.i(LoggerName.UI, "pick file: " + result);
                    File file = FileUtil.uriToFile(result, this, true);
                    messageViewModel.sendFileMessage(file);
                });

        binding.inputFile.setOnClickListener(v -> {
            pickFileLauncher.launch("*/*");
            needRefreshMessage = false;
        });
    }

    private void bindLocation() {
        ActivityResultLauncher locationActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getData() == null) {
                        return;
                    }

                    // 获取定位数据
                    String snapshotFilePath = result.getData().getStringExtra("snapshotFilePath");
                    File snapshotFile = new File(snapshotFilePath);
//                    AMapLocation location = result.getData().getParcelableExtra("location");

                    // 发送定位消息
//                    messageViewModel.sendLocationMessage(snapshotFile, location);
                }
        );

        ActivityResultLauncher permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    if (result.isEmpty()) {
                        ToastUtil.makeToast(ChatActivity.this, "无定位权限", 1000);
                        return;
                    }

                    // 打开定位页面
                    Intent intent = new Intent(this, LocationActivity.class);
                    locationActivityLauncher.launch(intent);
                    needRefreshMessage = false;
                });

        binding.inputLocation.setOnClickListener(v -> {
            permissionLauncher.launch(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS});
        });
    }

    private void bindRichText() {
        ActivityResultLauncher launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getData() == null) {
                        return;
                    }

                    String text = result.getData().getStringExtra("text");
                    messageViewModel.sendRichTextMessage(text);
                }
        );
        binding.inputRichtext.setOnClickListener(v -> {
            Intent intent = new Intent(this, RichTextInputActivity.class);
            launcher.launch(intent);
            needRefreshMessage = false;
        });
    }

    private void bindInputUrl() {
        ActivityResultLauncher launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getData() == null) {
                        return;
                    }

                    String url = result.getData().getStringExtra("url");
                    messageViewModel.sendUrlMessage("", url);
                }
        );

        binding.inputUrl.setOnClickListener(v -> {
            Intent intent = new Intent(this, InputUrlActivity.class);
            launcher.launch(intent);
            needRefreshMessage = false;
        });
    }

    private void bindVisitingCard() {
        ActivityResultLauncher launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getData() == null) {
                        return;
                    }

                    VisitingCardData data = result.getData().getParcelableExtra("visitingCardData");
                    messageViewModel.sendCustomMessage(CustomMessageTypeEnum.VISITING_CARD.name(), "1.0.0",
                            JSONObject.toJSONString(data));
                }
        );

        binding.inputVisitingCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, SendVisitingCardActivity.class);
            launcher.launch(intent);
            needRefreshMessage = false;
        });
    }

    private void bindCard() {
        binding.inputCard.setOnClickListener(v -> sendSimpleCardMsg());
    }

    private void sendSimpleCardMsg() {
        messageViewModel.sendSimpleCardMessage();
    }

    private void bindCustomCommand() {
        ActivityResultLauncher launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getData() == null) {
                        return;
                    }

                    String command = result.getData().getStringExtra("command");
                    messageViewModel.sendCustomCommandMessage(CustomCommandMessageTypeEnum.PUSH.name(), "1.0", command);
                }
        );

        binding.inputCustomCommand.setOnClickListener(v -> {
            Intent intent = new Intent(this, InputCustomCommandActivity.class);
            launcher.launch(intent);
            needRefreshMessage = false;
        });
    }

    private void showInputLayout() {
        binding.inputLayout.setVisibility(View.VISIBLE);
    }

    private void showInputEmojiView() {
        hideSoftKeyboard();

        binding.inputEmojiView.setVisibility(View.VISIBLE);
        binding.inputMultimediaFlipper.setVisibility(View.GONE);
    }

    private void showInputMultimediaLayout() {
        hideSoftKeyboard();

        binding.inputEmojiView.setVisibility(View.GONE);
        binding.inputMultimediaFlipper.setVisibility(View.VISIBLE);
    }

    private void showMultiSelectMenu() {
        binding.multiSelectMenuLayout.setVisibility(View.VISIBLE);
    }

    private void showSoftKeyboard() {
        // TODO 不生效，软键盘弹不出来
        binding.inputText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.inputText, InputMethodManager.SHOW_IMPLICIT);
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void hideInputLayout() {
        hideSoftKeyboard();
        binding.inputLayout.setVisibility(View.GONE);
    }

    private void hideInputMoreLayout() {
        binding.inputEmojiView.setVisibility(View.GONE);
        binding.inputMultimediaFlipper.setVisibility(View.GONE);
    }

    private void hideAllInputLayout() {
        hideInputLayout();
        hideInputMoreLayout();
    }

    private void hideMultiSelectMenu() {
        binding.multiSelectMenuLayout.setVisibility(View.GONE);
    }

    private void initMessageList() {
        this.messageListAdapter = new MessageListAdapter(this, c.isSecretChat());
        binding.messageListView.setAdapter(messageListAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        binding.messageListView.setLayoutManager(linearLayoutManager);

        binding.messageListView.setHasFixedSize(true);
        //binding.messageListView.setAnimation(null);

        customOnScrollListener = new CustomOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
            }

            @Override
            public void onRefresh() {
                // 加载上一页(更早的)消息
                doQueryHistoryMessages(false);
            }

            @Override
            public void onNoNextPage() {
            }
        };

        binding.messageListView.addOnScrollListener(customOnScrollListener);

        // 点击消息列表的空白处时，隐藏所有展开的输入组件，只保留基础输入组件
        binding.messageListView.setOnClickListListener(() -> {
            hideSoftKeyboard();
            hideInputMoreLayout();

            binding.switchToEmoji.setVisibility(View.VISIBLE);
            binding.switchEmojiToText.setVisibility(View.GONE);

            binding.switchToMultimedia.setVisibility(View.VISIBLE);
            binding.switchMultimediaToText.setVisibility(View.GONE);
        });
    }

    private void refreshHistoryMessage() {
        refreshHistoryMessage = true;
        customOnScrollListener.reset();
        doQueryHistoryMessages(true);
    }

    private void doQueryHistoryMessages(boolean init) {
        if (c.isSecretChat()) {
            messageViewModel.querySecretChatHistoryMessages(c, init);
        } else {
            messageViewModel.queryHistoryMessages(c, init);
        }
    }

    private void setTitle() {
        // 密聊会话标题为马赛克图像
        if (c.isSecretChat()) {
            binding.titleLabel.setText(StringUtils.EMPTY);
            binding.titleLabel.setBackground(getDrawable(R.drawable.ic_secret_chat_title));
            return;
        }

        // 页面标题设置为会话名称
        if (StringUtils.isNotBlank(c.getSessionName())) {
            binding.titleLabel.setText(c.getSessionName());
        } else {
            binding.titleLabel.setText(c.getConversationName());
        }

        // 展示免打扰图标
        if (c.isShieldMode()) {
            Drawable icon = this.getDrawable(R.drawable.ic_no_disturb);
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            binding.titleLabel.setCompoundDrawables(null, null, icon, null);
            binding.titleLabel.setCompoundDrawablePadding(10);
        }
    }

    /**
     * 发送文本消息
     */
    public void sendTextMessage() {
        String textMsg = binding.inputText.getText().toString();
        if (StringUtils.isBlank(textMsg)) {
            return;
        }

        List<String> atUserIds = binding.inputText.getAtUserIds();
        if (currentQuotedMessage == null) {
            messageViewModel.sendTextMessage(textMsg, atUserIds);
        } else {
            messageViewModel.sendQuoteReplyTextMessage(c, currentQuotedMessage, textMsg, atUserIds);
        }
    }

    public void reSendMessage(Message message) {
        // 重发前先从消息列表中删除原来的消息
        messageListAdapter.remove(message);

        messageViewModel.reSendMessage(message);
    }

    private void bindMessageViewModel() {
        // 拉取到历史消息列表后的动作
        messageViewModel.getMessageListResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            if (CollectionUtils.isEmpty(result.getData())) {
                // 清空原有消息列表，兼容在设置页清空聊天记录后返回消息页的情况
                if (refreshHistoryMessage) {
                    messageListAdapter.clearAll();
                    messageListAdapter.notifyDataSetChanged();
                }

                if (messageListAdapter.getItemCount() > 0) {
                    ToastUtil.makeToast(ChatActivity.this, "No more msg", 1000);
                }
                return;
            }

            // 过滤掉已删除消息
            List<Message> messages = result.getData().stream()
                    .filter(m -> m.getDeleteStatus() == MessageDeleteStatusEnum.NORMAL)
                    .collect(Collectors.toList());

            // 如果是重新拉取历史消息，则清空原有消息
            if (refreshHistoryMessage) {
                messageListAdapter.clearAll();
            }

            // 新拉取到的消息插到消息列表的头部，并重新渲染消息列表
            messageListAdapter.addAll(0, messages);
            messageListAdapter.notifyDataSetChanged();

            // 消息列表滚动到指定位置
            if (refreshHistoryMessage) {
                binding.messageListView.scrollToBottom();
            } else {
                binding.messageListView.scrollToTop(messages.size());
            }

            refreshHistoryMessage = false;
        });

        // 收到在线推送消息/指令后的动作
        messageViewModel.getReceiveMessageResult().observe(this, result -> {
            Message message = result.getMessage();

            switch (result.getType()) {
                // 普通消息
                case Normal: {
                    // 插入消息列表
                    insertMessage(message);
                    
                    // 消息设置为已读
                    messageViewModel.setMessageStatusRead(c, message);
                    break;
                }
                // TODO 消息已达通知
                // 后续可在这里处理消息内容质检结果等逻辑
                case NotifyMessageReach: {
                    break;
                }
                // 消息已读通知
                case AckMessageRead: {
                    markMessageRead(message);
                    break;
                }
                // 批量消息已读通知
                case AckBatchMessageRead: {
                    batchMarkMessageRead(message);
                    break;
                }
                // 消息撤回ack
                case AckMessageRecalled:
                    // 消息撤回通知
                case CmdRecall: {
                    markMessageRecalled(message);
                    break;
                }
                // TODO 消息一键全部已读通知
                case CmdSetAllMsgRead:
                    break;
                // 引用回复消息
                case QuoteReplyInfo: {
                    // 插入消息列表
                    insertMessage(message);

                    // 消息设置为已读
                    messageViewModel.setMessageStatusRead(c, message);

                    // 重新渲染话题原始消息，更新引用回复计数
                    if (message.getQuoteReplyInfo() != null) {
                        refreshTopicOriginalMessage(message, message.getQuoteReplyInfo().getTopicId());
                    }
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

                    // 清理引用消息
                    binding.quotedMessageLayout.setVisibility(View.GONE);
                    currentQuotedMessage = null;
                    break;
                }
                // 消息发送中(仅用于需要更新发送进度的消息，例如图片/视频等)
                case IN_PROGRESS: {
                    updateMessageSendingProgress(message, result.getProgress());
                    break;
                }
                // 消息发送成功
                case SUCCESS: {
                    updateMessageSendStatusToSuccess(message);

                    // 重新渲染话题原始消息，更新引用回复计数
                    if (StringUtils.isNotBlank(result.getTopicId())) {
                        refreshTopicOriginalMessage(message, result.getTopicId());
                    }
                    break;
                }
                // 消息发送失败
                case FAIL: {
                    ToastUtil.makeToast(this, result.getErrorMessage(), 3000);
                    updateMessageSendStatusToError(message, result.getErrorCode(), result.getErrorMessage());
                    break;
                }
                default:
                    break;
            }
        });

        // 发送快捷回复表情后的动作
        messageViewModel.getSendInstantReplyEmojiResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // 重新渲染该消息，刷新快捷回复表情列表
            Message message = result.getData();
            refreshInstantReplyMessage(message.getTntInstId(), message.getSid(), Long.valueOf(message.getId()),
                    message.getInstantReplyFaceInfos());
        });

        // 收到快捷回复表情通知后的动作
        messageViewModel.getNotifyInstantReplyEmojiResult().observe(this, result -> {
            Message message = result.first;
            List<InstantReplyFaceInfo> faceInfos = result.second;

            // 获取快捷回复表情引用的原始消息id
            String quoteMessageId = null;
            // 收到新的快捷回复表情
            if (message.getContent() instanceof InstantReplyFaceContent) {
                quoteMessageId = ((InstantReplyFaceContent) message.getContent()).getQuoteMessageId();
            }
            // 收到快捷回复表情撤回通知
            else if (message.getContent() instanceof CmdRecallInstantReplyFace) {
                quoteMessageId = ((CmdRecallInstantReplyFace) message.getContent()).getQuoteMessageId();
            }

            // 刷新原始消息的快捷回复表情列表
            refreshInstantReplyMessage(message.getTntInstId(), message.getSid(), Long.valueOf(quoteMessageId),
                    faceInfos);
        });
    }

    private void insertMessage(Message message) {
        messageListAdapter.addOne(message);
        messageListAdapter.notifyItemInserted(messageListAdapter.getItemCount() - 1);

        binding.messageListView.scrollToBottom();
    }

    private void refreshMessage(Message message) {
        int index = messageListAdapter.indexOfByMsgId(message.getTntInstId(), message.getSid(),
                Long.valueOf(message.getId()));
        if (index != -1) {
            messageListAdapter.notifyItemChanged(index);
        }
    }

    /**
     * 消息列表跳转到指定消息的位置
     *
     * @param message
     */
    public void jumpToMessage(Message message) {
        int index = messageListAdapter.indexOfByMsgId(message.getTntInstId(), message.getSid(),
                Long.valueOf(message.getId()));
        if (index != -1) {
            // 滚动到指定消息的前面几条消息的位置，尽量使指定消息处于屏幕居中位置
            binding.messageListView.scrollTo((index < 3) ? 0 : (index - 3));
        }
    }

    private void markMessageRead(Message message) {
        AckMessageRead content = (AckMessageRead) message.getContent();

        Log.i("message",
                DateUtils.formatNow() + " received ack message (" + content.getCid() + "," + content.getMessageId() + "," + content.getLocalId() + ")");

        doMarkMessageRead(message, content.getMessageId());
    }

    private void batchMarkMessageRead(Message message) {
        AckBatchMessageRead content = (AckBatchMessageRead) message.getContent();

        Log.i("message",
                DateUtils.formatNow() + " received batch read ack message (" + content.getCid() + "," + message.getLocalId() + ")");

        List<Long> msgIdList = content.getMessageIds();
        if (CollectionUtils.isEmpty(msgIdList)) {
            return;
        }

        for (Long msgId : msgIdList) {
            doMarkMessageRead(message, msgId);
        }
    }

    private void doMarkMessageRead(Message message, long messageId) {
        int index = messageListAdapter.indexOfByMsgId(message.getTntInstId(), message.getSid(), messageId);
        if (index == -1) {
            return;
        }

        // 定位到列表中的指定消息
        Message<?> msgInList = messageListAdapter.getMessage(index);

        // 更新消息已读状态
        msgInList.getExtInfo().putAll(message.getExtInfo());
        msgInList.setReadStatus(MessageReadStatusEnum.READ);

        // 更新@用户的已读状态
        if (CollectionUtils.isNotEmpty(msgInList.getAtUsers())) {
            String senderUserId = message.getFrom().getUid();
            Optional<User> atUser = msgInList.getAtUsers().stream().filter(
                    u -> StringUtils.equals(u.getUserId(), senderUserId)).findFirst();

            if (atUser.isPresent()) {
                atUser.get().getExtInfo().put(Constants.AT_USER_READ_STATUS, MessageReadStatusEnum.READ.getCode());
            }
        }

        messageListAdapter.notifyItemChanged(index);
    }

    private void markMessageRecalled(Message message) {
        String localId = null;

        // 自己撤回消息时收到ack
        if (message.getContent() instanceof AckMessageRecalled) {
            AckMessageRecalled content = (AckMessageRecalled) message.getContent();
            localId = content.getLocalId();
        }
        // 别人撤回消息时收到通知
        else if (message.getContent() instanceof CmdRecall) {
            CmdRecall content = (CmdRecall) message.getContent();
            localId = content.getOriginalMessageLocalId();
        }

        int index = messageListAdapter.indexOf(message.getTntInstId(), message.getSid(), localId);
        if (index == -1) {
            return;
        }

        Message msgInChat = messageListAdapter.getMessage(index);
        msgInChat.setRecallStatus(MessageRecallStatusEnum.RECALLED);
        messageListAdapter.notifyItemChanged(index);
    }

    private void refreshInstantReplyMessage(String tntInstId, String cid, long quoteMessageId,
                                            List<InstantReplyFaceInfo> faceInfos) {
        int index = messageListAdapter.indexOfByMsgId(tntInstId, cid, quoteMessageId);
        if (index == -1) {
            return;
        }

        Message quotedMessage = messageListAdapter.getMessage(index);
        quotedMessage.setInstantReplyFaceInfos(faceInfos);

        messageListAdapter.notifyItemChanged(index);
    }

    private void refreshTopicOriginalMessage(Message message, String topicId) {
        int index = messageListAdapter.indexOfByMsgId(message.getTntInstId(), message.getSid(), Long.valueOf(topicId));
        if (index == -1) {
            return;
        }

        Message originalMessage = messageListAdapter.getMessage(index);

        QuotedInfo quotedInfo = originalMessage.getQuotedInfo();
        if (quotedInfo == null) {
            quotedInfo = new QuotedInfo();
            quotedInfo.setTopicId(topicId);
            originalMessage.setQuotedInfo(quotedInfo);
        }

        quotedInfo.setReplyCounter(quotedInfo.getReplyCounter() + 1);
        messageListAdapter.notifyItemChanged(index);

    }

    private void updateMessageSendingProgress(Message message, MediaSendingProgress progress) {
        int index = messageListAdapter.indexOf(message.getTntInstId(), message.getSid(), message.getLocalId());
        if (index != -1) {
            Log.d(LoggerName.UI, "update sending progress: " + index);
            messageListAdapter.notifyItemChanged(index, progress);
        }
    }

    private void updateMessageSendStatusToSuccess(Message message) {
        // 更新当前会话的最近一条消息id，确保如果需要重新拉取历史消息，一定是从最近一条消息开始拉
        if (StringUtils.isNotBlank(message.getId())) {
            c.setLastMsgId(Long.valueOf(message.getId()));
        }

        // 重新渲染该消息，更新发送状态/已读状态等状态信息
        refreshMessage(message);
    }

    private void updateMessageSendStatusToError(Message message, String errorCode, String errorMessage) {
        if (message == null) {
            return;
        }

        int index = messageListAdapter.indexOf(message.getTntInstId(), message.getSid(), message.getLocalId());
        if (index == -1) {
            return;
        }

        // 如果是用户自行取消，则移除当前消息
        if (message.getSendStatus() == MessageSendStatusEnum.CANCEL || StringUtils.equalsIgnoreCase(errorCode,
                ErrorCodeEnum.USER_CANCEL_UPLOAD.getCode())) {
            messageListAdapter.removeItem(index);
        }
        // 其它错误，重新渲染消息，展示失败状态(重发按钮)
        else {
            messageListAdapter.notifyItemChanged(index);
        }
    }

    private void bindSessionViewModel() {
        // 会话查询结果通知
        sessionViewModel.getConversationResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // 设置当前会话，确保每次进入设置页时拿到的数据都是最新的
            Conversation c = result.getData();
            AlipayCcmIMClient.getInstance().getConversationManager().setCurrentConversation(result.getData());

            // 打开设置页
            if (c.isSingle()) {
                Intent intent = new Intent(this, SingleChatSettingActivity.class);
                this.startActivity(intent);
            } else if (c.isGroupConversation()) {
                Intent intent = new Intent(this, GroupChatSettingActivity.class);
                this.startActivity(intent);
            }
        });
    }

    private void bindSelectItemViewModel() {
        selectItemViewModel.getSelectedItemResult().observe(this, result -> {
            // TODO 暂不需要实时更新已选择消息数量
        });
    }

    /**
     * 复制消息内容
     *
     * @param message
     */
    public void copyMessage(Message message) {
        String text = MessageUtil.getContentTextForCopy(message);

        if (StringUtils.isBlank(text)) {
            ToastUtil.makeToast(this, "暂不支持复制该消息内容", 1000);
            return;
        }

        CopyUtil.copyToClipboard(this, text);
    }

    /**
     * 转发单条消息到其他会话
     *
     * @param message
     */
    public void transferMessage(Message message) {
        doTransferMessage(Arrays.asList(message), false);
    }

    private void doTransferMessage(Collection<Message> messages, boolean isMerge) {
        String messageIds = StringUtils.join(messages.stream().map(m -> m.getId()).collect(Collectors.toList()), ",");

        Intent intent = new Intent(this, TransferMessageActivity.class);
        intent.putExtra(TransferMessageActivity.SESSION_ID, c.getCid());
        intent.putExtra(TransferMessageActivity.MESSAGE_ID, messageIds);
        intent.putExtra(TransferMessageActivity.IS_MERGE, isMerge);
        this.startActivity(intent);
        needRefreshMessage = false;
    }

    /**
     * 撤回消息
     *
     * @param message
     */
    public void recallMessage(Message message) {
        // 发送撤回指令
        messageViewModel.recallMessage(message);

        // 立即把本地消息状态置为撤回中，并重新渲染消息
        int index = messageListAdapter.indexOfByMsgId(message.getTntInstId(), message.getSid(),
                Long.valueOf(message.getId()));
        if (index != -1) {
            message.setRecallStatus(MessageRecallStatusEnum.RECALLING);
            messageListAdapter.notifyItemChanged(index);
        }
    }

    /**
     * 重新编辑已撤回消息
     *
     * @param message
     */
    public void reEditRecalledMessage(Message message) {
        String recalledText = MessageConverter.getTextValue(message);
        binding.inputText.append(recalledText);
    }

    /**
     * 删除消息
     *
     * @param message
     * @param position
     */
    public void deleteMessage(Message message, int position) {
        messageListAdapter.removeItem(position);
        messageViewModel.deleteMessage(message);
    }

    /**
     * 引用回复消息
     *
     * @param message
     */
    public void quoteReplyMessage(Message message) {
        // 展示引用消息
        binding.quotedMessageLayout.setVisibility(View.VISIBLE);

        String senderUserName = MessageUtil.getSenderUserName(c, message);
        binding.quotedMessageSender.setText(senderUserName);
        binding.quotedMessageContent.setText(MessageUtil.getContentText(message));

        // 暂存引用消息
        currentQuotedMessage = message;

        // 如果引用的是群聊会话非本人发送的消息，则自动@该用户
        if (c.isGroupConversation() && !MessageUtil.isSendByMe(message)) {
            appendAtUser(message.getFrom().getUid(), senderUserName);
        }

        // 自动弹出软键盘
        showSoftKeyboard();
    }

    /**
     * 收藏消息
     *
     * @param messageIds
     */
    public void addFavoriteMessages(List<String> messageIds) {
//        // 打开新增收藏页
//        Intent intent = new Intent(this, AddFavoriteContentActivity.class);
//        intent.putExtra(AddFavoriteContentActivity.TARGET_TYPE, CollectType.MESSAGE.getCode());
//        intent.putExtra(AddFavoriteContentActivity.TARGET_VALUE, c.getCid());
//        intent.putStringArrayListExtra(AddFavoriteContentActivity.TARGET_VALUE_LIST, Lists.newArrayList(messageIds));
//        this.startActivity(intent);
    }

    /**
     * 弹出多选消息菜单
     *
     * @param message
     */
    public void multiSelectMessage(Message message) {
        hideAllInputLayout();
        showMultiSelectMenu();

        binding.goBack.setVisibility(View.GONE);
        binding.cancelMultiSelect.setVisibility(View.VISIBLE);

        multiSelectMode = true;

        // 重新渲染消息列表，展示消息复选框
        messageListAdapter.notifyDataSetChanged();
    }

    /**
     * 发送快捷回复表情
     *
     * @param message
     * @param emoji
     */
    public void sendInstantReplyEmoji(Message message, FaceIconEmoji emoji) {
        messageViewModel.sendInstantReplyEmoji(c, message, emoji);
    }

    /**
     * 撤回快捷回复表情
     *
     * @param message
     * @param emoji
     */
    public void recallInstantReplyEmoji(Message message, FaceIconEmoji emoji) {
        messageViewModel.recallInstantReplyEmoji(c, message, emoji);
    }

    private void appendEmoji(FaceIconEmoji emoji) {
        binding.inputText.appendEmoji(emoji);
    }

    private void deleteEmoji() {
        binding.inputText.deleteLastItem();
    }

    public void appendAtUser(String atUserId, String atUserName) {
        binding.inputText.appendAtUser(atUserId, atUserName);
    }

    private void appendAtUsers(Map<String, String> atUserMap) {
        atUserMap.forEach((k, v) -> appendAtUser(k, v));
    }

}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.fastjson.JSONObject;
import cn.hsbcsd.mpaastest.R;
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
import com.alipay.fc.ccmimplus.sdk.core.enums.RtcTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import cn.hsbcsd.mpaastest.databinding.ActivityChatBinding;
import com.hsbcd.mpaastest.kotlin.samples.enums.CustomCommandMessageTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.enums.CustomMessageTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.model.MediaSendingProgress;
import com.hsbcd.mpaastest.kotlin.samples.model.VisitingCardData;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.config.face.FaceIconEmoji;
import com.alipay.fc.ccmimplus.sdk.core.constants.Constants;
import com.alipay.fc.ccmimplus.sdk.core.enums.ErrorCodeEnum;
import com.alipay.fc.ccmimplus.sdk.core.favorite.CollectType;
import com.alipay.fc.ccmimplus.sdk.core.message.MessageConverter;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.DateUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.MessageListAdapter;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.command.InputCustomCommandActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.emoji.EmojiInputView;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.richtext.RichTextInputActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.url.InputUrlActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.voice.VoiceRecorder;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting.GroupChatSettingActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.setting.SingleChatSettingActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BottomMenuDialog;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.NotifyViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.SelectMessageViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.session.SessionViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.SendVisitingCardActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.CopyUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.FileUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.MessageUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
import com.amap.api.location.AMapLocation;
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
 * ???????????????
 *
 * @author liyalong
 * @version ChatActivity.java, v 0.1 2022???08???02??? 14:45 liyalong
 */
public class ChatActivity extends AppCompatActivity {

    protected ActivityChatBinding binding;

    private MessageListAdapter messageListAdapter;

    private MessageViewModel messageViewModel;

    private SessionViewModel sessionViewModel;

    private SelectMessageViewModel selectItemViewModel;

    private NotifyViewModel notifyViewModel;

    protected Conversation c;

    private CustomOnScrollListener customOnScrollListener;

    private boolean refreshHistoryMessage = false;

    private Handler handler = new Handler();

    private Uri currentMediaUri;

    private ActivityResultLauncher takePictureLauncher;

    private ActivityResultLauncher captureVideoLauncher;

    private VoiceRecorder voiceRecorder = new VoiceRecorder(this);

    /**
     * ??????????????????????????????
     */
    private boolean multiSelectMode = false;

    /**
     * ?????????????????????????????????????????????
     */
    private boolean needRefreshMessage = true;

    /**
     * ????????????????????????????????????
     */
    private Message currentQuotedMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setCurrentSession();

        // ??????????????????
        bindAction();

        // ??????????????????????????????
        bindInputText();
        bindEmojiGrid();
        bindRecordVoice();
        bindPickMedia();
        bindTakePicture();
        bindCaptureVideo();
        bindRtc();
        bindPickFile();
        bindLocation();
        bindRichText();
        bindInputUrl();
        bindVisitingCard();
        bindCard();
        bindCustomCommand();

        // ???????????????????????????
        messageViewModel = new ViewModelProvider(this).get(MessageViewModel.class);
        bindMessageViewModel();

        // ?????????????????????
        sessionViewModel = new ViewModelProvider(this).get(SessionViewModel.class);
        bindSessionViewModel();

        // ????????????????????????
        selectItemViewModel = new ViewModelProvider(this).get(SelectMessageViewModel.class);
        bindSelectItemViewModel();

        // ??????????????????
        //notifyViewModel = new ViewModelProvider(this).get(NotifyViewModel.class);

        // ?????????????????????
        initMessageList();
    }

    /**
     * ?????????????????????
     */
    @Override
    protected void onStart() {
        super.onStart();

        // ???????????????????????????????????????????????????
        setCurrentSession();

        // ???????????????????????????????????????????????????????????????????????????
        setTitle();

        // ?????????????????????????????????app???????????????/????????????????????????????????????????????????????????????
        if (needRefreshMessage) {
            refreshHistoryMessage();
        }
        // ??????????????????/?????????/?????????/????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
        else {
            needRefreshMessage = true;
        }
    }

    /**
     * ?????????????????????
     */
    @Override
    protected void onStop() {
        super.onStop();

        // ??????????????????(????????????????????????)
        // ???????????????????????????activity?????????????????????(????????????????????????)?????????finished=false???????????????
        if (!c.isChatroomConversation() && isFinishing()) {
            AlipayCcmIMClient.getInstance().getConversationManager().clearCurrentConversation();
        }
    }

    private void setCurrentSession() {
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c == null) {
            Log.e(LoggerName.UI, "chat unset, cannot go to message screen");
            super.onBackPressed();
            return;
        }
        this.c = c;
    }

    /**
     * ??????????????????????????????
     *
     * @return
     */
    public boolean isMultiSelectMode() {
        return multiSelectMode;
    }

    /**
     * ???????????????????????????????????????????????????????????????
     * ?????????????????????/??????/???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     *
     * @param needRefreshMessage
     */
    public void setNeedRefreshMessage(boolean needRefreshMessage) {
        this.needRefreshMessage = needRefreshMessage;
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> onBackPressed());

        // ????????????????????????(???????????????????????????)
        if (c.isSecretChat() || c.isChatroomConversation()) {
            binding.chatSettingView.setVisibility(View.GONE);
        } else {
            binding.chatSettingView.setVisibility(View.VISIBLE);
            binding.chatSettingView.setOnClickListener(v -> {
                // ??????????????????????????????????????????????????????????????????
                sessionViewModel.querySingleConversation(c.getCid());
            });
        }

        // ??????????????????
        binding.chatroomLiveLayout.setVisibility(c.isChatroomConversation() ? View.VISIBLE : View.GONE);

        // ?????????????????????????????????
        bindInputViewSwitch();

        // ???????????????????????????
        bindMultiSelect();

        // ?????????????????????
        bindQuotedMessage();
    }

    private void bindInputViewSwitch() {
        // ????????????/????????????
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

        // ???????????????????????????????????????????????????????????????
        binding.inputText.setOnClickListener(v -> {
            hideInputMoreLayout();

            binding.switchToEmoji.setVisibility(View.VISIBLE);
            binding.switchEmojiToText.setVisibility(View.GONE);

            binding.switchToMultimedia.setVisibility(View.VISIBLE);
            binding.switchMultimediaToText.setVisibility(View.GONE);
        });

        // ????????????????????????/??????
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

        // ???????????????????????????/??????
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

        // ?????????????????????????????????
        binding.inputMultimediaPage1.setOnClickListener(v -> binding.inputMultimediaFlipper.goToNextPage());
        binding.inputMultimediaPage2.setOnClickListener(v -> binding.inputMultimediaFlipper.goToPreviousPage());
    }

    private void bindMultiSelect() {
        // ?????????????????????????????????
        binding.cancelMultiSelect.setOnClickListener(v -> {
            quitMultiSelectMode();
        });

        // ????????????
        binding.batchTransferSingle.setOnClickListener(v -> {
            Collection<Message> selectedMessages = selectItemViewModel.getSelectedItems();
            doTransferMessage(selectedMessages, false);
            quitMultiSelectMode();
        });

        // ????????????
        binding.batchTransferMerge.setOnClickListener(v -> {
            Collection<Message> selectedMessages = selectItemViewModel.getSelectedItems();
            doTransferMessage(selectedMessages, true);
            quitMultiSelectMode();
        });

        // ????????????
        binding.batchFav.setOnClickListener(v -> {
            List<String> selectedMessageIds = selectItemViewModel.getSelectedItems().stream().map(
                    Message::getId).collect(Collectors.toList());
            addFavoriteMessages(selectedMessageIds);
            quitMultiSelectMode();
        });

        // ????????????
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

        // ????????????????????????????????????????????????
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
                // ??????????????????????????????????????????ACTION_UP???ACTION_DOWN??????????????????ACTION_UP?????????????????????????????????
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
                // ??????????????????@????????????????????????@??????????????????????????????????????????
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

            // ?????????????????????"@"??????
            if (position > 0) {
                editable.delete(position - 1, position);
            }

            // ?????????@????????????
            appendAtUsers(atUserMap);

            // ?????????????????????
            showSoftKeyboard();
        });

        dialog.show(((AppCompatActivity) ChatActivity.this).getSupportFragmentManager(), "");
    }

    private void bindEmojiGrid() {
        // ??????????????????????????????????????????
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
                        ToastUtil.makeToast(ChatActivity.this, "No audio record permission", 1000);
                        return;
                    }

                    // ????????????
                    startRecordVoice();
                });

        binding.recordVoice.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    // ???????????????
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    // ???????????????????????????pressed??????????????????????????????
                    v.setPressed(true);

                    // ???????????????????????????????????????
                    if (event.getY() >= binding.recordVoice.getY()) {
                        voiceRecorder.getDialogManager().showRecording();
                    }
                    // ???????????????????????????????????????
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
                        ToastUtil.makeToast(ChatActivity.this, "no camera permission", 1000);
                        return;
                    }

                    // ??????????????????
                    showShotMenu();
                });

        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(), result -> {
                    if (result) {
                        Log.i(LoggerName.UI, "take picture uri: " + currentMediaUri);
                        if (currentMediaUri == null) {
                            ToastUtil.makeToast(this, "take photo failed", 1000);
                        } else {
                            onTakePictureResult(currentMediaUri);
                        }
                    }

                    currentMediaUri = null;
                });

        binding.inputCamera.setOnClickListener(v -> permissionLauncher.launch(Manifest.permission.CAMERA));
    }

    private void showShotMenu() {
        List<BottomMenuDialog.MenuItem> menuItems = Lists.newArrayList();
        menuItems.add(new BottomMenuDialog.MenuItem("photo", () -> doTakePicture()));
        menuItems.add(new BottomMenuDialog.MenuItem("video", () -> doCaptureVideo()));

        BottomMenuDialog bottomMenuDialog = new BottomMenuDialog("camera", menuItems, true);
        bottomMenuDialog.show(this.getSupportFragmentManager(), "");
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
        captureVideoLauncher = registerForActivityResult(
                new ActivityResultContracts.CaptureVideo() {
                    /**
                     * ??????????????????????????????????????????
                     */
                    @NonNull
                    @Override
                    public Intent createIntent(@NonNull Context context, @NonNull Uri input) {
                        Intent intent = super.createIntent(context, input);
                        // ????????????????????????
                        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 60);
                        // ?????????0-?????????1-????????????????????????????????????
                        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
                        return intent;
                    }
                }, result -> {
                    if (result) {
                        Log.i(LoggerName.UI, "capture video uri: " + currentMediaUri);
                        if (currentMediaUri == null) {
                            ToastUtil.makeToast(this, "take video failed", 1000);
                        } else {
                            onCaptureVideoResult(currentMediaUri);
                        }
                    }

                    currentMediaUri = null;
                });
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

    private void bindRtc() {

//        int ret = ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT);
//        if (ret != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN}, 1);
//        }

        ActivityResultLauncher permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    if (result.isEmpty()) {
                        ToastUtil.makeToast(ChatActivity.this, "No audio permission", 1000);
                        return;
                    }

                    // ?????????????????????
                    showRtcMenu();
                });

        binding.inputRtc.setOnClickListener(v -> {
            permissionLauncher.launch(new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    // mpaas???????????????????????????????????????
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN});
        });
    }

    private void showRtcMenu() {
        boolean isSingle = c.isSingle() || c.isSecretChat();

        // ?????????????????????????????????????????????
        if (isSingle) {
            List<BottomMenuDialog.MenuItem> menuItems = Lists.newArrayList();
            menuItems.add(new BottomMenuDialog.MenuItem("audio chat", () -> goToCallRtcMeeting(RtcTypeEnum.AUDIO)));
            menuItems.add(new BottomMenuDialog.MenuItem("video chat", () -> goToCallRtcMeeting(RtcTypeEnum.VIDEO)));

            String title = String.format("call %s",
                    StringUtils.defaultIfBlank(c.getSessionName(), c.getConversationName()));
            BottomMenuDialog bottomMenuDialog = new BottomMenuDialog(title, menuItems, true);
            bottomMenuDialog.show(this.getSupportFragmentManager(), "");
        }
        // ?????????????????????????????????????????????
        else {
            List<BottomMenuDialog.MenuItem> menuItems = Lists.newArrayList();
            menuItems.add(new BottomMenuDialog.MenuItem("audio meeting", () -> goToCreateRtcMeeting(RtcTypeEnum.AUDIO)));
            menuItems.add(new BottomMenuDialog.MenuItem("vudeo meeting", () -> goToCreateRtcMeeting(RtcTypeEnum.VIDEO)));

            BottomMenuDialog bottomMenuDialog = new BottomMenuDialog("start meeting", menuItems, true);
            bottomMenuDialog.show(this.getSupportFragmentManager(), "");
        }
    }

    private void goToCallRtcMeeting(RtcTypeEnum rtcType) {
//        Intent intent = new Intent(this, CallRtcMeetingActivity.class);
//        intent.putExtra(CallRtcMeetingActivity.RTC_TYPE_KEY, rtcType.name());
//        intent.putExtra(CallRtcMeetingActivity.INVITE_USER_ID_KEY,
//                c.getOtherUid(AlipayCcmIMClient.getInstance().getCurrentUserId()));
//
//        this.startActivity(intent);
    }

    private void goToCreateRtcMeeting(RtcTypeEnum rtcType) {
//        Intent intent = new Intent(this, CreateRtcMeetingActivity.class);
//        intent.putExtra(CreateRtcMeetingActivity.RTC_TYPE_KEY, rtcType.name());
//        this.startActivity(intent);
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

                    // ??????????????????
                    String snapshotFilePath = result.getData().getStringExtra("snapshotFilePath");
                    File snapshotFile = new File(snapshotFilePath);
                    AMapLocation location = result.getData().getParcelableExtra("location");

                    // ??????????????????
                    messageViewModel.sendLocationMessage(snapshotFile, location);
                }
        );

        ActivityResultLauncher permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                    if (result.isEmpty()) {
                        ToastUtil.makeToast(ChatActivity.this, "no location permission", 1000);
                        return;
                    }

                    // ??????????????????
//                    Intent intent = new Intent(this, LocationActivity.class);
//                    locationActivityLauncher.launch(intent);
//                    needRefreshMessage = false;
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
        // TODO ?????????????????????????????????
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
                // ???????????????(?????????)??????
                doQueryHistoryMessages();
            }

            @Override
            public void onNoNextPage() {
            }
        };

        binding.messageListView.addOnScrollListener(customOnScrollListener);

        // ???????????????????????????????????????????????????????????????????????????????????????????????????
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
        // ?????????????????????????????????????????????????????????
        if (c.isChatroomConversation()) {
            return;
        }

        refreshHistoryMessage = true;
        customOnScrollListener.reset();
        doQueryHistoryMessages();
    }

    private void doQueryHistoryMessages() {
        if (c.isSecretChat()) {
            messageViewModel.querySecretChatHistoryMessages(c);
        } else {
            messageViewModel.queryHistoryMessages(c);
        }
    }

    private void setTitle() {
        // ????????????????????????????????????
        if (c.isSecretChat()) {
            binding.titleLabel.setText(StringUtils.EMPTY);
            binding.titleLabel.setBackground(getDrawable(R.drawable.ic_secret_chat_title));
            return;
        }

        // ?????????????????????????????????
        if (StringUtils.isNotBlank(c.getSessionName())) {
            binding.titleLabel.setText(c.getSessionName());
        } else {
            binding.titleLabel.setText(c.getConversationName());
        }

        // ?????????????????????
        if (c.isShieldMode()) {
            Drawable icon = this.getDrawable(R.drawable.ic_no_disturb);
            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
            binding.titleLabel.setCompoundDrawables(null, null, icon, null);
            binding.titleLabel.setCompoundDrawablePadding(10);
        }
    }

    /**
     * ??????????????????
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
        // ???????????????????????????????????????????????????
        messageListAdapter.remove(message);

        messageViewModel.reSendMessage(message);
    }

    private void bindMessageViewModel() {
        // ???????????????????????????????????????
        messageViewModel.getMessageListResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            if (CollectionUtils.isEmpty(result.getData())) {
                // ??????????????????????????????????????????????????????????????????????????????????????????
                if (refreshHistoryMessage) {
                    messageListAdapter.clearAll();
                    messageListAdapter.notifyDataSetChanged();
                }

                if (messageListAdapter.getItemCount() > 0) {
                    ToastUtil.makeToast(ChatActivity.this, "no more message", 1000);
                }
                return;
            }

            // ????????????????????????
            List<Message> messages = result.getData().stream()
                    .filter(m -> m.getDeleteStatus() == MessageDeleteStatusEnum.NORMAL)
                    .collect(Collectors.toList());

            // ?????????????????????????????????????????????????????????
            if (refreshHistoryMessage) {
                messageListAdapter.clearAll();
            }

            // ??????????????????????????????????????????????????????????????????????????????
            messageListAdapter.addAll(0, messages);
            messageListAdapter.notifyDataSetChanged();

            // ?????????????????????????????????
            if (refreshHistoryMessage) {
                binding.messageListView.scrollToBottom();
            } else {
                binding.messageListView.scrollToTop(messages.size());
            }

            refreshHistoryMessage = false;
        });

        // ????????????????????????/??????????????????
        messageViewModel.getReceiveMessageResult().observe(this, result -> {
            Message message = result.getMessage();

            switch (result.getType()) {
                // ????????????
                case Normal: {
                    // ??????????????????
                    insertMessage(message);

                    // ?????????????????????
                    messageViewModel.setMessageStatusRead(c, message);
                    break;
                }
                // TODO ??????????????????
                // ?????????????????????????????????????????????????????????
                case NotifyMessageReach: {
                    break;
                }
                // ??????????????????
                case AckMessageRead: {
                    markMessageRead(message);
                    break;
                }
                // ????????????????????????
                case AckBatchMessageRead: {
                    batchMarkMessageRead(message);
                    break;
                }
                // ????????????ack
                case AckMessageRecalled:
                    // ??????????????????
                case CmdRecall: {
                    markMessageRecalled(message);
                    break;
                }
                // TODO ??????????????????????????????
                case CmdSetAllMsgRead:
                    break;
                // ??????????????????
                case QuoteReplyInfo: {
                    // ??????????????????
                    insertMessage(message);

                    // ?????????????????????
                    messageViewModel.setMessageStatusRead(c, message);

                    // ?????????????????????????????????????????????????????????
                    if (message.getQuoteReplyInfo() != null) {
                        refreshTopicOriginalMessage(message, message.getQuoteReplyInfo().getTopicId());
                    }
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

                    // ??????????????????
                    binding.quotedMessageLayout.setVisibility(View.GONE);
                    currentQuotedMessage = null;
                    break;
                }
                // ???????????????(?????????????????????????????????????????????????????????/?????????)
                case IN_PROGRESS: {
                    updateMessageSendingProgress(message, result.getProgress());
                    break;
                }
                // ??????????????????
                case SUCCESS: {
                    updateMessageSendStatusToSuccess(message);

                    // ?????????????????????????????????????????????????????????
                    if (StringUtils.isNotBlank(result.getTopicId())) {
                        refreshTopicOriginalMessage(message, result.getTopicId());
                    }
                    break;
                }
                // ??????????????????
                case FAIL: {
                    ToastUtil.makeToast(this, result.getErrorMessage(), 3000);
                    updateMessageSendStatusToError(message, result.getErrorCode(), result.getErrorMessage());
                    break;
                }
                default:
                    break;
            }
        });

        // ????????????????????????????????????
        messageViewModel.getSendInstantReplyEmojiResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // ??????????????????????????????????????????????????????
            Message message = result.getData();
            refreshInstantReplyMessage(message.getTntInstId(), message.getSid(), Long.valueOf(message.getId()),
                    message.getInstantReplyFaceInfos());
        });

        // ??????????????????????????????????????????
        messageViewModel.getNotifyInstantReplyEmojiResult().observe(this, result -> {
            Message message = result.first;
            List<InstantReplyFaceInfo> faceInfos = result.second;

            // ?????????????????????????????????????????????id
            String quoteMessageId = null;
            // ??????????????????????????????
            if (message.getContent() instanceof InstantReplyFaceContent) {
                quoteMessageId = ((InstantReplyFaceContent) message.getContent()).getQuoteMessageId();
            }
            // ????????????????????????????????????
            else if (message.getContent() instanceof CmdRecallInstantReplyFace) {
                quoteMessageId = ((CmdRecallInstantReplyFace) message.getContent()).getQuoteMessageId();
            }

            // ?????????????????????????????????????????????
            refreshInstantReplyMessage(message.getTntInstId(), message.getSid(), Long.valueOf(quoteMessageId),
                    faceInfos);
        });
    }

    protected void insertMessage(Message message) {
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
     * ??????????????????????????????????????????
     *
     * @param message
     */
    public void jumpToMessage(Message message) {
        int index = messageListAdapter.indexOfByMsgId(message.getTntInstId(), message.getSid(),
                Long.valueOf(message.getId()));
        if (index != -1) {
            // ???????????????????????????????????????????????????????????????????????????????????????????????????
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

        // ?????????????????????????????????
        Message<?> msgInList = messageListAdapter.getMessage(index);

        // ????????????????????????
        msgInList.getExtInfo().putAll(message.getExtInfo());
        msgInList.setReadStatus(MessageReadStatusEnum.READ);

        // ??????@?????????????????????
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

        // ???????????????????????????ack
        if (message.getContent() instanceof AckMessageRecalled) {
            AckMessageRecalled content = (AckMessageRecalled) message.getContent();
            localId = content.getLocalId();
        }
        // ?????????????????????????????????
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
        // ???????????????????????????????????????id???????????????????????????????????????????????????????????????????????????????????????
        if (StringUtils.isNotBlank(message.getId())) {
            c.setLastMsgId(Long.valueOf(message.getId()));
        }

        // ??????????????????????????????????????????/???????????????????????????
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

        // ???????????????????????????????????????????????????
        if (message.getSendStatus() == MessageSendStatusEnum.CANCEL || StringUtils.equalsIgnoreCase(errorCode,
                ErrorCodeEnum.USER_CANCEL_UPLOAD.getCode())) {
            messageListAdapter.removeItem(index);
        }
        // ??????????????????????????????????????????????????????(????????????)
        else {
            messageListAdapter.notifyItemChanged(index);
        }
    }

    private void bindSessionViewModel() {
        // ????????????????????????
        sessionViewModel.getConversationResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast(this, result.getMessage(), 3000);
                return;
            }

            // ?????????????????????????????????????????????????????????????????????????????????
            Conversation c = result.getData();
            AlipayCcmIMClient.getInstance().getConversationManager().setCurrentConversation(result.getData());

            // ???????????????
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
            // TODO ?????????????????????????????????????????????
        });
    }

    /**
     * ??????????????????
     *
     * @param message
     */
    public void copyMessage(Message message) {
        String text = MessageUtil.getContentTextForCopy(message);

        if (StringUtils.isBlank(text)) {
            ToastUtil.makeToast(this, "cannot copy message now", 1000);
            return;
        }

        CopyUtil.copyToClipboard(this, text);
    }

    /**
     * ?????????????????????????????????
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
     * ????????????
     *
     * @param message
     */
    public void recallMessage(Message message) {
        // TODO ????????????????????????????????????????????????
        if (!StringUtils.equals(message.getFrom().getUid(), AlipayCcmIMClient.getInstance().getCurrentUserId())) {
            ToastUtil.makeToast(this, "cannot recall message sent by others", 1000);
            return;
        }

        // ??????????????????
        messageViewModel.recallMessage(message);

        // ??????????????????????????????????????????????????????????????????
        int index = messageListAdapter.indexOfByMsgId(message.getTntInstId(), message.getSid(),
                Long.valueOf(message.getId()));
        if (index != -1) {
            message.setRecallStatus(MessageRecallStatusEnum.RECALLING);
            messageListAdapter.notifyItemChanged(index);
        }
    }

    /**
     * ???????????????????????????
     *
     * @param message
     */
    public void reEditRecalledMessage(Message message) {
        String recalledText = MessageConverter.getTextValue(message);
        binding.inputText.append(recalledText);
    }

    /**
     * ????????????
     *
     * @param message
     * @param position
     */
    public void deleteMessage(Message message, int position) {
        messageListAdapter.removeItem(position);
        messageViewModel.deleteMessage(message);
    }

    /**
     * ??????????????????
     *
     * @param message
     */
    public void quoteReplyMessage(Message message) {
        // ??????????????????
        binding.quotedMessageLayout.setVisibility(View.VISIBLE);

        String senderUserName = MessageUtil.getSenderUserName(c, message);
        binding.quotedMessageSender.setText(senderUserName);
        binding.quotedMessageContent.setText(MessageUtil.getContentText(message));

        // ??????????????????
        currentQuotedMessage = message;

        // ??????????????????????????????????????????????????????????????????@?????????
        if (c.isGroupConversation() && !MessageUtil.isSendByMe(message)) {
            appendAtUser(message.getFrom().getUid(), senderUserName);
        }

        // ?????????????????????
        showSoftKeyboard();
    }

    /**
     * ????????????
     *
     * @param messageIds
     */
    public void addFavoriteMessages(List<String> messageIds) {
//        // ?????????????????????
//        Intent intent = new Intent(this, AddFavoriteContentActivity.class);
//        intent.putExtra(AddFavoriteContentActivity.TARGET_TYPE, CollectType.MESSAGE.getCode());
//        intent.putExtra(AddFavoriteContentActivity.TARGET_VALUE, c.getCid());
//        intent.putStringArrayListExtra(AddFavoriteContentActivity.TARGET_VALUE_LIST, Lists.newArrayList(messageIds));
//        this.startActivity(intent);
    }

    /**
     * ????????????????????????
     *
     * @param message
     */
    public void multiSelectMessage(Message message) {
        hideAllInputLayout();
        showMultiSelectMenu();

        binding.goBack.setVisibility(View.GONE);
        binding.cancelMultiSelect.setVisibility(View.VISIBLE);

        multiSelectMode = true;

        // ????????????????????????????????????????????????
        messageListAdapter.notifyDataSetChanged();
    }

    /**
     * ????????????????????????
     *
     * @param message
     * @param emoji
     */
    public void sendInstantReplyEmoji(Message message, FaceIconEmoji emoji) {
        messageViewModel.sendInstantReplyEmoji(c, message, emoji);
    }

    /**
     * ????????????????????????
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
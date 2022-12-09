/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import cn.com.hsbc.hsbcchina.cert.R;
import cn.com.hsbc.hsbcchina.cert.databinding.MessageContainerBinding;

import com.alipay.fc.ccmimplus.common.message.domain.MessageContentType;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.CardContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.CustomContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.FaceContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.FileContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.ImageContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.InstantReplyFaceInfo;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.LocationContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.MergeContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.QuoteReplyInfo;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.QuotedInfo;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.RichTextContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.TextContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.UrlContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.VideoContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.VoiceContent;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageRecallStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.MessageVO;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.CommonConstants;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.converter.MessageConverter;
import com.hsbcd.mpaastest.kotlin.samples.enums.CustomMessageTypeEnum;
import com.hsbcd.mpaastest.kotlin.samples.model.InstantReplyEmojiItem;
import com.hsbcd.mpaastest.kotlin.samples.model.MediaSendingProgress;
import com.hsbcd.mpaastest.kotlin.samples.model.VisitingCardData;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.alipay.fc.ccmimplus.sdk.core.client.ImProgressCallback;
import com.alipay.fc.ccmimplus.sdk.core.constants.Constants;
import com.alipay.fc.ccmimplus.sdk.core.enums.MessageSendPhraseEnum;
import com.alipay.fc.ccmimplus.sdk.core.executor.AsyncExecutorService;
import com.alipay.fc.ccmimplus.sdk.core.message.MessageManager;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.AppUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.DateUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.FileUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.HttpUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.MapUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.MsgUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.PermissionUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.ProgressBarUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.UrlUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.ChatActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.MessagePopupMenu;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.TopicMessageListDialog;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.image.ImagePreviewActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.merged.MergedMessageActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.richtext.RichTextView;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.text.CustomTextView;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.video.VideoPlayerActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseListItemHolder;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.SelectMessageViewModel;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.contacts.UserDetailActivity;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.UserCacheManager;
import com.hsbcd.mpaastest.kotlin.samples.util.FileUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.MessageUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.ThreadUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 消息holder基类
 *
 * @author liyalong
 * @version AbstractMessageHolder.java, v 0.1 2022年08月12日 11:54 liyalong
 */
public abstract class AbstractMessageHolder extends BaseListItemHolder<Message> {

    private static final int UNKNOWN_TYPE = -1;

    private static final int MAX_IMAGE_PIXEL = 512;

    protected MessageContainerBinding messageBinding;

    protected Handler handler = new Handler();

    /**
     * 是否为渲染话题消息列表模式，该模式下需要隐藏部分组件
     */
    protected boolean topicMessageMode = false;

    private CheckBox checkBox;

    private Map<Integer, View> viewTypesMapping = new HashMap<>();

    private SelectMessageViewModel selectItemViewModel;

    public AbstractMessageHolder(Context context, View itemView) {
        super(context, itemView);

        // 绑定选择消息数据
        selectItemViewModel = new ViewModelProvider((AppCompatActivity) context).get(SelectMessageViewModel.class);
    }

    public AbstractMessageHolder(Context context, View itemView, OnItemActionListener onItemActionListener) {
        super(context, itemView, onItemActionListener);

        // 绑定选择消息数据
        selectItemViewModel = new ViewModelProvider((AppCompatActivity) context).get(SelectMessageViewModel.class);
    }

    protected void initMessageBinding(MessageContainerBinding messageBinding, CheckBox checkBox) {
        this.messageBinding = messageBinding;
        this.checkBox = checkBox;

        viewTypesMapping.put(MessageContentType.TEXT_VALUE, messageBinding.textMsg);
        viewTypesMapping.put(MessageContentType.FACE_VALUE, messageBinding.faceMsg);
        viewTypesMapping.put(MessageContentType.IMAGE_VALUE, messageBinding.imageMsgLayout);
        viewTypesMapping.put(MessageContentType.VOICE_VALUE, messageBinding.voiceMsgLayout);
        viewTypesMapping.put(MessageContentType.VIDEO_VALUE, messageBinding.videoMsgLayout);
        viewTypesMapping.put(MessageContentType.FILE_VALUE, messageBinding.fileMsgLayout);
        viewTypesMapping.put(MessageContentType.LOCATION_VALUE, messageBinding.locationMsgLayout);
        viewTypesMapping.put(MessageContentType.URL_VALUE, messageBinding.urlMsgLayout);
        viewTypesMapping.put(MessageContentType.CUSTOM_VALUE, messageBinding.cardMsgLayout);
        viewTypesMapping.put(MessageContentType.MERGED_VALUE, messageBinding.mergedMsgLayout);
        viewTypesMapping.put(MessageContentType.RICH_TEXT_VALUE, messageBinding.richTextMsg);
        viewTypesMapping.put(MessageContentType.CARD_VALUE, messageBinding.simpleCardMsg);
        viewTypesMapping.put(UNKNOWN_TYPE, messageBinding.unknownMsg);
    }

    /**
     * 绑定消息弹出菜单
     *
     * @param message
     */
    protected void bindMessagePopupMenu(Message message) {
        messageBinding.getRoot().setOnLongClickListener(v -> showMessagePopupMenu(message));
    }

    private boolean showMessagePopupMenu(Message message) {
        // 密聊会话禁用消息弹出菜单
        // TODO 重新写一个密聊专用菜单，只开放撤回和删除功能
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c != null && c.isSecretChat()) {
            return false;
        }

        MessagePopupMenu popupWindow = new MessagePopupMenu(context, itemView, message, this.getAdapterPosition(),
                topicMessageMode);
        popupWindow.show(((AppCompatActivity) context).getSupportFragmentManager(), "");

        return false;
    }

    /**
     * 渲染消息内容
     *
     * @param message
     * @return
     */
    protected void renderMessageContent(Message message) {
        // 渲染已撤回消息
        if (message.getRecallStatus() == MessageRecallStatusEnum.RECALLING
                || message.getRecallStatus() == MessageRecallStatusEnum.RECALLED) {
            renderRecalledMessage(message);
            return;
        }

        // 显示普通消息(非撤回消息)视图
        showNormalMessage();

        // 渲染消息复选框
        renderSelectMessageCheckbox(message);

        // 渲染消息内容
        switch (message.getContentTypeCode()) {
            // 文本消息
            case MessageContentType.TEXT_VALUE: {
                hideOtherAndShowMe(MessageContentType.TEXT_VALUE);
                renderTextMessage(message);
                break;
            }
            // 富文本消息
            case MessageContentType.RICH_TEXT_VALUE: {
                hideOtherAndShowMe(MessageContentType.RICH_TEXT_VALUE);
                renderRichTextMessage(message);
                break;
            }
            // 单个表情消息
            case MessageContentType.FACE_VALUE: {
                hideOtherAndShowMe(MessageContentType.FACE_VALUE);
                renderFaceMessage(message);
                break;
            }
            // 图片消息
            case MessageContentType.IMAGE_VALUE: {
                checkPermission();
                hideOtherAndShowMe(MessageContentType.IMAGE_VALUE);
                renderImageMessage(message);
                break;
            }
            // 语音消息
            case MessageContentType.VOICE_VALUE: {
                checkPermission();
                hideOtherAndShowMe(MessageContentType.VOICE_VALUE);
                renderVoiceMessage(message);
                break;
            }
            // 视频消息
            case MessageContentType.VIDEO_VALUE: {
                checkPermission();
                hideOtherAndShowMe(MessageContentType.VIDEO_VALUE);
                renderVideoMessage(message);
                break;
            }
            // 文件消息
            case MessageContentType.FILE_VALUE: {
                checkPermission();
                hideOtherAndShowMe(MessageContentType.FILE_VALUE);
                renderFileMessage(message);
                break;
            }
            // 位置消息
            case MessageContentType.LOCATION_VALUE: {
                hideOtherAndShowMe(MessageContentType.LOCATION_VALUE);
                renderLocationMessage(message);
                break;
            }
            // URL消息
            case MessageContentType.URL_VALUE: {
                hideOtherAndShowMe(MessageContentType.URL_VALUE);
                renderUrlMessage(message);
                break;
            }
            // 自定义消息
            case MessageContentType.CUSTOM_VALUE: {
                hideOtherAndShowMe(MessageContentType.CUSTOM_VALUE);
                renderCustomMessage(message);
                break;
            }
            //简单卡片
            case MessageContentType.CARD_VALUE: {
                hideOtherAndShowMe(MessageContentType.CARD_VALUE);
                renderCardMessage(message);
                break;
            }
            // 合并转发消息
            case MessageContentType.MERGED_VALUE: {
                hideOtherAndShowMe(MessageContentType.MERGED_VALUE);
                renderMergedMessage(message);
                break;
            }
            default: {
                hideOtherAndShowMe(UNKNOWN_TYPE);
                renderUnknownMessage(String.format("[未知消息类型: %s]", message.getContentType()));
                break;
            }
        }

        // 渲染快捷回复表情列表
        renderInstantReplyEmojiList(message);

        // 渲染引用的消息内容
        renderQuotedMessage(message);
    }

    private void renderSelectMessageCheckbox(Message message) {
        if (checkBox == null) {
            return;
        }

        if (context instanceof ChatActivity && ((ChatActivity) context).isMultiSelectMode()) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setOnCheckedChangeListener(null);
            checkBox.setChecked(selectItemViewModel.isSelected(message));
            checkBox.setOnCheckedChangeListener((v, b) -> selectItemViewModel.updateSelectedItems(message, b));
        } else {
            checkBox.setVisibility(View.GONE);
        }
    }

    private void checkPermission() {
        PermissionUtils.requireDiskReadWritePermission((Activity) context);
    }

    protected abstract void showNormalMessage();

    protected abstract void renderRecalledMessage(Message message);

    protected void renderQuoteReplyCount(TextView textView, Message message) {
        QuotedInfo quotedInfo = message.getQuotedInfo();
        if (quotedInfo == null || quotedInfo.getReplyCounter() == 0) {
            textView.setVisibility(View.GONE);
            return;
        }

        textView.setVisibility(View.VISIBLE);
        textView.setText(String.format("%d条回复", quotedInfo.getReplyCounter()));

        textView.setOnClickListener(v -> renderTopicMessageList(quotedInfo.getTopicId()));
    }

    protected void setTopicMessageMode(boolean topicMessageMode) {
        this.topicMessageMode = topicMessageMode;
    }

    private void hideAllNormalMessage() {
        for (Integer type : viewTypesMapping.keySet()) {
            viewTypesMapping.get(type).setVisibility(View.GONE);
        }
    }

    private void hideOtherAndShowMe(int type) {
        int typeCode = type;
        for (int key : viewTypesMapping.keySet()) {
            if (key != typeCode) {
                viewTypesMapping.get(key).setVisibility(View.GONE);
            } else {
                viewTypesMapping.get(key).setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 初始化消息发送进度
     *
     * @param message
     */
    private void initSendingProgress(Message message) {
        Object sendPhraseCode = message.getExtInfo().get(Constants.MESSAGE_SEND_STATUS);
        MessageContentType type = MessageContentType.forNumber(message.getContentTypeCode());

        // 如果消息处于准备发送阶段，则初始化进度条
        if (sendPhraseCode != null && StringUtils.equalsIgnoreCase((String) sendPhraseCode,
                MessageSendPhraseEnum.PREPARE.getCode())) {

            MediaSendingProgress progress = new MediaSendingProgress(100, 0, false, type);
            updateSendingProgress(progress);
        }
        // 否则置为发送完成状态
        else {
            finishSendingProgress(type);
        }
    }

    /**
     * 更新消息发送进度
     *
     * @param progress 进度
     */
    public void updateSendingProgress(MediaSendingProgress progress) {
        if (progress.isFinished()) {
            finishSendingProgress(progress.getType());
            return;
        }

        if (ThreadUtil.isUiThread()) {
            doUpdateSendingProgress(progress);
        } else {
            handler.post(() -> doUpdateSendingProgress(progress));
        }
    }

    private void doUpdateSendingProgress(MediaSendingProgress progress) {
        switch (progress.getType()) {
            case IMAGE: {
                messageBinding.imageMsg.setImageAlpha(100);
                messageBinding.imageProgress.setProgress(progress.getProgress());
                messageBinding.imageProgress.setVisibility(View.VISIBLE);
                break;
            }
            case VIDEO: {
                messageBinding.videoCover.setImageAlpha(100);
                messageBinding.videoProgress.setProgress(progress.getProgress());
                messageBinding.videoProgress.setVisibility(View.VISIBLE);
                messageBinding.videoIcon.setVisibility(View.GONE);
                break;
            }
            case FILE: {
                messageBinding.fileIcon.getBackground().setAlpha(100);
                messageBinding.fileProgress.setProgress(progress.getProgress());
                messageBinding.fileProgress.setVisibility(View.VISIBLE);
                break;
            }
            default:
                break;
        }
    }

    /**
     * 结束进度
     *
     * @param type
     */
    public void finishSendingProgress(MessageContentType type) {
        if (ThreadUtil.isUiThread()) {
            doFinishSendingProgress(type);
        } else {
            handler.post(() -> doFinishSendingProgress(type));
        }
    }

    private void doFinishSendingProgress(MessageContentType type) {
        switch (type) {
            case IMAGE: {
                messageBinding.imageMsg.setImageAlpha(255);
                messageBinding.imageProgress.setVisibility(View.GONE);
                break;
            }
            case VIDEO: {
                messageBinding.videoCover.setImageAlpha(255);
                messageBinding.videoProgress.setVisibility(View.GONE);
                messageBinding.videoIcon.setVisibility(View.VISIBLE);
                break;
            }
            case FILE: {
                messageBinding.fileIcon.getBackground().setAlpha(255);
                messageBinding.fileProgress.setVisibility(View.GONE);
                break;
            }
            default:
                break;
        }
    }

    private void downloadFile(Message message, String fileType, String downloadUrl, DownloadCallback callback) {
        File parent = new File(
                AlipayCcmIMClient.getInstance().getInitConfig().getStorageDir() + Constants.STORAGE_FILE);
        if (!parent.exists()) {
            parent.mkdirs();
        }

        String name = message.getLocalId() + "." + fileType;
        File file = new File(parent, name);

        ImProgressCallback progressCallback = new ImProgressCallback() {
            @Override
            public void onProgress(long total, long download, boolean done) {
                Log.i(LoggerName.UI, String.format("downloading progress: %d/%d/%s", download, total, done));

                MediaSendingProgress progress = new MediaSendingProgress(total, download, done,
                        MessageContentType.forNumber(message.getContentTypeCode()));
                updateSendingProgress(progress);
            }
        };

        ProgressBarUtils progressBarUtils = ProgressBarUtils.create();
        HttpUtils.download(downloadUrl, file, new ImCallback<File>() {
            @Override
            public void onProgress(long total, long downloaded, boolean done) {
                progressBarUtils.reWriteOnProgress(progressCallback, total, downloaded, done);
            }

            @Override
            public void onSuccess(File localFile) {
                updateMessageLocalFile(localFile, message);

                finishSendingProgress(MessageContentType.forNumber(message.getContentTypeCode()));
                callback.onSuccess(localFile.getAbsolutePath());
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {
                t.printStackTrace();
                callback.onError(errorCode, message, t);
            }
        });
    }

    private void updateMessageLocalFile(File localFile, Message message) {
        //设置本地文件路径
        message.getExtInfo().put(Constants.MESSAGE_LOCAL_FILE_PATH, localFile.getAbsolutePath());
        AsyncExecutorService.getInstance().execute(() -> MessageManager.getInstance().updateMessageExtInfo(message));
    }

    private void renderTextMessage(Message message) {
        CustomTextView textMsg = messageBinding.textMsg;
        textMsg.setMessage(message);

        Object content = message.getContent();
        if (content instanceof MessageVO.TextMessageVO) {
            textMsg.setText(((MessageVO.TextMessageVO) message.getContent()).getText());
        } else {
            if (message.getContent() instanceof TextContent) {
                textMsg.setText(((TextContent) message.getContent()).getText());
            }
        }

        // 如果是话题消息列表中的原始消息，重设组件边距和字体颜色
        if (this instanceof TopicOriginalMessageHolder) {
            textMsg.setPadding(10, 0, 0, 0);
            textMsg.setTextColor(context.getResources().getColor(android.R.color.tab_indicator_text));
        }
    }

    private void renderRichTextMessage(Message message) {
        RichTextView textMsg = messageBinding.richTextMsg;
        Object content = message.getContent();
        RichTextContent richTxtContent = (RichTextContent) content;
        textMsg.rend(richTxtContent);
    }

    private void renderFaceMessage(Message message) {
        CustomTextView faceMsg = messageBinding.faceMsg;
        FaceContent faceContent = (FaceContent) message.getContent();
        faceMsg.setText(faceContent.getEmoji());
    }

    private void renderImageMessage(Message message) {
        initSendingProgress(message);

        ImageContent content = (ImageContent) message.getContent();

        // 如果本地文件存在，则直接渲染
        if (FileUtils.localFileExistsOfMessage(message)) {
            String localFilePath = MapUtils.getString(message.getExtInfo(), Constants.MESSAGE_LOCAL_FILE_PATH);
            Pair<Integer, Integer> rawSize = FileUtil.getImageWidthHeight(localFilePath);

            doRenderImage(localFilePath, rawSize.first, rawSize.second);
        }
        // 如果不存在，可能是我的另一个设备发送的，也可能是对方发送的，需要先下载到本地再渲染
        else {
            downloadFile(message, content.getType(), content.getPic(), new DownloadCallback() {
                @Override
                public void onSuccess(String filePath) {
                    handler.post(() -> doRenderImage(filePath, content.getWidth(), content.getHeight()));
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {

                }
            });
        }

        // 显式绑定长按事件，解决绑定了单击事件以后无法响应rootView长按事件的问题，下同
        if (context instanceof ChatActivity) {
            messageBinding.imageMsgLayout.setOnLongClickListener(v -> showMessagePopupMenu(message));
        }
    }

    private void doRenderImage(String filePath, int width, int height) {
        Log.i(Constants.LOG_MESSAGE, DateUtils.formatNow() + ":" + getAdapterPosition() +
                "--->load image from local path (" + filePath + ")");

        try {
            if (((Activity) this.context).isDestroyed()) {
                return;
            }

            Pair<Integer, Integer> resized = calculateDisplayImageSize(width, height);

            ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) messageBinding.imageMsg.getLayoutParams();
            layoutParams.width = resized.first;
            layoutParams.height = resized.second;
            messageBinding.imageMsg.setLayoutParams(layoutParams);

            Glide.with(this.context).load(filePath).diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(resized.first, resized.second).into(messageBinding.imageMsg);

        } catch (Exception e) {
            Log.e(LoggerName.UI, e.getMessage(), e);
        }

        messageBinding.imageMsgLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, ImagePreviewActivity.class);
            intent.putExtra(ImagePreviewActivity.IMAGE_FILE_PATH, filePath);
            context.startActivity(intent);

            if (context instanceof ChatActivity) {
                ((ChatActivity) context).setNeedRefreshMessage(false);
            }
        });
    }

    private void renderVoiceMessage(Message message) {
        VoiceContent content = (VoiceContent) message.getContent();

        int durationSeconds = isHttpRemoteResource(content.getUrl()) ? (int) content.getDuration() :
                FileUtil.getVoiceDuration(content.getUrl());
        messageBinding.voiceDuration.setText(String.format("%d\"", durationSeconds));

        messageBinding.voiceMsgLayout.setOnClickListener(v -> {
            SimpleExoPlayer player = new SimpleExoPlayer.Builder(context).build();

            DefaultDataSourceFactory factory = new DefaultDataSourceFactory(context);
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(factory).createMediaSource(
                    MediaItem.fromUri(content.getUrl()));

            player.setMediaSource(mediaSource);
            player.prepare();
            player.play();
        });

        if (context instanceof ChatActivity) {
            messageBinding.voiceMsgLayout.setOnLongClickListener(v -> showMessagePopupMenu(message));
        }
    }

    public void renderVideoMessage(Message message) {
        initSendingProgress(message);

        VideoContent content = (VideoContent) message.getContent();

        Pair<Integer, Integer> resized;
        int durationSeconds;

        // 如果视频地址是url，说明是服务端下发的视频消息，取服务端生成的视频截图和时长信息
        if (isHttpRemoteResource(content.getUrl())) {
            resized = calculateDisplayImageSize(content.getCoverImageWidth(), content.getCoverImageHeight());
            durationSeconds = (int) content.getDuration();

            Glide.with(context).load(content.getCoverImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(resized.first, resized.second)
                    .into(messageBinding.videoCover);
        }
        // 如果视频是本地文件，说明是本机拍摄的视频，解析视频文件获取视频截图和时长信息
        else {
            Pair<Bitmap, Integer> pair = FileUtil.getVideoSnapshotAndDuration(content.getUrl());

            Bitmap snapshot = pair.first;
            resized = calculateDisplayImageSize(snapshot.getWidth(), snapshot.getHeight());
            durationSeconds = pair.second;

            Glide.with(context).load(snapshot)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .override(resized.first, resized.second)
                    .into(messageBinding.videoCover);
        }

        messageBinding.videoCover.setLayoutParams(new LinearLayout.LayoutParams(resized.first, resized.second));
        messageBinding.videoDuration.setText(String.format("%02d:%02d", durationSeconds / 60, durationSeconds % 60));

        messageBinding.videoMsgLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra(VideoPlayerActivity.VIDEO_URL_KEY, content.getUrl());
            context.startActivity(intent);

            if (context instanceof ChatActivity) {
                ((ChatActivity) context).setNeedRefreshMessage(false);
            }
        });

        if (context instanceof ChatActivity) {
            messageBinding.videoMsgLayout.setOnLongClickListener(v -> showMessagePopupMenu(message));
        }
    }

    private boolean isHttpRemoteResource(String url) {
        return StringUtils.startsWith(url, "http") || StringUtils.startsWith(url, "https");
    }

    private Pair<Integer, Integer> calculateDisplayImageSize(int rawWidth, int rawHeight) {
        if (rawWidth <= MAX_IMAGE_PIXEL && rawHeight <= MAX_IMAGE_PIXEL) {
            return Pair.create(rawWidth, rawHeight);
        }

        if (rawWidth > rawHeight) {
            int resizedWidth = MAX_IMAGE_PIXEL;
            int resizedHeight = (int) (rawHeight * ((double) resizedWidth / rawWidth));
            return Pair.create(resizedWidth, resizedHeight);

        } else {
            int resizedHeight = MAX_IMAGE_PIXEL;
            int resizedWidth = (int) (rawWidth * ((double) resizedHeight / rawHeight));
            return Pair.create(resizedWidth, resizedHeight);
        }
    }

    private void renderFileMessage(Message message) {
        initSendingProgress(message);

        FileContent content = (FileContent) message.getContent();

        int iconResId = R.drawable.ic_file_default;
        String type = StringUtils.defaultIfBlank(content.getType(), FileUtils.getExtension(content.getFileName()));
        if (StringUtils.isNotBlank(type)) {
            if (type.startsWith("doc")) {
                iconResId = R.drawable.ic_file_word;
            } else if (type.startsWith("ppt")) {
                iconResId = R.drawable.ic_file_ppt;
            } else if (type.startsWith("pdf")) {
                iconResId = R.drawable.ic_file_pdf;
            }
        }
        messageBinding.fileIcon.setBackground(context.getDrawable(iconResId));

        String fileName = StringUtils.defaultIfBlank(
                MapUtils.getString(message.getExtInfo(), Constants.MESSAGE_LOCAL_FILE_NAME), content.getFileName());
        messageBinding.fileName.setText(fileName);

        messageBinding.fileSize.setText(FileUtil.getFileSize(content.getSize()));
        messageBinding.fileMsgLayout.setOnClickListener(v -> openFile(message));

        if (context instanceof ChatActivity) {
            messageBinding.fileMsgLayout.setOnLongClickListener(v -> showMessagePopupMenu(message));
        }
    }

    private void openFile(Message message) {
        FileContent content = (FileContent) message.getContent();

        // 如果本地文件存在，则直接打开
        if (FileUtils.localFileExistsOfMessage(message)) {
            String filePath = FileUtils.getLocalFileOfMessage(message);
            doOpenFile(filePath);
        }
        // 否则先下载，再打开
        else {
            downloadFile(message, content.getType(), content.getUrl(), new DownloadCallback() {
                @Override
                public void onSuccess(String filePath) {
                    doOpenFile(filePath);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {

                }
            });
        }
    }

    private void doOpenFile(String filePath) {
        handler.post(() -> {
            try {
                Intent intent = FileUtil.getOpenFileIntent(filePath);
                context.startActivity(intent);

                if (context instanceof ChatActivity) {
                    ((ChatActivity) context).setNeedRefreshMessage(false);
                }
            } catch (Exception e) {
                ToastUtil.makeToast((Activity) context, "当前文件不支持预览", 3000);
            }
        });
    }

    private void renderLocationMessage(Message message) {
        LocationContent content = (LocationContent) message.getContent();

        messageBinding.locationAddress.setText(StringUtils.defaultIfBlank(content.getTitle(), content.getSubTitle()));

        String fileUrl = content.getImageFileUrl();
        if (FileUtils.localFileExistsOfMessage(message)) {
            fileUrl = FileUtils.getLocalFileOfMessage(message);
        }

        Glide.with(context).load(fileUrl).override(1024, 512).diskCacheStrategy(
                DiskCacheStrategy.ALL).into(messageBinding.locationSnapshot);

        messageBinding.locationMsgLayout.setOnClickListener((e) -> openLocationActivity(content));

        if (context instanceof ChatActivity) {
            messageBinding.locationMsgLayout.setOnLongClickListener(v -> showMessagePopupMenu(message));
        }
    }

    private void openLocationActivity(LocationContent content) {
        // 跳转到高德地图app的导航路线
        if (AppUtils.checkAppInstalled(context, "com.autonavi.minimap")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);

            Uri uri = Uri.parse("androidamap://route/plan/?dlat=" + content.getLatitude() + "&dlon=" + content
                    .getLongitude() + "&dname=我的定位&dev=0&t=0");
            intent.setData(uri);

            this.context.startActivity(intent);

        } else {
            Activity activity = (Activity) context;
            ToastUtil.makeToast(activity, "您尚未安装高德地图", 3000);
            Uri uri = Uri.parse("market://details?id=com.autonavi.minimap");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                activity.startActivity(intent);
            }
        }

        if (context instanceof ChatActivity) {
            ((ChatActivity) context).setNeedRefreshMessage(false);
        }
    }

    /**
     * 渲染url消息
     *
     * @param message
     */
    private void renderUrlMessage(Message message) {
        UrlContent content = (UrlContent) message.getContent();

        // url文本转换为超链接
        String html = String.format("<font color='#0091FF'><a href='%s' target='_blank'>%s</a></font>",
                content.getHref(), StringUtils.defaultIfBlank(content.getTitle(), content.getHref()));
        messageBinding.urlMsg.setText(Html.fromHtml(html));

        // 点击时调用系统浏览器打开链接
        messageBinding.urlMsgLayout.setOnClickListener((e) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (UrlUtils.isPureUrl(content.getHref())) {
                intent.setData(Uri.parse(content.getHref()));
                context.startActivity(intent);
            }
        });

        if (context instanceof ChatActivity) {
            messageBinding.urlMsgLayout.setOnLongClickListener(v -> showMessagePopupMenu(message));
        }

        // TODO 使用第三方库生成url预览视图
//        messageBinding.richLinkView.load(content.getHref(), new LinkViewCallback() {
//            @Override
//            public void onSuccess(@NonNull LinkData linkData) {
//                Log.i(LoggerName.UI, "link view load success: " + linkData.toString());
//
//                if (StringUtils.isBlank(linkData.getTitle())) {
//                    linkData.setTitle(linkData.getUrl());
//                    messageBinding.richLinkView.load(linkData);
//                }
//            }
//
//            @Override
//            public void onError(@NonNull Exception e) {
//                Log.e(LoggerName.UI, "link view load fail", e);
//
//                LinkData linkData = new LinkData();
//                linkData.setTitle(content.getHref());
//                linkData.setUrl(content.getHref());
//                messageBinding.richLinkView.load(linkData);
//            }
//        });
    }

    private void renderCardMessage(Message message) {
        CardContent content = message.getCardMessageContent();
        if (content != null) {
            messageBinding.simpleCardTitle.setText(content.getTitle());
            messageBinding.simpleCardContent.setText(content.getContent());
        }
    }

    /**
     * 渲染自定义消息
     *
     * @param message
     */
    private void renderCustomMessage(Message message) {
        CustomContent content = (CustomContent) message.getContent();

        CustomMessageTypeEnum typeEnum = null;
        try {
            typeEnum = CustomMessageTypeEnum.valueOf(content.getDataType());
        } catch (Exception e) {
            Log.e(LoggerName.UI, "unknown custom message data type: " + content.getDataType(), e);
            renderUnknownCustomMessage();
            return;
        }

        switch (typeEnum) {
            // 名片消息
            case VISITING_CARD: {
                VisitingCardData data = JSONObject.parseObject(new String(content.getData()), VisitingCardData.class);

                Glide.with(context).load(data.getUserAvatar()).diskCacheStrategy(DiskCacheStrategy.ALL).into(
                        messageBinding.cardUserAvatar);
                messageBinding.cardUserName.setText(data.getUserName());
                messageBinding.cardNickName.setText(data.getNickName());

                messageBinding.cardMsgLayout.setOnClickListener(v -> {
                    Intent intent = new Intent(context, UserDetailActivity.class);
                    intent.putExtra(CommonConstants.USER_ID, data.getUserId());
                    context.startActivity(intent);

                    if (context instanceof ChatActivity) {
                        ((ChatActivity) context).setNeedRefreshMessage(false);
                    }
                });

                if (context instanceof ChatActivity) {
                    messageBinding.cardMsgLayout.setOnLongClickListener(v -> showMessagePopupMenu(message));
                }
                break;
            }
            default: {
                renderUnknownCustomMessage();
                break;
            }
        }
    }

    /**
     * 渲染合并转发消息
     *
     * @param message
     */
    private void renderMergedMessage(Message message) {
        MergeContent mergeContent = (MergeContent) message.getContent();

        // 合并转发标题
        messageBinding.mergedMsgTitle.setText(mergeContent.getMsgTitle());

        // 合并转发消息摘要
        String messageDigest = StringUtils.join(mergeContent.getOriginalMessageDigest(), "\n");
        messageBinding.mergedMsgDigest.setText(messageDigest);

        // 打开完整的合并转发消息列表页
        messageBinding.mergedMsgLayout.setOnClickListener(v -> {
            Intent intent = new Intent(context, MergedMessageActivity.class);
            intent.putExtra(MergedMessageActivity.SESSION_ID, mergeContent.getOriginalCid());
            intent.putStringArrayListExtra(MergedMessageActivity.MESSAGE_IDS,
                    Lists.newArrayList(mergeContent.getOriginalMessageIds()));
            context.startActivity(intent);
        });

        if (context instanceof ChatActivity) {
            messageBinding.mergedMsgLayout.setOnLongClickListener(v -> showMessagePopupMenu(message));
        }
    }

    /**
     * 渲染未知自定义消息
     */
    private void renderUnknownCustomMessage() {
        hideOtherAndShowMe(UNKNOWN_TYPE);
        renderUnknownMessage("[未知自定义消息]");
    }

    /**
     * 渲染未知消息
     *
     * @param message
     */
    private void renderUnknownMessage(String message) {
        messageBinding.unknownMsg.setText(message);
    }

    /**
     * 渲染附着在消息体下方的快捷回复表情列表
     *
     * @param message
     */
    private void renderInstantReplyEmojiList(Message message) {
        RecyclerView listView = messageBinding.instantReplyEmojiListView;

        List<InstantReplyFaceInfo> faceInfos = message.getInstantReplyFaceInfos();
        if (CollectionUtils.isEmpty(faceInfos)) {
            listView.setVisibility(View.GONE);
            setMessageLayoutPadding(false);
            return;
        }

        listView.setVisibility(View.VISIBLE);
        setMessageLayoutPadding(true);

        // 初始化表情列表
        InstantReplyEmojiListAdapter listAdapter = new InstantReplyEmojiListAdapter(context,
                new InstantReplyEmojiItemHolder.OnEmojiItemActionListener() {
                    @Override
                    public void onClickItem(InstantReplyEmojiItem emojiItem) {
                        if (context instanceof ChatActivity) {
                            // 绑定表情的点击事件，如果已发送表情，则撤回，否则发送
                            String currentUserId = AlipayCcmIMClient.getInstance().getCurrentUserId();
                            if (emojiItem.hasSentEmoji(currentUserId)) {
                                ((ChatActivity) context).recallInstantReplyEmoji(message, emojiItem.getEmoji());
                            } else {
                                ((ChatActivity) context).sendInstantReplyEmoji(message, emojiItem.getEmoji());
                            }
                        }
                    }

                    @Override
                    public void onClickAddEmoji() {
                        // 打开表情选择面板
                        showMessagePopupMenu(message);
                    }
                });

        listView.setAdapter(listAdapter);
        listView.setLayoutManager(new LinearLayoutManager(context));

        // 批量查询表情发送人的用户信息
        List<String> senderUserIds = Lists.newArrayList(faceInfos.stream().map(info -> info.getFrom().getUid()).collect(
                Collectors.toSet()));
        UserCacheManager.getInstance().batchQueryUsers(senderUserIds, new ImCallback<List<UserInfoVO>>() {
            @Override
            public void onSuccess(List<UserInfoVO> data) {
                handler.post(() -> {
                    Map<String, UserInfoVO> senderUserInfoMap = data.stream().collect(
                            Collectors.toMap(UserInfoVO::getUserId, info -> info));

                    // 渲染表情列表
                    listAdapter.setItems(
                            MessageConverter.convertInstantReplyEmojiList(faceInfos, senderUserInfoMap));
                    listAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onError(String errorCode, String message, Throwable t) {

            }
        });
    }

    /**
     * 给原本没有边框的消息体加一个边框，用于容纳快捷回复表情列表
     *
     * @param needPadding
     */
    private void setMessageLayoutPadding(boolean needPadding) {
        int padding = needPadding ? 20 : 0;

        messageBinding.imageMsgLayout.setPadding(padding, padding, padding, padding);
        messageBinding.videoMsgLayout.setPadding(padding, padding, padding, padding);
        messageBinding.fileMsgLayout.setPadding(padding, padding, padding, padding);
        messageBinding.locationMsgLayout.setPadding(padding, padding, padding, padding);
        messageBinding.cardMsgLayout.setPadding(padding, padding, padding, padding);
    }

    /**
     * 渲染引用的消息内容
     *
     * @param message
     */
    private void renderQuotedMessage(Message message) {
        if (!MsgUtils.isQuoteReplyMessage(message) || topicMessageMode) {
            messageBinding.quotedMessageLayout.setVisibility(View.GONE);
            return;
        }

        messageBinding.quotedMessageLayout.setVisibility(View.VISIBLE);

        QuoteReplyInfo quoteReplyInfo = message.getQuoteReplyInfo();
        messageBinding.quotedMessageSender.setText(
                (quoteReplyInfo.getQuoteMessageSender() == null) ? StringUtils.EMPTY : quoteReplyInfo.getQuoteMessageSender().getNickName());
        messageBinding.quotedMessageContent.setText(
                MessageUtil.getContentText(quoteReplyInfo.getQuoteContentTypeCode(), quoteReplyInfo.getQuoteContent()));

        // 点击引用消息时，打开话题消息列表
        // 注：查看合并转发消息时，需要禁用该点击事件
        if (!(context instanceof MergedMessageActivity)) {
            messageBinding.quotedMessageLayout.setOnClickListener(
                    v -> renderTopicMessageList(quoteReplyInfo.getTopicId()));
        }
    }

    private void renderTopicMessageList(String topicId) {
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();

        if (c == null) {
            return;
        }

        TopicMessageListDialog dialog = new TopicMessageListDialog(context, c, topicId);
        dialog.show(((AppCompatActivity) context).getSupportFragmentManager(), "");
    }

    /**
     * 多媒体文件下载回调
     */
    private interface DownloadCallback {
        void onSuccess(String filePath);

        void onError(String errorCode, String message, Throwable t);
    }

}
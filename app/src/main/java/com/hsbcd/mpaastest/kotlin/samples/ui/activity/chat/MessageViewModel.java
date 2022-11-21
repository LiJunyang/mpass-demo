/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat;

import android.os.Handler;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.alipay.fc.ccmimplus.common.message.domain.MessageContentType;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.InstantReplyFaceContent;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.InstantReplyFaceInfo;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.QuoteReplyInfo;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.ack.AckBatchMessageRead;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.ack.AckMessageRead;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.ack.AckMessageRecalled;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.cmd.CmdRecall;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.cmd.CmdRecallInstantReplyFace;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.cmd.CmdSetAllMsgRead;
import com.alipay.fc.ccmimplus.common.service.facade.domain.message.notify.NotifyMessageReach;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageReadStatusEnum;
import com.alipay.fc.ccmimplus.common.service.facade.model.User;
import com.alipay.fc.ccmimplus.common.service.facade.result.vo.UserInfoVO;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.hsbcd.mpaastest.kotlin.samples.model.LiveDataResult;
import com.hsbcd.mpaastest.kotlin.samples.model.MediaSendingProgress;
import com.hsbcd.mpaastest.kotlin.samples.model.ReceiveMessageResult;
import com.hsbcd.mpaastest.kotlin.samples.model.SendMessageResult;
import com.alipay.fc.ccmimplus.sdk.core.client.AlipayCcmIMClient;
import com.alipay.fc.ccmimplus.sdk.core.client.ImCallback;
import com.alipay.fc.ccmimplus.sdk.core.client.ImMessageQueryCallback;
import com.alipay.fc.ccmimplus.sdk.core.config.face.FaceIconEmoji;
import com.alipay.fc.ccmimplus.sdk.core.connection.Connection;
import com.alipay.fc.ccmimplus.sdk.core.enums.MsgFetchDirectionEnum;
import com.alipay.fc.ccmimplus.sdk.core.executor.AsyncExecutorService;
import com.alipay.fc.ccmimplus.sdk.core.message.MessageListener;
import com.alipay.fc.ccmimplus.sdk.core.message.MessageManager;
import com.alipay.fc.ccmimplus.sdk.core.message.QueryHistoryMessageRequest;
import com.alipay.fc.ccmimplus.sdk.core.message.SendMessageCallback;
import com.alipay.fc.ccmimplus.sdk.core.message.content.CardMessageContent;
import com.alipay.fc.ccmimplus.sdk.core.message.content.CustomMessageContent;
import com.alipay.fc.ccmimplus.sdk.core.message.content.RichTextMessageContent;
import com.alipay.fc.ccmimplus.sdk.core.message.content.TextMessageContent;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.alipay.fc.ccmimplus.sdk.core.util.UrlUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.user.UserCacheManager;
import com.hsbcd.mpaastest.kotlin.samples.util.ConnectionUtil;
import com.hsbcd.mpaastest.kotlin.samples.util.MessageUtil;
import com.amap.api.location.AMapLocation;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 消息数据层
 *
 * @author liyalong
 * @version MessageViewModel.java, v 0.1 2022年08月03日 11:54 liyalong
 */
public class MessageViewModel extends ViewModel implements MessageListener {

    private static final int QUERY_PAGE_SIZE = 20;

    private MutableLiveData<ReceiveMessageResult> receiveMessageResult = new MutableLiveData<>();

    private MutableLiveData<SendMessageResult> sendMessageResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult<List<Message>>> messageListResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult<List<Message>>> topicMessageListResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult> transferMessageResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult<List<UserInfoVO>>> memberListResult = new MutableLiveData<>();

    private MutableLiveData<LiveDataResult<Message>> sendInstantReplyEmojiResult = new MutableLiveData<>();

    private MutableLiveData<Pair<Message, List<InstantReplyFaceInfo>>> notifyInstantReplyEmojiResult = new MutableLiveData<>();

    private long lastQueryHisMsgId = 0;

    private Handler handler = new Handler();

    public MessageViewModel() {
        AlipayCcmIMClient.getInstance().getMessageManager().registerUserListener(this);
    }

    // ~~~ 消息查询逻辑 start

    /**
     * 查询指定会话的历史消息列表
     *
     * @param c
     * @param init
     */
    public void queryHistoryMessages(Conversation c, boolean init) {
        if (init) {
            lastQueryHisMsgId = c.getLastMsgId();
        }

        QueryHistoryMessageRequest request = new QueryHistoryMessageRequest();
        request.setCid(c.getCid());
        request.setLastMsgId(lastQueryHisMsgId);
        request.setPageSize(QUERY_PAGE_SIZE);
        Date now = new Date();
        request.setStartTime(getStartDate(now).getTime());
        request.setEndTime(now.getTime());

        AsyncExecutorService.getInstance().execute(() -> {
            MessageManager.getInstance().queryHistoryMessage(request, new ImMessageQueryCallback() {
                @Override
                public void onQueryResult(boolean hasNextPage, long nextMsgId, List<Message> messages) {
                    if (hasNextPage) {
                        lastQueryHisMsgId = nextMsgId;
                    } else if (CollectionUtils.isNotEmpty(messages)) {
                        // 如果没有下一页了，把游标置在最老的一条消息之前，这样下一次查询就会返回空结果
                        lastQueryHisMsgId = Long.valueOf(messages.get(0).getId()) - 1;
                    }

                    // 通知ui线程
                    LiveDataResult result = new LiveDataResult(true, messages);
                    messageListResult.postValue(result);

                    // 批量设置消息已读
                    setMessageStatusRead(c, messages);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    String msg = String.format("查询历史消息列表失败: %s/%s/%s", errorCode, message, t.getMessage());
                    LiveDataResult result = new LiveDataResult(false, msg);
                    messageListResult.postValue(result);
                }
            });
        });
    }

    /**
     * 获取查询历史消息的起始时间
     *
     * @param now
     * @return
     */
    private Date getStartDate(Date now) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.DATE, calendar.get(Calendar.DATE) - 180);
        return calendar.getTime();
    }

    /**
     * 查询指定密聊会话的历史消息列表
     *
     * @param c
     * @param init
     */
    public void querySecretChatHistoryMessages(Conversation c, boolean init) {
        if (init) {
            lastQueryHisMsgId = c.getLastMsgId();
        }

        AsyncExecutorService.getInstance().execute(() -> {
            MessageManager.getInstance().querySecretChatHistoryMessage(c.getCid(), lastQueryHisMsgId, QUERY_PAGE_SIZE,
                    MsgFetchDirectionEnum.DIRECTION_BACK, new ImMessageQueryCallback() {
                        @Override
                        public void onQueryResult(boolean hasNextPage, long nextMsgId, List<Message> messages) {
                            if (hasNextPage) {
                                lastQueryHisMsgId = nextMsgId;
                            } else if (CollectionUtils.isNotEmpty(messages)) {
                                // 如果没有下一页了，把游标置在最老的一条消息之前，这样下一次查询就会返回空结果
                                lastQueryHisMsgId = Long.valueOf(messages.get(0).getId()) - 1;
                            }

                            // 通知ui线程
                            LiveDataResult result = new LiveDataResult(true, messages);
                            messageListResult.postValue(result);

                            // 批量设置消息已读
                            setMessageStatusRead(c, messages);
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            String msg = String.format("查询密聊历史消息列表失败: %s/%s/%s", errorCode, message, t.getMessage());
                            LiveDataResult result = new LiveDataResult(false, msg);
                            messageListResult.postValue(result);
                        }
                    });
        });
    }

    /**
     * 批量查询消息列表
     *
     * @param cid
     * @param messageIds
     */
    public void queryMessageList(String cid, List<String> messageIds) {
        AsyncExecutorService.getInstance().execute(() -> {
            MessageManager.getInstance().queryMultiplyMessageListFromRemote(cid, messageIds,
                    new ImCallback<List<Message>>() {
                        @Override
                        public void onSuccess(List<Message> data) {
                            LiveDataResult result = new LiveDataResult(true, data);
                            messageListResult.postValue(result);
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            String msg = String.format("查询消息列表失败: %s/%s/%s", errorCode, message, t.getMessage());
                            LiveDataResult result = new LiveDataResult(false, msg);
                            messageListResult.postValue(result);
                        }
                    });
        });
    }

    /**
     * 查询话题消息列表
     *
     * @param cid
     * @param topicId
     * @param pageIndex
     */
    public void queryTopicMessageList(String cid, String topicId, int pageIndex) {
        AsyncExecutorService.getInstance().execute(() -> {
            MessageManager.getInstance().queryRemoteTopicMessages(cid, topicId, pageIndex, QUERY_PAGE_SIZE,
                    new ImCallback<List<Message>>() {
                        @Override
                        public void onSuccess(List<Message> data) {
                            LiveDataResult result = new LiveDataResult(true, data);
                            topicMessageListResult.postValue(result);
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            String msg = String.format("查询话题消息列表失败: %s/%s/%s", errorCode, message, t.getMessage());
                            LiveDataResult result = new LiveDataResult(false, msg);
                            topicMessageListResult.postValue(result);
                        }
                    });
        });
    }

    public void queryMessageUnReadUsers(String cid, String msgId) {
        queryMessageReadOrUnReadUsers(cid, msgId, MessageReadStatusEnum.UN_READ);
    }

    public void queryMessageReadUsers(String cid, String msgId) {
        queryMessageReadOrUnReadUsers(cid, msgId, MessageReadStatusEnum.READ);
    }

    public void queryMessageReadOrUnReadUsers(String cid, String msgId, MessageReadStatusEnum readStatus) {
        AsyncExecutorService.getInstance().execute(() -> {
            MessageManager.getInstance().queryOneMessageReadOrUnReadUsers(cid, msgId, readStatus, 1, 100,
                    new ImCallback<List<UserInfoVO>>() {
                        @Override
                        public void onSuccess(List<UserInfoVO> data) {
                            LiveDataResult result = new LiveDataResult(true, data);
                            memberListResult.postValue(result);
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            String msg = String.format("查询消息%s列表失败: %s/%s/%s", readStatus.getDesc(), errorCode, message,
                                    t.getMessage());
                            LiveDataResult result = new LiveDataResult(false, msg);
                            memberListResult.postValue(result);
                        }
                    });
        });
    }

    // ~~~ 消息查询逻辑 end

    // ~~~ 消息操作逻辑 start

    /**
     * 设置消息为已读
     *
     * @param c
     * @param message
     */
    public void setMessageStatusRead(Conversation c, Message message) {
        setMessageStatusRead(c, Arrays.asList(message));
    }

    /**
     * 批量设置消息为已读
     *
     * @param c
     * @param messages
     */
    public void setMessageStatusRead(Conversation c, List<Message> messages) {
        AsyncExecutorService.getInstance().execute(() -> {
            String currentUserId = AlipayCcmIMClient.getInstance().getInitConfig().getUserId();
            MessageManager.getInstance().setMessagesStatusRead(c, currentUserId, messages);
        });
    }

    /**
     * 指定会话的所有消息全部设为已读
     *
     * @param c
     */
    public void setAllMessageStatusRead(Conversation c) {
        AsyncExecutorService.getInstance().execute(() -> {
            MessageManager.getInstance().setAllUnReadMessagesToStatusReadByConversation(c,
                    new SendMessageCallback<Message>() {
                        @Override
                        public void onSuccess(Message data) {
                            Log.i(LoggerName.VIEW_MODEL, String.format("设置全部已读成功: %s", c.getCid()));
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            Log.e(LoggerName.VIEW_MODEL, String.format("设置全部已读失败: %s", c.getCid()), t);
                        }
                    });
        });
    }

    public void sendTextMessage(String textMsg, List<String> atUserIds) {
        // 如果是纯url，则发送url消息
        if (UrlUtils.isPureUrl(textMsg)) {
            Log.i("message", "send url message (" + textMsg + ")");
            sendUrlMessage(textMsg, textMsg);
            return;
        }

        // TODO 如果是单个表情，则发送表情消息

        Log.i("message", "send text message (" + textMsg + ")");
        TextMessageContent content = buildTextMessageContent(textMsg, atUserIds);

        AlipayCcmIMClient.getInstance().executeAsync(() ->
                MessageManager.getInstance().sendSingleMessage(content, new CustomSendMessageCallback()));
    }

    private TextMessageContent buildTextMessageContent(String textMsg, List<String> atUserIds) {
        TextMessageContent content = new TextMessageContent(textMsg);

        // 填充@用户信息
        if (CollectionUtils.isNotEmpty(atUserIds)) {
            Set<User> atUsers = atUserIds.stream().map(userId -> new User(userId)).collect(Collectors.toSet());
            content.setAtUsers(atUsers);
        }

        Map<String, Object> extInfo = new HashMap<>();
        content.setExtInfo(extInfo);

        return content;
    }

    public void sendImageMessage(File imageFile) {
        AlipayCcmIMClient.getInstance().executeAsync(() ->
                MessageManager.getInstance().sendImageMessage(imageFile, imageFile.getName(),
                        new CustomSendMessageCallback()));
    }

    public void sendVoiceMessage(File voiceFile) {
        AlipayCcmIMClient.getInstance().executeAsync(() ->
                MessageManager.getInstance().sendVoiceMessage(voiceFile, voiceFile.getName(),
                        new CustomSendMessageCallback()));
    }

    public void sendVideoMessage(File videoFile) {
        AlipayCcmIMClient.getInstance().executeAsync(() ->
                MessageManager.getInstance().sendVideoMessage(videoFile, videoFile.getName(),
                        new CustomSendMessageCallback()));
    }

    public void sendFileMessage(File file) {
        AlipayCcmIMClient.getInstance().executeAsync(() ->
                MessageManager.getInstance().sendFileMessage(file, file.getName(), new CustomSendMessageCallback()));
    }

    public void sendLocationMessage(File snapshotFile, AMapLocation location) {
        AlipayCcmIMClient.getInstance().executeAsync(() ->
                MessageManager.getInstance().sendLocationMessage(snapshotFile, location.getLatitude(),
                        location.getLongitude(), location.getAddress(), null, null, new CustomSendMessageCallback()));
    }

    public void sendUrlMessage(String title, String url) {
        AlipayCcmIMClient.getInstance().executeAsync(() ->
                MessageManager.getInstance().sendUrlMessage(title, url, new CustomSendMessageCallback()));
    }

    public void sendCustomMessage(String dataType, String dataVersion, String data) {
        AlipayCcmIMClient.getInstance().executeAsync(() ->
                MessageManager.getInstance().sendCustomMessage(dataType, dataVersion, data.getBytes(),
                        new CustomSendMessageCallback()));
    }

    public void sendCustomCommandMessage(String dataType, String dataVersion, String data) {
        AsyncExecutorService.getInstance().execute(() -> {
            CustomMessageContent content = new CustomMessageContent(dataType, dataVersion, data.getBytes());
            MessageManager.getInstance().sendCustomCommandMessage(content, false, new SendMessageCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    Log.i(LoggerName.UI, "custom command send success");
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    Log.e(LoggerName.UI,
                            String.format("custom command send fail: %s/%s/%s", errorCode, message, t.getMessage()), t);
                }
            });
        });
    }

    public void sendRichTextMessage(String text) {
        RichTextMessageContent content = new RichTextMessageContent("Rich Text", text);
        AsyncExecutorService.getInstance().execute(
                () -> MessageManager.getInstance().sendSingleMessage(content, new CustomSendMessageCallback()));
    }

    public void sendSimpleCardMessage() {
        AsyncExecutorService.getInstance().execute(() -> {
            MessageManager mm = MessageManager.getInstance();
            CardMessageContent content = new CardMessageContent("卡片消息");
            content.setTitle("测试");
            mm.sendSingleMessage(content, new CustomSendMessageCallback());
        });
    }

    /**
     * 重发消息
     *
     * @param message
     */
    public void reSendMessage(Message message) {
        AlipayCcmIMClient.getInstance().executeAsync(() ->
                MessageManager.getInstance().reSendLocalFailedMessage(message, new CustomSendMessageCallback()));
    }

    /**
     * 转发指定消息到其他会话
     *
     * @param originalCid
     * @param messageIds
     * @param toCidSet
     * @param isMerge
     */
    public void transferMessage(String originalCid, List<Long> messageIds, Set<String> toCidSet, boolean isMerge) {
        AsyncExecutorService.getInstance().execute(() -> {
            MessageManager.getInstance().transferMessages(originalCid, messageIds, toCidSet, isMerge,
                    new SendMessageCallback<Message>() {
                        @Override
                        public void onSuccess(Message data) {
                            LiveDataResult result = new LiveDataResult(true);
                            transferMessageResult.postValue(result);
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            String msg = String.format("%s/%s/%s", errorCode, message, t.getMessage());
                            LiveDataResult result = new LiveDataResult(false, msg);
                            transferMessageResult.postValue(result);
                        }
                    });
        });
    }

    /**
     * 撤回消息
     *
     * @param message
     */
    public void recallMessage(Message message) {
        AlipayCcmIMClient.getInstance().executeAsync(() -> {
            MessageManager.getInstance().recallMessage(message, new SendMessageCallback<Message>() {
                @Override
                public void onSuccess(Message data) {
                    // TODO 暂不处理
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                }
            });
        });
    }

    /**
     * 删除消息
     * 注：服务端是逻辑删除，在消息上打删除标
     *
     * @param message
     */
    public void deleteMessage(Message message) {
        AlipayCcmIMClient.getInstance().executeAsync(() -> {
            MessageManager.getInstance().deleteMessage(message);
        });
    }

    /**
     * 批量删除消息
     *
     * @param c
     * @param messages
     */
    public void batchDeleteMessage(Conversation c, List<Message> messages) {
        //messages.forEach(m -> deleteMessage(m));

        AlipayCcmIMClient.getInstance().executeAsync(() -> {
            MessageManager.getInstance().batchDeleteMessage(c.getCid(), messages, new ImCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    Log.i(LoggerName.UI, "消息批量删除成功: " + data);
                }

                @Override
                public void onError(String errorCode, String message, Throwable t) {
                    Log.e(LoggerName.UI, String.format("消息批量删除失败: %s/%s/%s", errorCode, message, t.getMessage()));
                }
            });
        });
    }

    /**
     * 发送快捷回复表情
     *
     * @param c
     * @param message
     * @param emoji
     */
    public void sendInstantReplyEmoji(Conversation c, Message message, FaceIconEmoji emoji) {
        // 如果当前用户已经发过该表情了，则不再重复发送
        List<InstantReplyFaceInfo> faceInfos = message.getInstantReplyFaceInfos();
        if (CollectionUtils.isNotEmpty(faceInfos)) {
            Optional<InstantReplyFaceInfo> alreadySendEmoji = faceInfos.stream().filter(f ->
                    StringUtils.equals(f.getFaceContent().getIconId(), emoji.getGlobalUniqueId()) && StringUtils.equals(
                            f.getFrom().getUid(), AlipayCcmIMClient.getInstance().getCurrentUserId())).findFirst();
            if (alreadySendEmoji.isPresent()) {
                return;
            }
        }

        AlipayCcmIMClient.getInstance().executeAsync(() -> {
            UserInfoVO currentUserInfo = UserCacheManager.getInstance().getCurrentUserInfo();

            MessageManager.getInstance().quickFaceReplyMessage(c, message, currentUserInfo.getNickName(),
                    currentUserInfo.getNickName(), emoji.getGlobalUniqueId(), emoji.getCn(),
                    new SendMessageCallback<List<InstantReplyFaceInfo>>() {
                        @Override
                        public void onSuccess(List<InstantReplyFaceInfo> data) {
                            // 更新内存数据：该消息的快捷回复表情列表
                            message.setInstantReplyFaceInfos(data);

                            LiveDataResult result = new LiveDataResult(true, message);
                            sendInstantReplyEmojiResult.postValue(result);
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            String msg = String.format("发送快捷回复表情失败: %s/%s/%s", errorCode, message, t.getMessage());
                            LiveDataResult result = new LiveDataResult(false, msg);
                            sendInstantReplyEmojiResult.postValue(result);
                        }
                    });
        });
    }

    /**
     * 撤回快捷回复表情
     *
     * @param c
     * @param message
     * @param emoji
     */
    public void recallInstantReplyEmoji(Conversation c, Message message, FaceIconEmoji emoji) {
        AlipayCcmIMClient.getInstance().executeAsync(() -> {
            MessageManager.getInstance().recallQuickFaceReplyMessage(c, message, emoji.getGlobalUniqueId(),
                    new SendMessageCallback<List<InstantReplyFaceInfo>>() {
                        @Override
                        public void onSuccess(List<InstantReplyFaceInfo> data) {
                            // 更新内存数据：该消息的快捷回复表情列表
                            message.setInstantReplyFaceInfos(data);

                            LiveDataResult result = new LiveDataResult(true, message);
                            sendInstantReplyEmojiResult.postValue(result);
                        }

                        @Override
                        public void onError(String errorCode, String message, Throwable t) {
                            String msg = String.format("发送快捷回复表情失败: %s/%s/%s", errorCode, message, t.getMessage());
                            LiveDataResult result = new LiveDataResult(false, msg);
                            sendInstantReplyEmojiResult.postValue(result);
                        }
                    });
        });
    }

    /**
     * 发送引用回复的文本消息
     *
     * @param c
     * @param quotedMessage
     * @param replyText
     */
    public void sendQuoteReplyTextMessage(Conversation c, Message quotedMessage, String replyText,
                                          List<String> atUserIds) {
        AlipayCcmIMClient.getInstance().executeAsync(() ->
                MessageManager.getInstance().quoteReplyTextMessage(c.getCid(), quotedMessage.getId(), replyText,
                        new CustomSendMessageCallback(c, quotedMessage, atUserIds)));
    }

    // ~~~ 消息操作逻辑 end

    // ~~~ IM消息监听逻辑 start

    @Override
    public void onNewMessage(MessageListener messageListener, Connection connection, Message message) {
        if (!(messageListener instanceof MessageViewModel)) {
            return;
        }

        postNewMessage(ReceiveMessageResult.Type.Normal, message);
    }

    @Override
    public void onMessageSendSuccess(Connection connection, Message message) {
        // 忽略心跳包的已达通知
        NotifyMessageReach content = (NotifyMessageReach) message.getContent();
        if (StringUtils.equals(content.getMessageContentType(), MessageContentType.HB_HEARTBEAT.name())) {
            return;
        }

        postNewMessage(ReceiveMessageResult.Type.NotifyMessageReach, message);
    }

    @Override
    public void onAckMessageRead(Connection connection, Message message, AckMessageRead ackRead) {
        postNewMessage(ReceiveMessageResult.Type.AckMessageRead, message);
    }

    @Override
    public void onAckBatchMessageRead(Connection connection, Message message,
                                      AckBatchMessageRead batchMessageRead) {
        postNewMessage(ReceiveMessageResult.Type.AckBatchMessageRead, message);
    }

    @Override
    public void onAckMessageRecall(Connection connection, Message message, AckMessageRecalled ackRecall) {
        postNewMessage(ReceiveMessageResult.Type.AckMessageRecalled, message);
    }

    @Override
    public void onCmdRecall(Connection connection, Message message, CmdRecall content) {
        postNewMessage(ReceiveMessageResult.Type.CmdRecall, message);
    }

    @Override
    public void onCmdSetAllToRead(Connection connection, Message message, CmdSetAllMsgRead content) {
        postNewMessage(ReceiveMessageResult.Type.CmdSetAllMsgRead, message);
    }

    @Override
    public void onTextReply(Connection connection, Message message, QuoteReplyInfo replyInfo) {
        // 忽略自己发送的引用回复消息，避免重复渲染
        if (StringUtils.equals(MessageUtil.getSenderConnectId(message), ConnectionUtil.getCurrentConnectionId())) {
            return;
        }

        postNewMessage(ReceiveMessageResult.Type.QuoteReplyInfo, message);
    }

    @Override
    public void onQuickFaceReply(Connection connection, Message message, InstantReplyFaceContent reply,
                                 List<InstantReplyFaceInfo> newestReplies) {
        postNotifyInstantReplyEmoji(message, newestReplies);
    }

    @Override
    public void onQuickFaceReplyRevoke(Connection connection, Message message, CmdRecallInstantReplyFace recall,
                                       List<InstantReplyFaceInfo> newestReplies) {
        postNotifyInstantReplyEmoji(message, newestReplies);
    }

    /**
     * 各类新消息统一投递到UI线程，再分别处理
     *
     * @param message
     */
    private void postNewMessage(ReceiveMessageResult.Type type, Message message) {
        if (needPostNewMessage(message)) {
            ReceiveMessageResult result = new ReceiveMessageResult(type, message);
            receiveMessageResult.postValue(result);
        }
    }

    private void postNotifyInstantReplyEmoji(Message message, List<InstantReplyFaceInfo> faceInfos) {
        if (needPostNewMessage(message)) {
            Pair<Message, List<InstantReplyFaceInfo>> result = Pair.create(message, faceInfos);
            notifyInstantReplyEmojiResult.postValue(result);
        }
    }

    private boolean needPostNewMessage(Message message) {
        Conversation c = AlipayCcmIMClient.getInstance().getConversationManager().getCurrentConversation();
        if (c == null) {
            return false;
        }

        if (!StringUtils.equalsIgnoreCase(message.getSid(), c.getCid())) {
            Log.i(LoggerName.VIEW_MODEL, String.format("收到其他会话的消息: %s", message.getSid()));
            return false;
        }

        return true;
    }

    // ~~~ IM消息监听逻辑 end

    // ~~~ getter & setter

    public MutableLiveData<ReceiveMessageResult> getReceiveMessageResult() {
        return receiveMessageResult;
    }

    public MutableLiveData<SendMessageResult> getSendMessageResult() {
        return sendMessageResult;
    }

    public MutableLiveData<LiveDataResult<List<Message>>> getMessageListResult() {
        return messageListResult;
    }

    public MutableLiveData<LiveDataResult<List<Message>>> getTopicMessageListResult() {
        return topicMessageListResult;
    }

    public MutableLiveData<LiveDataResult> getTransferMessageResult() {
        return transferMessageResult;
    }

    public MutableLiveData<LiveDataResult<List<UserInfoVO>>> getMemberListResult() {
        return memberListResult;
    }

    public MutableLiveData<LiveDataResult<Message>> getSendInstantReplyEmojiResult() {
        return sendInstantReplyEmojiResult;
    }

    public MutableLiveData<Pair<Message, List<InstantReplyFaceInfo>>> getNotifyInstantReplyEmojiResult() {
        return notifyInstantReplyEmojiResult;
    }

    private class CustomSendMessageCallback implements SendMessageCallback<Message> {
        private Message message;

        private Message quotedMessage;

        private Conversation c;

        private List<String> atUserIds;

        public CustomSendMessageCallback() {
        }

        public CustomSendMessageCallback(Conversation c, Message quotedMessage, List<String> atUserIds) {
            this.c = c;
            this.quotedMessage = quotedMessage;
            this.atUserIds = atUserIds;
        }

        @Override
        public void beginSend(Message message) {
            this.message = message;

            setMessageFromUserInfo();
            setQuotedReplyInfo();
            setAtUsers();

            SendMessageResult result = new SendMessageResult(SendMessageResult.Status.BEGIN, message);
            // 如果直接postValue，一次发多条消息的情况(例如选中多个文件批量发送)可能导致页面消息丢失
            // 因此先post到UI线程，再走setValue触发livedata通知
            handler.post(() -> sendMessageResult.setValue(result));
        }

        @Override
        public void onProgress(long total, long sent, boolean done) {
            Log.i(LoggerName.UI, String.format("sending progress: %d/%d/%s", sent, total, done));

            SendMessageResult result = new SendMessageResult(SendMessageResult.Status.IN_PROGRESS, message);
            result.setProgress(new MediaSendingProgress(total, sent, done,
                    MessageContentType.forNumber(message.getContentTypeCode())));
            sendMessageResult.postValue(result);
        }

        @Override
        public void onSuccess(Message msg) {
            Log.i(LoggerName.UI, "message send success: " + msg.getId());

            SendMessageResult result = new SendMessageResult(SendMessageResult.Status.SUCCESS, message);
            result.setTopicId(getTopicId());
            sendMessageResult.postValue(result);
        }

        @Override
        public void onError(String errorCode, String errorMessage, Throwable t) {
            Log.e(LoggerName.UI, "message send fail: ", t);

            //String errorMsg = String.format("消息发送失败: %s/%s/%s", errorCode, message, t.getMessage());
            SendMessageResult result = new SendMessageResult(SendMessageResult.Status.FAIL, message, errorCode,
                    errorMessage);
            sendMessageResult.postValue(result);
        }

        private void setMessageFromUserInfo() {
            UserInfoVO currentUser = UserCacheManager.getInstance().getCurrentUserInfo();
            if (currentUser == null) {
                return;
            }

            Message.Actor from = message.getFrom();

            from.setAvatarUrl(currentUser.getAvatarUrl());
            from.setName(currentUser.getNickName());
            from.setNickName(currentUser.getNickName());
        }

        private void setQuotedReplyInfo() {
            if (quotedMessage == null) {
                return;
            }

            // 组装引用回复信息，更新到内存模型，仅用于发送回复消息后的消息渲染
            QuoteReplyInfo quoteReplyInfo = new QuoteReplyInfo();

            quoteReplyInfo.setTopicId(getTopicId());
            quoteReplyInfo.setQuoteMessageId(quotedMessage.getId());
            quoteReplyInfo.setQuoteMessageSender(
                    new Message.Actor(quotedMessage.getFrom().getChannel(), quotedMessage.getFrom().getUid()));
            quoteReplyInfo.getQuoteMessageSender().setNickName(MessageUtil.getSenderUserName(c, quotedMessage));
            quoteReplyInfo.setQuoteContentType(quotedMessage.getContentType());
            quoteReplyInfo.setQuoteContentTypeCode(quotedMessage.getContentTypeCode());
            quoteReplyInfo.setQuoteContent(quotedMessage.getContent());

            message.setQuoteReplyInfo(quoteReplyInfo);
        }

        private String getTopicId() {
            if (quotedMessage == null) {
                return null;
            }

            if (quotedMessage.getQuoteReplyInfo() != null) {
                return quotedMessage.getQuoteReplyInfo().getTopicId();
            }

            if (quotedMessage.getQuotedInfo() != null) {
                return quotedMessage.getQuotedInfo().getTopicId();
            } else {
                return quotedMessage.getId();
            }
        }

        private void setAtUsers() {
            // 填充@用户信息
            if (CollectionUtils.isNotEmpty(atUserIds)) {
                List<User> atUsers = atUserIds.stream().map(userId -> new User(userId)).collect(Collectors.toList());
                message.setAtUsers(atUsers);
            }
        }
    }

}
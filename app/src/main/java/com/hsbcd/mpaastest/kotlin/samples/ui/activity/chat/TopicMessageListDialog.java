/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.alipay.fc.ccmimplus.common.service.facade.domain.message.Message;
import com.alipay.fc.ccmimplus.common.service.facade.enums.MessageDeleteStatusEnum;
import com.alipay.fc.ccmimplus.sdk.core.config.face.FaceIconEmoji;
import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.alipay.fc.ccmimplus.sdk.core.util.CollectionUtils;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.TopicMessageListAdapter;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.emoji.EmojiInputView;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.BaseBottomSheetDialog;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.CustomOnScrollListener;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import cn.hsbcsd.mpaastest.databinding.DialogTopicMessageListBinding;

/**
 * 话题(引用回复)消息列表对话框
 *
 * @author liyalong
 * @version TopicMessageListDialog.java, v 0.1 2022年09月27日 10:56 liyalong
 */
public class TopicMessageListDialog extends BaseBottomSheetDialog {

    private Context context;

    private Conversation c;

    private String topicId;

    private DialogTopicMessageListBinding binding;

    protected TopicMessageListAdapter topicMessageListAdapter;

    private MessageViewModel messageViewModel;

    private Message originalMessage;

    private CustomOnScrollListener customOnScrollListener;

    public TopicMessageListDialog(Context context, Conversation c, String topicId) {
        super(false);
        
        this.context = context;
        this.c = c;
        this.topicId = topicId;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogTopicMessageListBinding.inflate(inflater);

        // 绑定组件动作
        bindAction();

        // 绑定消息输入组件
        bindInputText();
        bindEmojiGrid();

        // 绑定聊天消息数据
        // 注意ViewModelStoreOwner使用宿主activity，这样有数据变更时可以同步更新到宿主activity
        messageViewModel = new ViewModelProvider(getActivity()).get(MessageViewModel.class);
        bindMessageViewModel();

        // 初始化话题消息列表
        initMessageList();

        // 加载话题消息列表
        messageViewModel.queryTopicMessageList(c.getCid(), topicId, 1);

        return binding.getRoot();
    }

    private void bindAction() {
        binding.goBack.setOnClickListener(v -> super.dismiss());

        binding.locateOriginalMessage.setOnClickListener(v -> {
            ((ChatActivity) context).jumpToMessage(originalMessage);
            super.dismiss();
        });

        // 消息输入视图的切换动作
        bindInputViewSwitch();
    }

    private void bindInputViewSwitch() {
        // 点击文本输入框时，隐藏表情视图
        binding.inputText.setOnClickListener(v -> hideInputEmojiView());

        // 表情输入视图展开/隐藏
        binding.switchToEmoji.setOnClickListener(v -> showInputEmojiView());
        binding.switchEmojiToText.setOnClickListener(v -> hideInputEmojiView());
    }

    private void showInputEmojiView() {
        hideSoftKeyboard();

        binding.inputEmojiView.setVisibility(View.VISIBLE);

        binding.switchToEmoji.setVisibility(View.GONE);
        binding.switchEmojiToText.setVisibility(View.VISIBLE);
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void hideInputEmojiView() {
        binding.inputEmojiView.setVisibility(View.GONE);

        binding.switchToEmoji.setVisibility(View.VISIBLE);
        binding.switchEmojiToText.setVisibility(View.GONE);
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
            }
        });
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

    /**
     * 发送文本消息
     */
    public void sendTextMessage() {
        String textMsg = binding.inputText.getText().toString();
        if (StringUtils.isBlank(textMsg)) {
            return;
        }

        messageViewModel.sendQuoteReplyTextMessage(c, originalMessage, textMsg, null);
    }

    private void appendEmoji(FaceIconEmoji emoji) {
        binding.inputText.appendEmoji(emoji);
    }

    private void deleteEmoji() {
        binding.inputText.deleteLastItem();
    }

    private void initMessageList() {
        this.topicMessageListAdapter = new TopicMessageListAdapter(context);
        binding.messageListView.setAdapter(topicMessageListAdapter);

        binding.messageListView.setLayoutManager(new LinearLayoutManager(context));

        customOnScrollListener = new CustomOnScrollListener() {
            @Override
            public void onLoadMore(int pageIndex) {
                messageViewModel.queryTopicMessageList(c.getCid(), topicId, pageIndex);
            }

            @Override
            public void onRefresh() {
            }

            @Override
            public void onNoNextPage() {
                ToastUtil.makeToast((Activity) context, "没有更多消息了", 1000);
            }
        };

        binding.messageListView.addOnScrollListener(customOnScrollListener);

        // 点击消息列表的空白处时，隐藏所有展开的输入组件，只保留基础输入组件
        binding.messageListView.setOnClickListListener(() -> {
            hideSoftKeyboard();
            hideInputEmojiView();

            binding.switchToEmoji.setVisibility(View.VISIBLE);
            binding.switchEmojiToText.setVisibility(View.GONE);
        });
    }

    private void bindMessageViewModel() {
        // 拉取到话题消息列表后的动作
        messageViewModel.getTopicMessageListResult().observe(getViewLifecycleOwner(), result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast((Activity) context, result.getMessage(), 3000);
                return;
            }

            if (CollectionUtils.isEmpty(result.getData())) {
                return;
            }

            // 过滤掉已删除消息
            List<Message> messages = result.getData().stream()
                    .filter(m -> m.getDeleteStatus() == MessageDeleteStatusEnum.NORMAL)
                    .collect(Collectors.toList());

            // 记录话题原始消息
            Optional<Message> message = result.getData().stream().filter(m -> m.getQuotedInfo() != null).findFirst();
            if (message.isPresent()) {
                originalMessage = message.get();
            }

            // 如果话题消息列表对应的话题id与当前查询的话题id不一致，则直接忽略
            // 原因：由于ViewModelStoreOwner使用的是宿主activity，ViewModel的生命周期与activity相同；
            // 在二次进入dialog时，会立即触发一次observer，返回的数据用的是上一次的，可能与当前查询的话题不是同一个，因此这里兼容处理下
            if (originalMessage != null && !StringUtils.equals(originalMessage.getId(), topicId)) {
                return;
            }

            // 渲染话题消息列表
            topicMessageListAdapter.addAll(messages);
            topicMessageListAdapter.notifyDataSetChanged();
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
                default:
                    break;
            }
        });

        // 发送快捷回复表情后的动作
        messageViewModel.getSendInstantReplyEmojiResult().observe(this, result -> {
            if (!result.isSuccess()) {
                ToastUtil.makeToast((Activity) context, result.getMessage(), 3000);
                return;
            }

            // 重新渲染该消息，刷新快捷回复表情列表
            refreshMessage(result.getData());
        });
    }

    private void insertMessage(Message message) {
        topicMessageListAdapter.addOne(message);
        topicMessageListAdapter.notifyItemInserted(topicMessageListAdapter.getItemCount() - 1);

        binding.messageListView.scrollToBottom();
    }

    private void refreshMessage(Message message) {
        int index = topicMessageListAdapter.indexOfByMsgId(message.getTntInstId(), message.getSid(),
                Long.valueOf(message.getId()));
        if (index != -1) {
            topicMessageListAdapter.notifyItemChanged(index);
        }
    }

}
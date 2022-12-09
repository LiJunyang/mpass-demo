/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.text;

import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.widget.AppCompatEditText;

import cn.com.hsbc.hsbcchina.cert.R;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.alipay.fc.ccmimplus.sdk.core.config.face.FaceIconEmoji;
import com.hsbcd.mpaastest.kotlin.samples.util.ImageUtil;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自定义文本输入框，支持展示表情和@的人
 *
 * @author liyalong
 * @version ChatActivity.java, v 0.1 2022年10月13日 11:10 liyalong
 */
public class CustomTextInput extends AppCompatEditText {

    private Context context;

    public CustomTextInput(Context context) {
        super(context);
        this.context = context;
        bindOnKeyListener();
    }

    public CustomTextInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        bindOnKeyListener();
    }

    public CustomTextInput(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        bindOnKeyListener();
    }

    private void bindOnKeyListener() {
        this.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // 拦截删除事件，处理自定义删除逻辑
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                    deleteLastItem();
                    return true;
                }

                return false;
            }
        });
    }

    public void appendEmoji(FaceIconEmoji emoji) {
        ImageUtil.asyncLoadImage(context, emoji.getLocalFilePath(), (bitmap) -> {
            // 创建表情图片span
            String emojiStr = emoji.getCn();
            SpannableString ss = new SpannableString(emojiStr);
            ss.setSpan(new EmojiSpan(context, bitmap, DynamicDrawableSpan.ALIGN_CENTER, emojiStr), 0,
                    emojiStr.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            // 插入到当前光标位置
            CustomTextInput.this.getEditableText().insert(CustomTextInput.this.getSelectionStart(), ss);
        });
    }

    public void appendAtUser(String atUserId, String atUserName) {
        String atUserStr = String.format("@%s ", atUserName);
        int start = 0;
        int end = atUserStr.length();
        int flag = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

        // 创建@某人的span
        SpannableString ss = new SpannableString(atUserStr);
        ss.setSpan(new AtUserSpan(atUserId, atUserName), start, end, flag);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.ant_blue)), start, end, flag);
        ss.setSpan(new SelectionSpanWatcher<AtUserSpan>(AtUserSpan.class), start, end, flag);

        // 插入到当前光标位置
        this.getEditableText().insert(this.getSelectionStart(), ss);
    }

    /**
     * 删除当前光标前面的最后一个元素，可能是span，也可能是普通字符
     */
    public void deleteLastItem() {
        // 删除表情
        boolean deleteEmoji = deleteSpan(EmojiSpan.class);
        // 删除@的人
        boolean deleteAtUser = deleteSpan(AtUserSpan.class);

        // 删除单个普通字符
        if (!deleteEmoji && !deleteAtUser) {
            deleteChar();
        }
    }

    private <T> boolean deleteSpan(Class<T> clazz) {
        Editable editable = this.getEditableText();
        if (editable == null || editable.length() == 0) {
            return false;
        }

        // 如果当前光标前面是某个span，则删除整个span
        T[] spans = editable.getSpans(0, editable.length(), clazz);
        for (T span : spans) {
            Log.d(LoggerName.UI, String.format("deleteSpan: %d/%d/%d", this.getSelectionStart(),
                    editable.getSpanStart(span), editable.getSpanEnd(span)));

            if (editable.getSpanEnd(span) == this.getSelectionStart()) {
                editable.delete(editable.getSpanStart(span), editable.getSpanEnd(span));
                return true;
            }
        }

        return false;
    }

    private void deleteChar() {
        int position = this.getSelectionStart();
        if (position > 0) {
            this.getEditableText().delete(position - 1, position);
        }
    }

    public List<String> getAtUserIds() {
        Editable editable = this.getEditableText();
        if (editable == null || editable.length() == 0) {
            return Lists.newArrayList();
        }

        AtUserSpan[] atUserSpans = editable.getSpans(0, editable.length(), AtUserSpan.class);
        return Arrays.stream(atUserSpans).map(AtUserSpan::getAtUserId).collect(
                Collectors.toCollection(LinkedHashSet::new)).stream().collect(Collectors.toList());
    }

    /**
     * 实现无法在内部插入光标的span，避免删除字符时span被部分删除
     * <p>
     * 参考：
     * 1. https://www.jianshu.com/p/83176fb89aed
     * 2. https://blog.csdn.net/eclipsexys/article/details/119922575
     *
     * @param <T>
     */
    private class SelectionSpanWatcher<T> implements SpanWatcher {

        private int selStart = 0;

        private int selEnd = 0;

        private Class<T> clazz;

        public SelectionSpanWatcher(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public void onSpanAdded(Spannable text, Object what, int start, int end) {
        }

        @Override
        public void onSpanRemoved(Spannable text, Object what, int start, int end) {
        }

        @Override
        public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {
            // 如果光标尝试定位到span内部，则强制把光标挪到span边缘 (前边或者后边，离哪边近就挪到哪边)
            if (what == Selection.SELECTION_END && selEnd != nstart) {
                selEnd = nstart;
                T[] spans = text.getSpans(nstart, nend, clazz);
                if (spans.length > 0) {
                    int spanStart = text.getSpanStart(this);
                    int spanEnd = text.getSpanEnd(this);
                    int index = (Math.abs(selEnd - spanEnd) > Math.abs(selEnd - spanStart)) ? spanStart : spanEnd;
                    Selection.setSelection(text, Selection.getSelectionStart(text), index);
                }
            }

            if (what == Selection.SELECTION_START && selStart != nstart) {
                selStart = nstart;
                T[] spans = text.getSpans(nstart, nend, clazz);
                if (spans.length > 0) {
                    int spanStart = text.getSpanStart(this);
                    int spanEnd = text.getSpanEnd(this);
                    int index = (Math.abs(selStart - spanEnd) > Math.abs(selStart - spanStart)) ? spanStart : spanEnd;
                    Selection.setSelection(text, index, Selection.getSelectionEnd(text));
                }
            }
        }
    }

}
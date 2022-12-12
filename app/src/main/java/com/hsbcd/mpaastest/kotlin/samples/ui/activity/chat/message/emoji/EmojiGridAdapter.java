/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.emoji;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import cn.hsbcsd.mpaastest.R;
import com.hsbcd.mpaastest.kotlin.samples.constants.LoggerName;
import com.alipay.fc.ccmimplus.sdk.core.config.face.FaceIconEmoji;
import com.alipay.fc.ccmimplus.sdk.core.config.face.FaceIconManager;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * 表情图标网格视图适配器
 *
 * @author maping.mp
 * @version 1.0: EmojiGridAdapter.java, v 0.1 2021年11月03日 12:45 下午 maping.mp Exp $
 */
public class EmojiGridAdapter extends BaseAdapter {

    private Context context;

    private List<FaceIconEmoji> emojis = new ArrayList<>();

    private OnClickItemListener onClickItemListener;

    public EmojiGridAdapter(Context context) {
        this.context = context;

        this.emojis.addAll(FaceIconManager.getInstance().getEmojiList());
        Log.d(LoggerName.UI, "加载表情总数(" + emojis.size() + ")");

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return emojis.size();
    }

    @Override
    public Object getItem(int position) {
        return emojis.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView emojiItem;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.emoji_grid_item, null);
            emojiItem = (ImageView) convertView.findViewById(R.id.emoji_item);
            convertView.setTag(emojiItem);
        } else {
            emojiItem = (ImageView) convertView.getTag();
        }

        // 渲染单个表情
        FaceIconEmoji emoji = emojis.get(position);
        if (emoji != null) {
            Glide.with(context).load(emoji.getLocalFilePath()).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC).into(
                    emojiItem);
        }

        // 绑定单个表情的点击动作
        emojiItem.setOnClickListener(v -> {
            if (onClickItemListener != null) {
                onClickItemListener.onClick(emoji);
            }
        });

        return convertView;
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    public interface OnClickItemListener {
        void onClick(FaceIconEmoji emoji);
    }

}
/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2021 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.session;

import android.content.Context;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.alipay.fc.ccmimplus.sdk.core.model.conversation.Conversation;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.common.SelectSessionViewModel;

import cn.com.hsbc.hsbcchina.cert.databinding.FragmentSelectSessionItemBinding;

/**
 * 用于选择的会话列表项holder
 *
 * @author liyalong
 * @version SelectSessionItemHolder.java, v 0.1 2022年08月25日 17:43 liyalong
 */
public class SelectSessionItemHolder extends AbstractSessionItemHolder {

    private FragmentSelectSessionItemBinding binding;

    private SelectSessionViewModel selectItemViewModel;

    public SelectSessionItemHolder(Context context, View itemView) {
        super(context, itemView);

        binding = FragmentSelectSessionItemBinding.bind(itemView);

        // 绑定选择会话数据
        selectItemViewModel = new ViewModelProvider((AppCompatActivity) context).get(SelectSessionViewModel.class);
    }

    /**
     * 绑定数据
     *
     * @param c
     */
    @Override
    public void bind(Conversation c) {
        this.c = c;

        // 会话头像
        renderSessionLogoUrl(binding.avatar);

        // 会话标题
        renderTitle(binding.title);

        // 绑定组件动作
        bindAction(c);
    }

    private void bindAction(Conversation c) {
//        // 单选模式
//        if (((TransferMessageActivity) context).isSingleSelectMode()) {
//            binding.selectCheckBox.setVisibility(View.GONE);
//            binding.sessionInfoLayout.setOnClickListener(
//                    v -> ((TransferMessageActivity) context).onClickSingleSession(c));
//        }
//        // 多选模式
//        else {
//            binding.selectCheckBox.setVisibility(View.VISIBLE);
//            binding.selectCheckBox.setOnCheckedChangeListener(null);
//            binding.selectCheckBox.setChecked(selectItemViewModel.isSelected(c));
//            binding.selectCheckBox.setOnCheckedChangeListener(
//                    (v, b) -> selectItemViewModel.updateSelectedItems(c, b));
//        }
    }

}
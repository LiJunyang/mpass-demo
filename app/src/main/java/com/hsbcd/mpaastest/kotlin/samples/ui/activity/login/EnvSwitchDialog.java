/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.login;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.hsbcd.mpaastest.kotlin.samples.app.ImplusApplication;

import cn.com.hsbc.hsbcchina.cert.R;
import cn.com.hsbc.hsbcchina.cert.databinding.DialogEnvSwitchBinding;


/**
 * 登录环境切换对话框
 *
 * @author liyalong
 * @version EnvSwitchDialog.java, v 0.1 2022年09月20日 17:32 liyalong
 */
public class EnvSwitchDialog extends AppCompatDialogFragment {

    private Context context;

    private DialogEnvSwitchBinding binding;

    @IdRes
    private int checkedEnvId;

    @IdRes
    private int checkedTntId;

    public EnvSwitchDialog(Context context) {
        this.context = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DialogEnvSwitchBinding.inflate(inflater);
        bindAction();

        initEnvGroup();
        initTntGroup();

        return binding.getRoot();
    }

    private void bindAction() {
        binding.envGroup.setOnCheckedChangeListener(((group, checkedId) -> {
            checkedEnvId = checkedId;
        }));

        binding.tntGroup.setOnCheckedChangeListener(((group, checkedId) -> {
            checkedTntId = checkedId;
        }));

        binding.confirmButton.setOnClickListener(v -> {
            ImplusApplication.Companion.setCurrentEnvInfo(checkedEnvId);
            ImplusApplication.Companion.setCurrentTntInfo(checkedTntId);

            dismiss();
        });

        binding.cancelButton.setOnClickListener(v -> dismiss());
    }

    private void initEnvGroup() {
        ImplusApplication.Companion.getEnvInfoList().forEach(
                info -> binding.envGroup.addView(buildRadioButton(info.getId(), info.getName())));

        checkedEnvId = ImplusApplication.Companion.getCurrentEnvInfo().getId();
        binding.envGroup.check(checkedEnvId);
    }

    private void initTntGroup() {
        ImplusApplication.Companion.getTntInfoList().forEach(
                info -> binding.tntGroup.addView(buildRadioButton(info.getId(), info.getName())));

        checkedTntId = ImplusApplication.Companion.getCurrentTntInfo().getId();
        binding.tntGroup.check(checkedTntId);
    }

    private RadioButton buildRadioButton(@IdRes int id, String name) {
        RadioButton item = new RadioButton(context);

        item.setId(id);
        item.setText(name);
        item.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        item.setPadding(20, 0, 0, 0);
        item.setButtonDrawable(R.drawable.sel_checkbox);

        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 20, 0, 20);
        item.setLayoutParams(params);

        return item;
    }

}
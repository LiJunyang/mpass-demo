/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.common;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

/**
 * 列表元素多选数据
 *
 * @author liyalong
 * @version SelectItemViewModel.java, v 0.1 2022年08月25日 17:42 liyalong
 */
public abstract class SelectItemViewModel<T> extends ViewModel {

    private MutableLiveData<Collection<T>> selectedItemResult = new MutableLiveData<>();

    private Map<String, T> selectedItemMap = Maps.newLinkedHashMap();

    public void updateSelectedItems(T item, boolean checked) {
        String uniqueKey = getUniqueKey(item);

        if (checked) {
            selectedItemMap.put(uniqueKey, item);
        } else {
            selectedItemMap.remove(uniqueKey);
        }

        selectedItemResult.postValue(selectedItemMap.values());
    }

    protected abstract String getUniqueKey(T item);

    public MutableLiveData<Collection<T>> getSelectedItemResult() {
        return selectedItemResult;
    }

    public Collection<T> getSelectedItems() {
        return selectedItemMap.values();
    }

    public boolean isSelected(T item) {
        String uniqueKey = getUniqueKey(item);
        return selectedItemMap.containsKey(uniqueKey);
    }

    public void clear() {
        selectedItemMap.clear();
    }

}
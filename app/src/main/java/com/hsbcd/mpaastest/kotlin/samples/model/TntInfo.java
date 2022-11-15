/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.model;

import android.view.View;

import androidx.annotation.IdRes;

/**
 * @author liyalong
 * @version EnvInfo.java, v 0.1 2022年09月20日 19:50 liyalong
 */
public class TntInfo {

    @IdRes
    private int id = View.generateViewId();

    private String name;

    private String tntInstId;

    private String appId;

    private String envId;

    private String accessKey;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTntInstId() {
        return tntInstId;
    }

    public void setTntInstId(String tntInstId) {
        this.tntInstId = tntInstId;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEnvId() {
        return envId;
    }

    public void setEnvId(String envId) {
        this.envId = envId;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }
}
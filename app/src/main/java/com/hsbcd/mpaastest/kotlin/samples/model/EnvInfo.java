/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.model;

import android.view.View;

import androidx.annotation.IdRes;

import java.util.List;

/**
 * @author liyalong
 * @version TntInfo.java, v 0.1 2022年09月20日 19:50 liyalong
 */
public class EnvInfo {

    @IdRes
    private int id = View.generateViewId();

    private String name;

    private String wsHost;

    private String httpHost;

    private String mediaHost;

    private List<HostMapping> hostMappings;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWsHost() {
        return wsHost;
    }

    public void setWsHost(String wsHost) {
        this.wsHost = wsHost;
    }

    public String getHttpHost() {
        return httpHost;
    }

    public void setHttpHost(String httpHost) {
        this.httpHost = httpHost;
    }

    public String getMediaHost() {
        return mediaHost;
    }

    public void setMediaHost(String mediaHost) {
        this.mediaHost = mediaHost;
    }

    public List<HostMapping> getHostMappings() {
        return hostMappings;
    }

    public void setHostMappings(List<HostMapping> hostMappings) {
        this.hostMappings = hostMappings;
    }

    public class HostMapping {

        private String ip;

        private String hostname;

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }
    }

}
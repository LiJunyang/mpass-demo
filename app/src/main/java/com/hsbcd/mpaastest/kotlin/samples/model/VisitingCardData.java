/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 名片模型
 *
 * @author liyalong
 * @version VisitingCardData.java, v 0.1 2022年08月22日 20:47 liyalong
 */
public class VisitingCardData implements Parcelable {

    private String userId;

    private String userAvatar;

    private String userName;

    private String nickName;

    public VisitingCardData() {

    }

    protected VisitingCardData(Parcel in) {
        userId = in.readString();
        userAvatar = in.readString();
        userName = in.readString();
        nickName = in.readString();
    }

    public static final Creator<VisitingCardData> CREATOR = new Creator<VisitingCardData>() {
        @Override
        public VisitingCardData createFromParcel(Parcel in) {
            return new VisitingCardData(in);
        }

        @Override
        public VisitingCardData[] newArray(int size) {
            return new VisitingCardData[size];
        }
    };

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userId);
        dest.writeString(this.userAvatar);
        dest.writeString(this.userName);
        dest.writeString(this.nickName);
    }
}
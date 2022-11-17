/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

/**
 * @author liyalong
 * @version ImageUtil.java, v 0.1 2022年10月14日 15:46 liyalong
 */
public class ImageUtil {

    public static void asyncLoadImage(Context context, String filePath, ImageLoadedCallback callback) {
        Glide.with(context).asBitmap().load(filePath).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                callback.onCallback(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }
        });
    }

    public interface ImageLoadedCallback {
        void onCallback(Bitmap bitmap);
    }

}
/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.exifinterface.media.ExifInterface;

import com.alipay.fc.ccmimplus.sdk.core.config.SystemServerConfig;
import com.alipay.fc.ccmimplus.sdk.core.util.FileUtils;
import com.google.common.collect.Maps;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * 文件工具类
 *
 * @author liyalong
 * @version FileUtil.java, v 0.1 2022年08月17日 15:05 liyalong
 */
public class FileUtil {

    private static final Map<String, String> FILE_MIME_TYPE_MAP = Maps.newHashMap();

    static {
        FILE_MIME_TYPE_MAP.put("txt", "text/plain");
        FILE_MIME_TYPE_MAP.put("doc", "application/msword");
        FILE_MIME_TYPE_MAP.put("docx", "application/msword");
        FILE_MIME_TYPE_MAP.put("xls", "application/vnd.ms-excel");
        FILE_MIME_TYPE_MAP.put("xlsx", "application/vnd.ms-excel");
        FILE_MIME_TYPE_MAP.put("ppt", "application/vnd.ms-powerpoint");
        FILE_MIME_TYPE_MAP.put("pptx", "application/vnd.ms-powerpoint");
        FILE_MIME_TYPE_MAP.put("pdf", "application/pdf");
        FILE_MIME_TYPE_MAP.put("zip", "application/zip");
        FILE_MIME_TYPE_MAP.put("rar", "application/x-rar-compressed");
    }

    public static Uri generateFileUri(Context context, Uri externalPath, Uri internalPath) {
        Uri uri;

        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            uri = context.getContentResolver().insert(externalPath, new ContentValues());
        } else {
            uri = context.getContentResolver().insert(internalPath, new ContentValues());
        }

        return uri;
    }

    /**
     * uri转文件
     *
     * @param uri
     * @param context
     * @return
     */
    public static File uriToFile(Uri uri, Context context, boolean useOriginalFileName) {
        if (uri == null) {
            return null;
        }

        File file = null;

        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            // 把文件复制到沙盒目录
            try {
                ContentResolver contentResolver = context.getContentResolver();
                String fileName = useOriginalFileName ? getFileName(uri, context) : generateFileName(uri,
                        contentResolver);

                InputStream is = contentResolver.openInputStream(uri);
                File cache = new File(context.getCacheDir().getAbsolutePath(), fileName);
                FileOutputStream fos = new FileOutputStream(cache);
                IOUtils.copy(is, fos);
                file = cache;
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    private static String getFileName(Uri uri, Context context) {
        String fileName = null;

        String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
        Cursor cursor = context.getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
            }
            cursor.close();
        }

        return fileName;
    }

    private static String generateFileName(Uri uri, ContentResolver contentResolver) {
        return System.currentTimeMillis() + Math.round((Math.random() + 1) * 1000)
                + "." + MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(uri));
    }

    /**
     * uri转视频文件
     *
     * @param uri
     * @param type
     * @param context
     * @return
     */
    public static File uriToVideoFile(Uri uri, String type, Context context) {
        return uriToFileWithType(uri, type, context);
    }

    @Nullable
    private static File uriToFileWithType(Uri uri, String type, Context context) {
        if (uri == null) {
            return null;
        }

        File file = null;

        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            String displayName = System.currentTimeMillis() + Math.round((Math.random() + 1) * 1000) + "." + type;

            try {
                InputStream is = contentResolver.openInputStream(uri);
                File cache = new File(context.getCacheDir().getAbsolutePath(), displayName);
                FileOutputStream fos = new FileOutputStream(cache);
                IOUtils.copy(is, fos);
                file = cache;
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return file;
    }

    /**
     * 判断uri指向的文件是否为图片
     *
     * @param uri
     * @return
     */
    public static boolean isImage(Uri uri) {
        String uriStr = uri.toString();

        if (uriStr.contains("/image")) {
            return true;
        }

        String type = FileUtils.getExtension(uriStr);
        SystemServerConfig.ImageMessageConfig config = SystemServerConfig.getInstance().getMessageSendOptions().getImageConfig();
        return config.getSupportFileTypes().contains(type);
    }

    /**
     * 判断uri指向的文件是否为视频
     *
     * @param uri
     * @return
     */
    public static boolean isVideo(Uri uri) {
        String uriStr = uri.toString();

        String type = FileUtils.getExtension(uriStr);
        SystemServerConfig.VideoMessageConfig config = SystemServerConfig.getInstance().getMessageSendOptions().getVideoConfig();
        return config.getSupportFileTypes().contains(type);
    }

    /**
     * 获取图片长宽
     *
     * @param filePath
     * @return
     */
    public static Pair<Integer, Integer> getImageWidthHeight(String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        BitmapFactory.decodeFile(filePath, options);

        // 获取长宽
        int width = options.outWidth;
        int height = options.outHeight;

        // 获取图片旋转信息
        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try {
            ExifInterface exifInterface = new ExifInterface(filePath);
            orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 如果图片有旋转，对调长宽
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
            case ExifInterface.ORIENTATION_ROTATE_270:
                width = options.outHeight;
                height = options.outWidth;
                break;
        }

        return Pair.create(width, height);
    }

    /**
     * 获取视频截图和时长
     *
     * @param filePath
     * @return
     */
    public static Pair<Bitmap, Integer> getVideoSnapshotAndDuration(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);

        Bitmap snapshot = mmr.getFrameAtTime();
        int durationSeconds = Integer.valueOf(
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;

        mmr.release();
        return Pair.create(snapshot, durationSeconds);
    }

    /**
     * 获取语音时长
     *
     * @param filePath
     * @return
     */
    public static int getVoiceDuration(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(filePath);

        int durationSeconds = Integer.valueOf(
                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;

        mmr.release();
        return durationSeconds;
    }

    public static String getFileSize(int size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return size / 1024 + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            return size / (1024 * 1024) + " MB";
        } else {
            return size / (1024 * 1024 * 1024) + " GB";
        }
    }

    public static Intent getOpenFileIntent(String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Uri uri = Uri.parse(filePath);
        String type = StringUtils.defaultIfBlank(FILE_MIME_TYPE_MAP.get(FileUtils.getExtension(filePath)),
                "text/plain");
        intent.setDataAndType(uri, type);

        return intent;
    }

}
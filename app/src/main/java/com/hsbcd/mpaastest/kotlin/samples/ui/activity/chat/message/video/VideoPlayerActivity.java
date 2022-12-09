/*
 * Ant Group
 * Copyright (c) 2004-2022 All Rights Reserved.
 */
package com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.message.video;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import cn.com.hsbc.hsbcchina.cert.R;
import cn.com.hsbc.hsbcchina.cert.databinding.ActivityVideoPlayerBinding;
import com.hsbcd.mpaastest.kotlin.samples.app.ImplusApplication;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;

/**
 * 视频播放页
 * 使用ExoPlayer，参考：https://faanghut.com/implement-a-cache-for-exoplayer/
 *
 * @author liyalong
 * @version VideoPlayerActivity.java, v 0.1 2022年08月18日 19:48 liyalong
 */
public class VideoPlayerActivity extends AppCompatActivity {

    public static final String VIDEO_URL_KEY = "videoUrl";

    private ActivityVideoPlayerBinding binding;

    private ExoPlayer player;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVideoPlayerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.playVideoIcon.setVisibility(View.GONE);
        binding.playVideoIcon.setOnClickListener(v -> doPlay());

        String videoUrl = getIntent().getStringExtra(VIDEO_URL_KEY);
        initPlayer(videoUrl);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (player != null) {
            player.release();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    /**
     * 设置全屏，隐藏系统状态栏
     */
    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void initPlayer(String videoUrl) {
        player = new ExoPlayer.Builder(this).build();

        // 绑定到播放器视图
        binding.videoPlayer.setPlayer(player);

        ProgressiveMediaSource mediaSource = new ProgressiveMediaSource.Factory(
                new CacheDataSource.Factory()
                        .setCache(ImplusApplication.Companion.getSimpleCache())
                        .setUpstreamDataSourceFactory(
                                new DefaultHttpDataSource.Factory().setUserAgent(getString(R.string.app_name)))
                        .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        ).createMediaSource(MediaItem.fromUri(videoUrl));

        player.setMediaSource(mediaSource);
        player.prepare();
        player.setPlayWhenReady(true);

        player.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                binding.playVideoIcon.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
            }
        });
    }

    private void doPlay() {
        int state = player.getPlaybackState();

        if (state == Player.STATE_IDLE) {
            player.prepare();
        } else if (state == Player.STATE_ENDED) {
            player.seekTo(player.getCurrentMediaItemIndex(), C.TIME_UNSET);
        }

        player.play();
    }

}
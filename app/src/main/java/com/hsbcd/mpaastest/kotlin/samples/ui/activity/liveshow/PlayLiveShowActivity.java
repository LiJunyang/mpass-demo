package com.hsbcd.mpaastest.kotlin.samples.ui.activity.liveshow;


import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSource;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.hsbcd.mpaastest.kotlin.samples.ui.activity.chat.chatroom.ChatroomLiveActivity;
import com.hsbcd.mpaastest.kotlin.samples.util.ToastUtil;

import java.io.File;

import cn.hsbcsd.mpaastest.R;

public class PlayLiveShowActivity extends AppCompatActivity {
    private static final String LOG_TAG = PlayLiveShowActivity.class.getCanonicalName();

    private PlayerView playerView;
    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_liveshow);
        this.playerView = findViewById(R.id.player_view);
        initializePlayer();
    }

    private void initializePlayer() {
        if (player != null) {
            player.release();
        }
        player = new ExoPlayer.Builder(this).build();
        // 绑定播放器视图
        this.playerView.setPlayer(player);
        RtmpDataSource.Factory rtmpDataSource = new RtmpDataSource.Factory();
        MediaSource mediaSource = new ProgressiveMediaSource.Factory(rtmpDataSource).createMediaSource(
                MediaItem.fromUri("rtmp://iosjenkins.tk/live/liveshow"));
        player.setMediaSource(mediaSource);
        // 开始加载直播流
        player.prepare();
        player.setPlayWhenReady(true);
    }

    private void stopMedia() {
        if(player == null) {
            return;
        }
        player.release();
        player = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopMedia();
    }
}
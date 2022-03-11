package com.example.concatenatingplayer;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.decoder.DecoderReuseEvaluation;
import com.google.android.exoplayer2.extractor.ts.H265Reader;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.EventLogger;

public class MainActivity extends AppCompatActivity {

    private enum Mode {
        IMMERSIVE,
        NORMAL
    }

    private static final String TAG = "ConcatenatingExoPlayer";

    private PlayerView playerView;
    private ExoPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        playerView = findViewById(R.id.player_view);

    }

    @Override
    protected void onResume() {
        super.onResume();
        switchTo(Mode.IMMERSIVE);
        player = initializePlayer(playerView);
        player.play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayers();
    }

    protected ExoPlayer initializePlayer(PlayerView view) {
        return initializeSimplePlayerConcatenatingLocal(this, view, false);
    }

    private void shutdownPlayer(ExoPlayer player) {
        player.stop();
        player.release();
    }

    private void releasePlayers() {
        playerView = null;
        if (player != null) {
            shutdownPlayer(player);
            player = null;
        }
    }

    protected ExoPlayer initializeSimplePlayerConcatenatingLocal(Context context, PlayerView view, boolean playWhenReady) {

        String fileUrl1 = "file:///sdcard/Movies/hvc1.mp4";
        String fileUrl2 = "file:///sdcard/Movies/hev1.mp4";

        Log.d(TAG, "Initializing player for " + fileUrl1);
        MediaItem mediaItem1 = MediaItem.fromUri(fileUrl1);

        MediaSource fileSource1 = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, "Exoplayer-local")).
                createMediaSource(mediaItem1);

        Log.d(TAG, "Initializing player for " + fileUrl2);
        MediaItem mediaItem2 = MediaItem.fromUri(fileUrl2);

        MediaSource fileSource2 = new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(this, "Exoplayer-local")).
                createMediaSource(mediaItem2);

        DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
        DefaultLoadControl defaultLoadControl = new DefaultLoadControl();

        ExoPlayer player = new ExoPlayer.Builder(context)
                .setRenderersFactory(new PlayListRenderersFactory(context))
                .setTrackSelector(trackSelector)
                .setLoadControl(defaultLoadControl)
                .build();

        ConcatenatingMediaSource concatenatingMediaSource = new ConcatenatingMediaSource();
        concatenatingMediaSource.addMediaSource(fileSource1);
        concatenatingMediaSource.addMediaSource(fileSource2);

        H265Reader h;
        player.addAnalyticsListener(new EventLogger(trackSelector) {
            @Override
            public void onVideoInputFormatChanged(EventTime eventTime, Format format, @Nullable DecoderReuseEvaluation decoderReuseEvaluation) {
                super.onVideoInputFormatChanged(eventTime, format, decoderReuseEvaluation);
            }
        });

        player.setMediaSource(concatenatingMediaSource);
        player.prepare();
        player.setPlayWhenReady(playWhenReady);
        view.setPlayer(player);
        return player;
    }

    private void switchTo(Mode mode) {
        switch (mode) {
            case IMMERSIVE:
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                break;
            case NORMAL:
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_VISIBLE);
                break;
        }
    }
}
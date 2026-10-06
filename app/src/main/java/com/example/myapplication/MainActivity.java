package com.example.myapplication;

import android.app.PictureInPictureParams;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Rational;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

public class MainActivity extends AppCompatActivity {

    private static final String SAMPLE_VIDEO_URL =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4";

    private PlayerView playerView;
    private ExoPlayer player;
    private Button btnPip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 
        playerView = findViewById(R.id.player_view);
        btnPip = findViewById(R.id.btn_pip);

        btnPip.setOnClickListener(v -> enterPipMode());

        initializePlayer();
    }

    private void initializePlayer() {
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            playerView.setPlayer(player);

            MediaItem mediaItem = MediaItem.fromUri(SAMPLE_VIDEO_URL);
            player.setMediaItem(mediaItem);
            player.prepare();
            player.setPlayWhenReady(true);
        }
    }

    private void enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Rational aspectRatio = new Rational(16, 9);
            PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pipBuilder.setAutoEnterEnabled(true);
            }

            try {
                enterPictureInPictureMode(pipBuilder.build());
            } catch (Exception e) {
                Toast.makeText(this, "Không thể bật chế độ PiP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Thiết bị không hỗ trợ PiP (yêu cầu Android 8.0+)", Toast.LENGTH_SHORT).show();
        }
    }

    private void updatePipParams() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Rational aspectRatio = new Rational(16, 9);
            PictureInPictureParams.Builder pipBuilder = new PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                pipBuilder.setAutoEnterEnabled(true);
            }

            setPictureInPictureParams(pipBuilder.build());
        }
    }

    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        // Fallback cho Android 8.0 - 11 khi người dùng nhấn nút Home hoặc vuốt về Home
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (player != null && player.isPlaying()) {
                enterPipMode();
            }
        }
    }

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, @NonNull Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        if (isInPictureInPictureMode) {
            // Ẩn nút bấm và controller khi ở trong chế độ PiP
            btnPip.setVisibility(View.GONE);
            playerView.setUseController(false);
        } else {
            // Hiện lại nút bấm và controller khi thoát PiP
            btnPip.setVisibility(View.VISIBLE);
            playerView.setUseController(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
        updatePipParams();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player == null) {
            initializePlayer();
            updatePipParams();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Không giải phóng player nếu ứng dụng đang ở chế độ PiP
        if (isInPictureInPictureMode()) {
            return;
        }
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }
}

package com.example.practicasoctavo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;


public class MediaPlayerActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private TextView tvEstado;
    private ImageView ivAlbumArt;
    private ObjectAnimator animacionDisco;
    private final int SALTO_MS = 15000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        tvEstado = findViewById(R.id.tvEstado);
        ivAlbumArt = findViewById(R.id.ivAlbumArt);

        mediaPlayer = MediaPlayer.create(this, R.raw.audio_prueba);

        animacionDisco = ObjectAnimator.ofFloat(ivAlbumArt, "rotation", 0f, 360f);
        animacionDisco.setDuration(3000); // 3 segundos por vuelta
        animacionDisco.setRepeatCount(ValueAnimator.INFINITE);
        animacionDisco.setInterpolator(new LinearInterpolator()); // Velocidad constante
    }

    public void reproducirAudio(View v) {
        if (mediaPlayer == null) mediaPlayer = MediaPlayer.create(this, R.raw.audio_prueba);

        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            tvEstado.setText("Estado: Reproduciendo");
            if (animacionDisco.isPaused()) animacionDisco.resume();
            else animacionDisco.start();
        }
    }

    public void pausarAudio(View v) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            tvEstado.setText("Estado: Pausado");
            animacionDisco.pause();
        }
    }

    public void detenerAudio(View v) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            animacionDisco.end();
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
                tvEstado.setText("Estado: Detenido");
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void retroceder15(View v) {
        if (mediaPlayer != null) {
            int pos = mediaPlayer.getCurrentPosition();
            mediaPlayer.seekTo(Math.max(pos - SALTO_MS, 0));
            Toast.makeText(this, "-15s", Toast.LENGTH_SHORT).show();
        }
    }

    public void adelantar15(View v) {
        if (mediaPlayer != null) {
            int pos = mediaPlayer.getCurrentPosition();
            int total = mediaPlayer.getDuration();
            if (pos + SALTO_MS < total) mediaPlayer.seekTo(pos + SALTO_MS);
            else detenerAudio(null);
            Toast.makeText(this, "+15s", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        animacionDisco.cancel();
    }
}
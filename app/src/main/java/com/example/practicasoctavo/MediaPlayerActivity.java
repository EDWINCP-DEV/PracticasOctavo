package com.example.practicasoctavo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MediaPlayerActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private TextView tvEstado, tvTiempo;
    private SeekBar seekBar;
    private final int SALTO_MS = 15000;
    private Handler handler = new Handler();

    private int posicionGuardada = 0;
    private boolean estabaReproduciendo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        tvEstado = findViewById(R.id.tvEstado);
        tvTiempo = findViewById(R.id.tvTiempo);
        seekBar = findViewById(R.id.seekBar);

        if (savedInstanceState != null) {
            posicionGuardada = savedInstanceState.getInt("POSICION");
            estabaReproduciendo = savedInstanceState.getBoolean("REPRODUCIENDO");
        }

        inicializarMediaPlayer();
    }

    @Override
    public void onConfigurationChanged(@NonNull android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.activity_media_player);

        tvEstado = findViewById(R.id.tvEstado);
        tvTiempo = findViewById(R.id.tvTiempo);
        seekBar = findViewById(R.id.seekBar);

        actualizarInterfazManual();
        configurarSeekBar();
    }
    private void inicializarMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.audio_prueba);
            configurarSeekBar();
        }

        if (mediaPlayer != null) {
            mediaPlayer.seekTo(posicionGuardada);

            if (estabaReproduciendo) {
                reproducirAudio(null);
            } else {
                tvEstado.setText("Estado: Pausado");
                actualizarInterfazManual();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (mediaPlayer != null) {
            outState.putInt("POSICION", mediaPlayer.getCurrentPosition());
            outState.putBoolean("REPRODUCIENDO", mediaPlayer.isPlaying());
        }
        super.onSaveInstanceState(outState);
    }

    private void actualizarInterfazManual() {
        if (mediaPlayer != null) {
            int posActual = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(posActual);
            String tiempo = formatearTiempo(posActual) + " / " + formatearTiempo(mediaPlayer.getDuration());
            tvTiempo.setText(tiempo);
        }
    }

    private void configurarSeekBar() {
        if (mediaPlayer != null) {
            seekBar.setMax(mediaPlayer.getDuration());
        }
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                    actualizarInterfazManual();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private Runnable actualizarBarra = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                actualizarInterfazManual();
                handler.postDelayed(this, 1000);
            }
        }
    };

    public void reproducirAudio(View v) {
        if (mediaPlayer == null) inicializarMediaPlayer();
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            tvEstado.setText("Estado: Reproduciendo");
            handler.post(actualizarBarra);
        }
    }

    public void pausarAudio(View v) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            tvEstado.setText("Estado: Pausado");
            handler.removeCallbacks(actualizarBarra);
        }
    }

    public void detenerAudio(View v) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            handler.removeCallbacks(actualizarBarra);
            try {
                mediaPlayer.prepare();
                mediaPlayer.seekTo(0);
                seekBar.setProgress(0);
                tvEstado.setText("Estado: Detenido");
                tvTiempo.setText("00:00 / " + formatearTiempo(mediaPlayer.getDuration()));
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void retroceder15(View v) {
        if (mediaPlayer != null) {
            int pos = mediaPlayer.getCurrentPosition();
            mediaPlayer.seekTo(Math.max(pos - SALTO_MS, 0));
            actualizarInterfazManual();
        }
    }

    public void adelantar15(View v) {
        if (mediaPlayer != null) {
            int pos = mediaPlayer.getCurrentPosition();
            int total = mediaPlayer.getDuration();
            if (pos + SALTO_MS < total) {
                mediaPlayer.seekTo(pos + SALTO_MS);
                actualizarInterfazManual();
            } else {
                detenerAudio(null);
            }
        }
    }

    private String formatearTiempo(int ms) {
        int segundos = (ms / 1000) % 60;
        int minutos = (ms / (1000 * 60)) % 60;
        return String.format("%02d:%02d", minutos, segundos);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null && isFinishing()) {
            mediaPlayer.release();
            mediaPlayer = null;
            handler.removeCallbacks(actualizarBarra);
        }
    }
}
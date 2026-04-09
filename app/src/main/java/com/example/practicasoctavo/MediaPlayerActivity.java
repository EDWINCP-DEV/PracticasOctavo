package com.example.practicasoctavo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class MediaPlayerActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private TextView tvEstado, tvTiempo;
    private SeekBar seekBar;
    private SwitchMaterial swModoExterno;
    private MaterialButton btnSeleccionarArchivo;

    private final int SALTO_MS = 15000;
    private final Handler handler = new Handler();
    private Uri uriAudioExterno = null;
    private int posicionGuardada = 0;
    private boolean estabaReproduciendo = false;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    uriAudioExterno = result.getData().getData();
                    // Al seleccionar, reseteamos posición y cargamos
                    posicionGuardada = 0;
                    reiniciarMediaPlayer();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        // 1. Inicializar Vistas
        tvEstado = findViewById(R.id.tvEstado);
        tvTiempo = findViewById(R.id.tvTiempo);
        seekBar = findViewById(R.id.seekBar);
        swModoExterno = findViewById(R.id.swModoExterno);
        btnSeleccionarArchivo = findViewById(R.id.btnSeleccionarArchivo);

        // 2. Recuperar Estado (Rotación)
        if (savedInstanceState != null) {
            posicionGuardada = savedInstanceState.getInt("POSICION");
            estabaReproduciendo = savedInstanceState.getBoolean("REPRODUCIENDO");
            swModoExterno.setChecked(savedInstanceState.getBoolean("MODO"));

            String uriString = savedInstanceState.getString("URI");
            if (uriString != null) uriAudioExterno = Uri.parse(uriString);

            // Forzar visibilidad según el estado recuperado
            btnSeleccionarArchivo.setVisibility(swModoExterno.isChecked() ? View.VISIBLE : View.GONE);
        }

        // 3. Configurar Listeners e inicializar
        configurarEventos();
        inicializarMediaPlayer();
    }

    private void configurarEventos() {
        swModoExterno.setOnCheckedChangeListener((v, isChecked) -> {
            btnSeleccionarArchivo.setVisibility(isChecked ? View.VISIBLE : View.GONE);

            // Al cambiar de modo, liberamos el reproductor actual
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            posicionGuardada = 0;
            estabaReproduciendo = false;

            if (!isChecked) {
                uriAudioExterno = null; // Limpiar selección previa
                inicializarMediaPlayer(); // Carga el interno de inmediato
            } else {
                tvEstado.setText("Modo Externo: Seleccione un archivo");
                actualizarInterfazVacia();
            }
        });

        btnSeleccionarArchivo.setOnClickListener(v -> verificarPermisos());
    }

    private void inicializarMediaPlayer() {
        try {
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }

            if (!swModoExterno.isChecked()) {
                mediaPlayer = MediaPlayer.create(this, R.raw.audio_prueba);
                tvEstado.setText("Modo: Interno");
            } else if (uriAudioExterno != null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(this, uriAudioExterno);
                mediaPlayer.prepare();
                tvEstado.setText("Modo: Dispositivo");
            } else {
                return; // Esperando selección del usuario
            }

            if (mediaPlayer != null) {
                configurarSeekBar();
                mediaPlayer.seekTo(posicionGuardada);
                actualizarInterfazManual();
                if (estabaReproduciendo) {
                    mediaPlayer.start();
                    handler.post(actualizarBarra);
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al cargar audio", Toast.LENGTH_SHORT).show();
        }
    }

    private void reiniciarMediaPlayer() {
        estabaReproduciendo = true; // Queremos que suene al seleccionar
        inicializarMediaPlayer();
    }

    public void reproducirAudio(View v) {
        if (mediaPlayer == null) inicializarMediaPlayer();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            handler.post(actualizarBarra);
            tvEstado.setText("Reproduciendo...");
        }
    }

    public void pausarAudio(View v) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            handler.removeCallbacks(actualizarBarra);
            tvEstado.setText("Pausado");
        }
    }

    public void detenerAudio(View v) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
            posicionGuardada = 0;
            estabaReproduciendo = false;
            actualizarInterfazManual();
            handler.removeCallbacks(actualizarBarra);
            tvEstado.setText("Detenido");
        }
    }

    public void retroceder15(View v) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(Math.max(mediaPlayer.getCurrentPosition() - SALTO_MS, 0));
            actualizarInterfazManual();
        }
    }

    public void adelantar15(View v) {
        if (mediaPlayer != null) {
            int total = mediaPlayer.getDuration();
            int actual = mediaPlayer.getCurrentPosition();
            if (actual + SALTO_MS < total) {
                mediaPlayer.seekTo(actual + SALTO_MS);
                actualizarInterfazManual();
            } else {
                detenerAudio(null);
            }
        }
    }

    private void verificarPermisos() {
        String p = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                ? Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED) {
            abrirSelector();
        } else {
            requestPermissions(new String[]{p}, 100);
        }
    }

    private void abrirSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("audio/*");
        filePickerLauncher.launch(intent);
    }

    private void configurarSeekBar() {
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int p, boolean fromU) {
                if (fromU && mediaPlayer != null) {
                    mediaPlayer.seekTo(p);
                    actualizarInterfazManual();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {}
        });
    }

    private void actualizarInterfazManual() {
        if (mediaPlayer != null) {
            seekBar.setProgress(mediaPlayer.getCurrentPosition());
            tvTiempo.setText(formatearTiempo(mediaPlayer.getCurrentPosition()) + " / " + formatearTiempo(mediaPlayer.getDuration()));
        }
    }

    private void actualizarInterfazVacia() {
        seekBar.setProgress(0);
        tvTiempo.setText("00:00 / 00:00");
    }

    private final Runnable actualizarBarra = new Runnable() {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                actualizarInterfazManual();
                handler.postDelayed(this, 1000);
            }
        }
    };

    private String formatearTiempo(int ms) {
        int s = (ms / 1000) % 60;
        int m = (ms / (1000 * 60)) % 60;
        return String.format("%02d:%02d", m, s);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle out) {
        if (mediaPlayer != null) {
            out.putInt("POSICION", mediaPlayer.getCurrentPosition());
            out.putBoolean("REPRODUCIENDO", mediaPlayer.isPlaying());
        } else {
            out.putInt("POSICION", posicionGuardada);
            out.putBoolean("REPRODUCIENDO", estabaReproduciendo);
        }
        out.putBoolean("MODO", swModoExterno.isChecked());
        if (uriAudioExterno != null) out.putString("URI", uriAudioExterno.toString());
        super.onSaveInstanceState(out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(actualizarBarra);
    }
}
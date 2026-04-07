package com.example.practicasoctavo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MediaRecorderActivity extends AppCompatActivity {

    private MediaRecorder grabadora;
    private String rutaCarpeta;
    private String archivoActual;
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private Chronometer cronometro;
    private Button btnPausar, btnGrabar;
    private boolean estaPausado = false;
    private long tiempoPausado = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_recorder);

        cronometro = findViewById(R.id.cronometro);
        btnPausar = findViewById(R.id.btnPausar);
        btnGrabar = findViewById(R.id.btnGrabar);

        rutaCarpeta = getExternalFilesDir(null).getAbsolutePath();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO_PERMISSION);
        }
    }

    public void grabar(View view) {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        archivoActual = rutaCarpeta + "/AUDIO_" + timeStamp + ".3gp";

        grabadora = new MediaRecorder();
        grabadora.setAudioSource(MediaRecorder.AudioSource.MIC);
        grabadora.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        grabadora.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        grabadora.setOutputFile(archivoActual);

        try {
            grabadora.prepare();
            grabadora.start();

            cronometro.setBase(SystemClock.elapsedRealtime());
            cronometro.start();
            btnGrabar.setEnabled(false);
            btnPausar.setVisibility(View.VISIBLE);
            estaPausado = false;

            Toast.makeText(this, "Grabando...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("Grabadora", "Fallo en la grabación", e);
        }
    }

    public void pausarGrabacion(View view) {
        if (grabadora != null) {
            if (!estaPausado) {
                grabadora.pause();
                estaPausado = true;
                tiempoPausado = SystemClock.elapsedRealtime() - cronometro.getBase();
                cronometro.stop();
                btnPausar.setText("REANUDAR");
                Toast.makeText(this, "Grabación pausada", Toast.LENGTH_SHORT).show();
            } else {
                grabadora.resume();
                estaPausado = false;
                cronometro.setBase(SystemClock.elapsedRealtime() - tiempoPausado);
                cronometro.start();
                btnPausar.setText("PAUSAR");
                Toast.makeText(this, "Reanudando grabación...", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void detenerGrabacion(View view) {
        if (grabadora != null) {
            try {
                grabadora.stop();
            } catch (RuntimeException e) {
                Log.e("Grabadora", "Error al detener", e);
            }
            grabadora.release();
            grabadora = null;

            cronometro.stop();
            cronometro.setBase(SystemClock.elapsedRealtime());
            btnGrabar.setEnabled(true);
            btnPausar.setVisibility(View.GONE);
            btnPausar.setText("PAUSAR");

            Toast.makeText(this, "Audio guardado con éxito", Toast.LENGTH_LONG).show();
        }
    }

    public void reproducir(View view) {
        File carpeta = new File(rutaCarpeta);
        final File[] archivos = carpeta.listFiles();

        if (archivos == null || archivos.length == 0) {
            Toast.makeText(this, "No hay grabaciones", Toast.LENGTH_SHORT).show();
            return;
        }

        final String[] nombresArchivos = new String[archivos.length];
        for (int i = 0; i < archivos.length; i++) {
            nombresArchivos[i] = archivos[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecciona una grabación");
        builder.setItems(nombresArchivos, (dialog, which) -> {
            ejecutarReproduccion(archivos[which].getAbsolutePath());
        });
        builder.show();
    }

    private void ejecutarReproduccion(String ruta) {
        MediaPlayer mp = new MediaPlayer();
        try {
            mp.setDataSource(ruta);
            mp.prepare();
            mp.start();
            Toast.makeText(this, "Reproduciendo...", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.e("Grabadora", "Fallo en reproducción", e);
        }
    }
}
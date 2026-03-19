package com.example.practicasoctavo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;

public class VideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private int posicionActual = 0;
    private Uri uriActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        //seguridad de integracion
        //android.os.StrictMode.VmPolicy.Builder builder = new android.os.StrictMode.VmPolicy.Builder();
        //android.os.StrictMode.setVmPolicy(builder.build());

        videoView = findViewById(R.id.videoView);
        configurarControles();

        if (savedInstanceState != null) {
            posicionActual = savedInstanceState.getInt("posicion_video");
            String uriString = savedInstanceState.getString("uri_video");
            if (uriString != null) {
                uriActual = Uri.parse(uriString);
                prepararVideo(uriActual, "Estado Restaurado");
            }
        }
    }

    private void configurarControles() {
        MediaController mc = new MediaController(this);
        mc.setAnchorView(videoView);
        videoView.setMediaController(mc);
    }

    public void reproducirInterno(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_MEDIA_VIDEO}, 101);
                return;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
                return;
            }
        }

        ejecutarCargaInterna();
    }

    private void ejecutarCargaInterna() {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "prueba_video.mp4");

        if (file.exists()) {
            uriActual = Uri.parse(file.getAbsolutePath());
            posicionActual = 0;
            prepararVideo(uriActual, "Almacenamiento Interno");
        } else {
            Toast.makeText(this, "El video no está en la carpeta de descargas del dispositivo.", Toast.LENGTH_LONG).show();
        }
    }

    public void reproducirRaw(View v) {
        uriActual = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.prueba_video);
        posicionActual = 0;
        prepararVideo(uriActual, "RAW");
    }

    public void reproducirStreaming(View v) {
        uriActual = Uri.parse("https://ewincp-dev.com/prueba_video.mp4");
        posicionActual = 0;
        prepararVideo(uriActual, "Streaming");
    }

    private void prepararVideo(Uri uri, String fuente) {
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> {
            if (posicionActual > 0) {
                videoView.seekTo(posicionActual);
            }
            videoView.start();
            Toast.makeText(VideoActivity.this, "Reproduciendo: " + fuente, Toast.LENGTH_SHORT).show();
        });
    }

        @Override
        public void onConfigurationChanged(@NonNull Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            posicionActual = videoView.getCurrentPosition();
            setContentView(R.layout.activity_video);
            videoView = findViewById(R.id.videoView);
            configurarControles();
            if (uriActual != null) {
                prepararVideo(uriActual, "Rotación de pantalla");
            }
        }

        @Override
        protected void onSaveInstanceState(@NonNull Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("posicion_video", videoView.getCurrentPosition());
            if (uriActual != null) {
                outState.putString("uri_video", uriActual.toString());
            }
        }
}
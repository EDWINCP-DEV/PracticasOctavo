package com.example.practicasoctavo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class MenuPracticas extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menupracticas);
    }
    public void abrirSoundPool(View v) {
        startActivity(new Intent(this, PracticaSoundpoolActivity.class));
    }

    public void abrirMediaRecorder(View v) {
        startActivity(new Intent(this, MediaRecorderActivity.class));
    }

    public void abrirVideoView(View v) {
        startActivity(new Intent(this, VideoActivity.class));
    }

    public void abrirMediaPlayer(View v) {
        startActivity(new Intent(this, MediaPlayerActivity.class));
    }

    public void abrirMultimediaPlayer(View v) {
        startActivity(new Intent(this, MultimediaPlayerActivity.class));
    }
}
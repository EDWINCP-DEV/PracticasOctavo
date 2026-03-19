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
        Intent intent = new Intent(this, PracticaSoundpoolActivity.class);
        startActivity(intent);
    }

    public void abrirMediaRecorder(View v) {
        Intent intent = new Intent(this, MediaRecorderActivity.class);
        startActivity(intent);
    }

    public void abrirVideoView(View v) {
        Intent intent = new Intent(this, VideoActivity.class);
        startActivity(intent);
    }

    public void abrirMediaPlayer(View v) {
        Intent intent = new Intent(this, MediaPlayerActivity.class);
        startActivity(intent);
    }
}
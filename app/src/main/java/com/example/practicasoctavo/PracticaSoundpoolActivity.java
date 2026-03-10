package com.example.practicasoctavo;

import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class PracticaSoundpoolActivity extends AppCompatActivity {

    private SoundPool soundPool;
    private int idgrito, idherida, idqueja, idsalto, idsuspiro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practica_soundpool);

        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);

        idgrito = soundPool.load(this, R.raw.grito, 0);
        idherida = soundPool.load(this, R.raw.herida, 0);
        idqueja = soundPool.load(this, R.raw.queja, 0);
        idsalto = soundPool.load(this, R.raw.salto, 0);
        idsuspiro = soundPool.load(this, R.raw.suspiro, 0);
    }

    // Métodos para los 5 botones
    public void reproducirgrito(View v){
        soundPool.play(idgrito, 1, 1, 0, 0, 1);
    }

    public void reproducirherida(View v){
        soundPool.play(idherida, 1, 1, 1, 0, 1);
    }

    public void reproducirqueja(View v){
        soundPool.play(idqueja, 1, 1, 4, 0, 1);
    }

    public void reproducirsalto(View v){
        soundPool.play(idsalto, 1, 1, 2, 0, 1);
    }

    public void reproducirsuspiro(View v){
        soundPool.play(idsuspiro, 1, 1, 3, 0, 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }
}
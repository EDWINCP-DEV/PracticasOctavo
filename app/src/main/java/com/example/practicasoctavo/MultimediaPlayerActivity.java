package com.example.practicasoctavo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class MultimediaPlayerActivity extends AppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private VideoView videoView;
    private TextView txtEstado;
    private EditText editUrlNube;
    private Button btnModoWeb;
    private View layoutControlesFlotantes;

    private Uri fileUri;
    private int posicionActual = 0;

    private final Handler hideHandler = new Handler(Looper.getMainLooper());
    private final Runnable hideRunnable = () -> {
        if (layoutControlesFlotantes != null) layoutControlesFlotantes.setVisibility(View.GONE);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Carga inicial
        configurarVistasYListeners();

        if (savedInstanceState != null) {
            posicionActual = savedInstanceState.getInt("posicion", 0);
            String uriString = savedInstanceState.getString("uri");
            if (uriString != null) {
                fileUri = Uri.parse(uriString);
                videoView.setVideoURI(fileUri);
            }
        }

        // SOLICITUD MASIVA DE PERMISOS AL INICIAR
        verificarYSolicitarPermisos();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (videoView != null) posicionActual = videoView.getCurrentPosition();

        configurarVistasYListeners();

        if (fileUri != null) {
            actualizarLog("Sincronizando streaming... espera un momento.");
            videoView.setVideoURI(fileUri);

        }
    }

    private void configurarVistasYListeners() {
        setContentView(R.layout.activity_multimedia_player);

        videoView = findViewById(R.id.videoView);
        txtEstado = findViewById(R.id.txtEstado);
        editUrlNube = findViewById(R.id.editUrlNube);
        btnModoWeb = findViewById(R.id.btnModoWeb);
        layoutControlesFlotantes = findViewById(R.id.layoutControlesFlotantes);

        ImageButton btnPlay = findViewById(R.id.btnPlay);
        ImageButton btnPause = findViewById(R.id.btnPause);
        ImageButton btnStop = findViewById(R.id.btnStop);
        ImageButton btnForward = findViewById(R.id.btnForward);
        ImageButton btnRewind = findViewById(R.id.btnRewind);
        Button btnFiles = findViewById(R.id.btnFiles);

        if (videoView != null) {
            videoView.setZOrderMediaOverlay(true);
            videoView.setOnPreparedListener(mp -> {
                mp.setVideoScalingMode(android.media.MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                if (posicionActual > 0) videoView.seekTo(posicionActual);
                videoView.start();
                if (layoutControlesFlotantes != null) programarOcultarControles();
            });
            videoView.setOnClickListener(v -> {
                if (layoutControlesFlotantes != null) {
                    int vis = (layoutControlesFlotantes.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
                    layoutControlesFlotantes.setVisibility(vis);
                    if (vis == View.VISIBLE) programarOcultarControles();
                }
            });
        }

        // Lógica de URL (Botón + Teclado Enter)
        if (btnModoWeb != null) {
            btnModoWeb.setOnClickListener(v -> {
                int vis = (editUrlNube.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE;
                editUrlNube.setVisibility(vis);
                if (vis == View.VISIBLE) editUrlNube.requestFocus();
            });
        }

        if (editUrlNube != null) {
            editUrlNube.setOnEditorActionListener((v, actionId, event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                    reproducirCualquiera();
                    return true;
                }
                return false;
            });
        }

        // Botones Multimedia
        if (btnPlay != null) btnPlay.setOnClickListener(v -> reproducirCualquiera());
        if (btnPause != null) btnPause.setOnClickListener(v -> { if(videoView.isPlaying()) videoView.pause(); });
        if (btnStop != null) btnStop.setOnClickListener(v -> {
            videoView.stopPlayback();
            fileUri = null;
            posicionActual = 0;
            actualizarLog("Reproductor reseteado.");
        });

        if (btnForward != null) btnForward.setOnClickListener(v -> videoView.seekTo(videoView.getCurrentPosition() + 10000));
        if (btnRewind != null) btnRewind.setOnClickListener(v -> videoView.seekTo(videoView.getCurrentPosition() - 10000));
        if (btnFiles != null) btnFiles.setOnClickListener(v -> abrirExplorador());

        videoView.setOnErrorListener((mp, what, extra) -> {
            String errorMsg = "Error en URL: ";
            if (what == 1) errorMsg += "Archivo no encontrado (404)";
            else if (what == -1004) errorMsg += "Error de red/Internet";
            else errorMsg += "Formato no soportado";

            actualizarLog(errorMsg + " (Code: " + what + ")");
            return true;
        });
    }

    // --- SISTEMA DE PERMISOS REQUERIDO POR EL PROFESOR ---
    private void verificarYSolicitarPermisos() {
        List<String> permisosNecesarios = new ArrayList<>();

        // Permiso de Internet (Casi siempre es automático, pero se declara en el Manifest)
        permisosNecesarios.add(Manifest.permission.INTERNET);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ requiere permisos específicos por tipo de archivo
            permisosNecesarios.add(Manifest.permission.READ_MEDIA_VIDEO);
            permisosNecesarios.add(Manifest.permission.READ_MEDIA_AUDIO);
        } else {
            // Versiones anteriores usan el genérico de Almacenamiento
            permisosNecesarios.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permisosNecesarios.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        List<String> listaPendiente = new ArrayList<>();
        for (String p : permisosNecesarios) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                listaPendiente.add(p);
            }
        }

        if (!listaPendiente.isEmpty()) {
            ActivityCompat.requestPermissions(this, listaPendiente.toArray(new String[0]), PERMISSION_REQUEST_CODE);
        } else {
            actualizarLog("Todos los permisos concedidos.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "La app necesita permisos para funcionar", Toast.LENGTH_LONG).show();
                    return;
                }
            }
            actualizarLog("Permisos aceptados por el usuario.");
        }
    }

    private void reproducirCualquiera() {
        String url = (editUrlNube != null) ? editUrlNube.getText().toString().trim() : "";
        if (!url.isEmpty()) {
            actualizarLog("Conectando a Hostinger...");
            fileUri = Uri.parse(url);

            // Limpieza total antes de nueva conexión
            videoView.stopPlayback();
            videoView.setVideoURI(fileUri);

            // VITAL para streaming en algunos dispositivos
            videoView.requestFocus();

            editUrlNube.setVisibility(View.GONE);
        } else if (fileUri != null) {
            videoView.start();
        }
    }

    private void abrirExplorador() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, PICK_FILE_REQUEST);
    }

    private void actualizarLog(String m) { if (txtEstado != null) txtEstado.append("\n>> " + m); }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            fileUri = data.getData();
            final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getContentResolver().takePersistableUriPermission(fileUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            videoView.setVideoURI(fileUri);
            actualizarLog("Archivo local cargado.");
        }
    }@Override
    protected void onResume() {
        super.onResume();
        if (fileUri != null && !videoView.isPlaying() && videoView.getDuration() <= 0) {
            videoView.setVideoURI(fileUri);
            if (posicionActual > 0) {
                videoView.seekTo(posicionActual);
            }
            actualizarLog("Restaurando conexión de streaming...");
        }
    }
    private void programarOcultarControles() {
        hideHandler.removeCallbacks(hideRunnable);
        hideHandler.postDelayed(hideRunnable, 3000);
    }
}

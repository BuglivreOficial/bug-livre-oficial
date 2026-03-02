package com.bug.livre.oficial;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class PermissaoActivity extends AppCompatActivity {

    private MaterialButton btnInstalacao;
    private MaterialButton btnNotificacao;
    private MaterialButton btnSegundoPlano;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_permissao);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarComponentes();

        if (getPackageManager().canRequestPackageInstalls()) {
            btnInstalacao.setText("Concedida");
            btnInstalacao.setEnabled(false);
        }

        btnInstalacao.setOnClickListener(v -> permissaoInstalacao());

        btnNotificacao.setOnClickListener(v -> permissaoNotificacao());
        btnSegundoPlano.setOnClickListener(v -> permissaoSegundoPlano());
    }

    private void inicializarComponentes() {
        btnInstalacao = findViewById(R.id.btn_instalacao);
        btnNotificacao = findViewById(R.id.btn_notificacao);
        btnSegundoPlano = findViewById(R.id.btn_segundo_plano);
    }

    private void permissaoArmazenamento() {}

    private void permissaoInstalacao() {
        // Defina essa constante no topo da sua classe (ex: private static final int REQUEST_CODE = 1001;)
        if (!getPackageManager().canRequestPackageInstalls()) {

            // Em Java, instanciamos a Intent e configuramos os dados manualmente
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + getPackageName()));


            startActivityForResult(intent, 1200);
        }
    }

    private void permissaoNotificacao() {}

    private void permissaoSegundoPlano() {}

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1200) {
            if (getPackageManager().canRequestPackageInstalls()) {
                // Usuário ativou a permissão!
                btnInstalacao.setText("Concedida");
            } else {
                // Usuário recusou
                Toast.makeText(this, "Permissão negada pelo usuário", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
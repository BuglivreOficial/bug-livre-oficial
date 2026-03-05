package com.bug.livre.oficial;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class PermissaoActivity extends AppCompatActivity {

    private MaterialButton btnInstalacao, btnNotificacao, btnSegundoPlano, btnProximo;
    private LinearLayout linearNotificacao;

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

        //Verificar e encode o linear de notificação se for Android 8.0 (API 26) ao 12 (API 32)
        //se for Android 13 (API 33) ou maior não esconde o botão de conceder permissão
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            linearNotificacao.setVisibility(View.GONE);
        }

        //Verficar se a permissão de instalar aplicativos já foi concedida
        if (getPackageManager().canRequestPackageInstalls()) {
            btnInstalacao.setText("Concedida");
            btnInstalacao.setEnabled(false);
        }

        //Verificar se a permissão de notificações ja foi concedida
        if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
            btnNotificacao.setText("Concedida");
            btnNotificacao.setEnabled(false);
        }

        // Dentro do onCreate, após inicializar os componentes
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(getPackageName())) {
            btnSegundoPlano.setText("Concedida");
            btnSegundoPlano.setEnabled(false);
        }


        btnInstalacao.setOnClickListener(v -> permissaoInstalacao());
        btnNotificacao.setOnClickListener(v -> permissaoNotificacao());
        btnSegundoPlano.setOnClickListener(v -> solicitarPermissaoBateria());

        btnProximo.setOnClickListener(v -> {
            if (pm.isIgnoringBatteryOptimizations(getPackageName()) && NotificationManagerCompat.from(this).areNotificationsEnabled() && getPackageManager().canRequestPackageInstalls()) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Permissãos necessarias não concedida!", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void inicializarComponentes() {
        btnInstalacao = findViewById(R.id.btn_instalacao);
        btnNotificacao = findViewById(R.id.btn_notificacao);
        btnSegundoPlano = findViewById(R.id.btn_segundo_plano);

        btnProximo = findViewById(R.id.btn_proximo);

        linearNotificacao = findViewById(R.id.linear_notificacao);
    }

    private void permissaoInstalacao() {
        // Defina essa constante no topo da sua classe (ex: private static final int REQUEST_CODE = 1001;)
        if (!getPackageManager().canRequestPackageInstalls()) {

            // Em Java, instanciamos a Intent e configuramos os dados manualmente
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 110);
        }
    }

    private void permissaoNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 120);
            }
        }
    }

    public void solicitarPermissaoBateria() {
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        // Verifica se o app já está na lista de exceções
        if (!pm.isIgnoringBatteryOptimizations(packageName)) {
            Intent intent = new Intent();
            // ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS abre um diálogo direto de "Sim/Não"
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            startActivityForResult(intent, 130);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 110) {
            if (getPackageManager().canRequestPackageInstalls()) {
                // Usuário ativou a permissão!
                Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show();
                btnInstalacao.setText("Concedida");
                btnInstalacao.setEnabled(false);
            } else {
                // Usuário recusou
                Toast.makeText(this, "Permissão negada pelo usuário", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == 130) {
            // Criamos o PowerManager para checar o estado real, ignorando o resultCode
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

            if (pm.isIgnoringBatteryOptimizations(getPackageName())) {
                // Agora sim temos certeza que está concedida
                Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show();
                btnSegundoPlano.setText("Concedida");
                btnSegundoPlano.setEnabled(false);
            } else {
                // Realmente foi negada
                Toast.makeText(this, "Permissão negada pelo usuário.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 120) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Uhul! Usuário permitiu notificações
                Toast.makeText(this, "Permissão concedida!", Toast.LENGTH_SHORT).show();
                btnNotificacao.setText("Concedida");
                btnNotificacao.setEnabled(false);
            } else {
                Toast.makeText(this, "Vagabundo! concedar essa permissão", Toast.LENGTH_SHORT).show();
                // Usuário negou. Mostre um aviso explicando por que as notificações são úteis
            }
        }
    }


}
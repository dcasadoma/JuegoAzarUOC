package com.uoc.juegoazaruoc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private TextView tvWelcomeCoins;
    private ListView lvBestScores;
    private DatabaseHelper dbHelper;
    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Inicio - Lucky UOC");
        }

        tvWelcomeCoins = findViewById(R.id.tvWelcomeCoins);
        lvBestScores = findViewById(R.id.lvBestScores);
        Button btnStartGame = findViewById(R.id.btnStartGame);

        dbHelper = new DatabaseHelper(this);

        btnStartGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Cada vez que volvemos a la pantalla de bienvenida, refrescamos saldo y ranking con RxJava
        cargarDatosBienvenidaRx();
    }

    private void cargarDatosBienvenidaRx() {
        disposables.add(
                Single.fromCallable(() -> {
                            int coins = dbHelper.getPlayerCoins();
                            List<Partida> mejoresPartidas = dbHelper.getBestPartidas();
                            return new Pair<>(coins, mejoresPartidas);
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                            tvWelcomeCoins.setText("Monedas disponibles: " + pair.first);

                            List<String> itemsTexto = new ArrayList<>();
                            if (pair.second.isEmpty()) {
                                itemsTexto.add("Aún no hay partidas registradas. ¡Juega tu primera partida!");
                            } else {
                                for (Partida p : pair.second) {
                                    itemsTexto.add(p.toString());
                                }
                            }

                            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                    MainActivity.this,
                                    android.R.layout.simple_list_item_1,
                                    itemsTexto
                            );
                            lvBestScores.setAdapter(adapter);
                        }, throwable -> Toast.makeText(this, "Error al leer SQLite", Toast.LENGTH_SHORT).show())
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_game) {
            startActivity(new Intent(this, GameActivity.class));
            return true;
        } else if (id == R.id.menu_reset_coins) {
            reiniciarMonedasRx();
            return true;
        } else if (id == R.id.menu_exit) {
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void reiniciarMonedasRx() {
        disposables.add(
                Completable.fromAction(() -> dbHelper.updatePlayerCoins(100))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            cargarDatosBienvenidaRx();
                            Toast.makeText(this, "Saldo restablecido a 100 monedas", Toast.LENGTH_SHORT).show();
                        })
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
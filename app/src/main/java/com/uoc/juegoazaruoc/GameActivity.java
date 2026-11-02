package com.uoc.juegoazaruoc;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class GameActivity extends AppCompatActivity {

    private TextView tvGameCoins, tvResultMessage;
    private ImageView ivReel1, ivReel2, ivReel3;
    private Button btnSpin;

    private SlotMachineGame gameLogic;
    private DatabaseHelper dbHelper;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // Array de recursos estáticos (imágenes de los símbolos)
    private final int[] symbolDrawables = {
            R.drawable.symbol_cherry,
            R.drawable.symbol_bell,
            R.drawable.symbol_diamond,
            R.drawable.symbol_seven
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sala de Juego");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        tvGameCoins = findViewById(R.id.tvGameCoins);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        ivReel1 = findViewById(R.id.ivReel1);
        ivReel2 = findViewById(R.id.ivReel2);
        ivReel3 = findViewById(R.id.ivReel3);
        btnSpin = findViewById(R.id.btnSpin);

        dbHelper = new DatabaseHelper(this);

        // Cargamos el saldo inicial de SQLite de forma asíncrona con RxJava
        cargarMonedasRx();

        btnSpin.setOnClickListener(v -> ejecutarTirada());
    }

    private void cargarMonedasRx() {
        disposables.add(
                Single.fromCallable(() -> dbHelper.getPlayerCoins())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(coins -> {
                            gameLogic = new SlotMachineGame(coins);
                            actualizarMarcadorUI();
                        }, throwable -> Toast.makeText(this, "Error cargando saldo", Toast.LENGTH_SHORT).show())
        );
    }

    private void ejecutarTirada() {
        if (gameLogic == null) return;

        if (!gameLogic.canPlay()) {
            Toast.makeText(this, "¡Sin monedas! Usa el menú superior para recargar.", Toast.LENGTH_LONG).show();
            tvResultMessage.setText("Te has quedado a cero. ¡Recarga en el menú!");
            return;
        }

        btnSpin.setEnabled(false);
        tvResultMessage.setText("Girando rodillos...");

        // Ejecutamos el algoritmo aleatorio
        int[] resultadoRodillos = gameLogic.spinReels();
        int premio = gameLogic.calculateReward(resultadoRodillos);
        int saldoFinal = gameLogic.getCurrentCoins();

        // Lanzamos la animación visual de la partida
        animarRodillos(resultadoRodillos, premio, saldoFinal);
    }

    private void animarRodillos(int[] resultado, int premio, int saldoFinal) {
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(ivReel1, "rotationY", 0f, 720f);
        ObjectAnimator anim2 = ObjectAnimator.ofFloat(ivReel2, "rotationY", 0f, 720f);
        ObjectAnimator anim3 = ObjectAnimator.ofFloat(ivReel3, "rotationY", 0f, 720f);

        anim1.setDuration(600);
        anim2.setDuration(750);
        anim3.setDuration(900);

        anim3.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Actualizamos las imágenes estáticas según los números aleatorios obtenidos
                ivReel1.setImageResource(symbolDrawables[resultado[0]]);
                ivReel2.setImageResource(symbolDrawables[resultado[1]]);
                ivReel3.setImageResource(symbolDrawables[resultado[2]]);

                actualizarMarcadorUI();

                String textoResultado;
                if (premio > 0) {
                    textoResultado = "🎉 ¡PREMIO! Has ganado +" + premio + " monedas";
                } else {
                    textoResultado = "❌ Sin premio (-10 monedas). ¡Sigue intentándolo!";
                }
                tvResultMessage.setText(textoResultado);
                btnSpin.setEnabled(true);

                // Guardamos la partida en SQLite de forma asíncrona con RxJava
                String fechaActual = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date());
                String resumenDB = (premio > 0) ? "Premio +" + premio : "Sin premio";
                Partida nuevaPartida = new Partida(fechaActual, saldoFinal, resumenDB);

                guardarPartidaRx(nuevaPartida, saldoFinal);
            }
        });

        anim1.start();
        anim2.start();
        anim3.start();
    }

    private void guardarPartidaRx(Partida partida, int nuevoSaldo) {
        disposables.add(
                Completable.fromAction(() -> {
                            dbHelper.updatePlayerCoins(nuevoSaldo);
                            dbHelper.insertPartida(partida);
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                () -> { /* Guardado asíncrono completado con éxito */ },
                                throwable -> Toast.makeText(this, "Error al guardar en SQLite", Toast.LENGTH_SHORT).show()
                        )
        );
    }

    private void actualizarMarcadorUI() {
        tvGameCoins.setText("Saldo: " + gameLogic.getCurrentCoins() + " monedas");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home || id == R.id.menu_home) {
            finish();
            return true;
        } else if (id == R.id.menu_reset_coins) {
            recargarMonedasRx();
            return true;
        } else if (id == R.id.menu_exit) {
            finishAffinity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void recargarMonedasRx() {
        disposables.add(
                Completable.fromAction(() -> dbHelper.updatePlayerCoins(100))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            if (gameLogic != null) {
                                gameLogic.setCurrentCoins(100);
                                actualizarMarcadorUI();
                            }
                            Toast.makeText(this, "¡Monedas reiniciadas a 100!", Toast.LENGTH_SHORT).show();
                        })
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }
}
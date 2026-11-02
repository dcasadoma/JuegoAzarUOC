package com.uoc.juegoazaruoc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "juego_azar_uoc.db";
    private static final int DATABASE_VERSION = 1;

    // Tabla de estado del jugador (monedas actuales)
    private static final String TABLE_PLAYER = "jugador";
    private static final String COL_PLAYER_ID = "id";
    private static final String COL_COINS = "monedas";

    // Tabla de histórico de partidas y mejores puntuaciones
    private static final String TABLE_PARTIDAS = "partidas";
    private static final String COL_PARTIDA_ID = "id";
    private static final String COL_FECHA = "fecha";
    private static final String COL_PUNTUACION = "puntuacion";
    private static final String COL_RESULTADO = "resultado";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPlayerTable = "CREATE TABLE " + TABLE_PLAYER + " (" +
                COL_PLAYER_ID + " INTEGER PRIMARY KEY, " +
                COL_COINS + " INTEGER)";
        db.execSQL(createPlayerTable);

        // Insertamos 100 monedas iniciales para el jugador por defecto (ID = 1)
        ContentValues values = new ContentValues();
        values.put(COL_PLAYER_ID, 1);
        values.put(COL_COINS, 100);
        db.insert(TABLE_PLAYER, null, values);

        String createPartidasTable = "CREATE TABLE " + TABLE_PARTIDAS + " (" +
                COL_PARTIDA_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FECHA + " TEXT, " +
                COL_PUNTUACION + " INTEGER, " +
                COL_RESULTADO + " TEXT)";
        db.execSQL(createPartidasTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLAYER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTIDAS);
        onCreate(db);
    }

    // Obtener las monedas actuales del jugador
    public int getPlayerCoins() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_COINS + " FROM " + TABLE_PLAYER + " WHERE " + COL_PLAYER_ID + " = 1", null);
        int coins = 100;
        if (cursor.moveToFirst()) {
            coins = cursor.getInt(0);
        }
        cursor.close();
        return coins;
    }

    // Actualizar las monedas del jugador
    public void updatePlayerCoins(int newCoins) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_COINS, newCoins);
        db.update(TABLE_PLAYER, values, COL_PLAYER_ID + " = 1", null);
    }

    // Insertar una nueva partida en el histórico
    public void insertPartida(Partida partida) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FECHA, partida.getFecha());
        values.put(COL_PUNTUACION, partida.getPuntuacion());
        values.put(COL_RESULTADO, partida.getResultado());
        db.insert(TABLE_PARTIDAS, null, values);
    }

    // Obtener las mejores partidas ordenadas por puntuación (monedas alcanzadas) descendente
    public List<Partida> getBestPartidas() {
        List<Partida> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Ordenamos por puntuación máxima para cumplir la rúbrica de "mostrar las mejores (fecha y puntuación)"
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PARTIDAS +
                " ORDER BY " + COL_PUNTUACION + " DESC LIMIT 20", null);

        if (cursor.moveToFirst()) {
            do {
                String fecha = cursor.getString(cursor.getColumnIndexOrThrow(COL_FECHA));
                int puntuacion = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PUNTUACION));
                String resultado = cursor.getString(cursor.getColumnIndexOrThrow(COL_RESULTADO));
                lista.add(new Partida(fecha, puntuacion, resultado));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }
}
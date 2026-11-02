package com.uoc.juegoazaruoc;

public class Partida {
    private int id;
    private final String fecha;
    private final int puntuacion;
    private final String resultado;

    public Partida(String fecha, int puntuacion, String resultado) {
        this.fecha = fecha;
        this.puntuacion = puntuacion;
        this.resultado = resultado;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public String getResultado() {
        return resultado;
    }

    @Override
    public String toString() {
        return "📅 " + fecha + " | 🏆 Saldo: " + puntuacion + " (" + resultado + ")";
    }
}
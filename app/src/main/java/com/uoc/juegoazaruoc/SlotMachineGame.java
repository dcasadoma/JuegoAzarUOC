package com.uoc.juegoazaruoc;

import java.security.SecureRandom;

public class SlotMachineGame {

    // Parámetros fácilmente modificables para escalar la complejidad del juego
    public static final int NUM_REELS = 3;
    public static final int NUM_SYMBOLS = 4;
    public static final int SPIN_COST = 10;

    // Instancia única de SecureRandom para optimizar memoria y CPU
    private final SecureRandom secureRandom;
    private int currentCoins;

    public SlotMachineGame(int initialCoins) {
        this.secureRandom = new SecureRandom();
        this.currentCoins = initialCoins;
    }

    public boolean canPlay() {
        return currentCoins >= SPIN_COST;
    }

    // Genera los números aleatorios para cada rodillo (valores entre 0 y NUM_SYMBOLS - 1)
    public int[] spinReels() {
        currentCoins -= SPIN_COST;
        int[] results = new int[NUM_REELS];
        for (int i = 0; i < NUM_REELS; i++) {
            results[i] = secureRandom.nextInt(NUM_SYMBOLS);
        }
        return results;
    }

    // Evalúa la combinación obtenida y actualiza las monedas
    public int calculateReward(int[] reels) {
        int reward = 0;

        if (reels[0] == reels[1] && reels[1] == reels[2]) {
            // Tres símbolos iguales: si es el símbolo 3 (Estrella/Siete), Jackpot de 100
            reward = (reels[0] == 3) ? 100 : 50;
        } else if (reels[0] == reels[1] || reels[1] == reels[2] || reels[0] == reels[2]) {
            // Dos símbolos iguales
            reward = 20;
        }

        currentCoins += reward;
        return reward;
    }

    public int getCurrentCoins() {
        return currentCoins;
    }

    public void setCurrentCoins(int coins) {
        this.currentCoins = coins;
    }
}
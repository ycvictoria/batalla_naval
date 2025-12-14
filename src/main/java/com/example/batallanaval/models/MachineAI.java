package com.example.batallanaval.models;

import com.example.batallanaval.models.Board;

import java.util.*;

/**
 * Implementa una Inteligencia Artificial con estrategia "Hunt & Target".
 * La IA opera en dos modos:
 * 1. Hunt Mode (Caza):Dispara aleatoriamente a casillas no utilizadas.
 * 2. Target Mode (Objetivo):Una vez que se logra un acierto (HIT),
 * la IA entra en modo objetivo, disparando a las casillas adyacentes
 * al acierto para hundir el barco rápidamente.
 * La IA garantiza que nunca dispara dos veces a la misma casilla.
 */
public class MachineAI {

    private final Random random = new Random();

    /** Lista de tiros pendientes cuando la IA detecta un barco */
    private final Queue<int[]> targets = new ArrayDeque<>();

    /** Historial de tiros para evitar duplicados */
    private final Set<String> usedShots = new HashSet<>();

    /**
     * Devuelve el disparo de la IA como un array de coordenadas {fila, columna}.
     * @param playerBoard El tablero del oponente (jugador humano) sobre el que se va a disparar.
     * @return Array de dos enteros: {fila, columna} del disparo elegido.
     */
    public int[] shoot(Board playerBoard) {

        int size = playerBoard.getSize();

        if (!targets.isEmpty()) {
            int[] shot = targets.poll();
            String key = shot[0] + "," + shot[1];
            // Solo dispara si la casilla no ha sido utilizada antes.
            if (!usedShots.contains(key)) {
                usedShots.add(key);
                return shot;
            }
        }

        int row, col;
        String key;

        do {
            row = random.nextInt(size);
            col = random.nextInt(size);
            key = row + "," + col;
        } while (usedShots.contains(key));  // evitar duplicados

        usedShots.add(key);

        if (playerBoard.peek(row, col).hasShip()) {
            addAdjacentTargets(row, col, size);
        }

        return new int[] { row, col };
    }

    /**
     * Añade las coordenadas de los vecinos (Norte, Sur, Este, Oeste) de la casilla
     * impactada a la cola de objetivos prioritarios ({@code targets}).
     * Las coordenadas añadidas están dentro de los límites del tablero.
     * @param r    Fila del acierto.
     * @param c    Columna del acierto.
     * @param size Tamaño del tablero (para comprobación de límites).
     */
    private void addAdjacentTargets(int r, int c, int size) {

        if (r > 0)         targets.offer(new int[]{r - 1, c});
        if (r < size - 1)  targets.offer(new int[]{r + 1, c});
        if (c > 0)         targets.offer(new int[]{r, c - 1});
        if (c < size - 1)  targets.offer(new int[]{r, c + 1});
    }
}

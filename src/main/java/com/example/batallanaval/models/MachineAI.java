package com.example.batallanaval.models;

import com.example.batallanaval.models.Board;

import java.util.*;

/**
 * IA estilo "Hunt & Target".
 * - Dispara aleatoriamente hasta encontrar un barco.
 * - Cuando acierta, agrega los vecinos como objetivos prioritarios.
 * - Nunca dispara dos veces a la misma casilla.
 */
public class MachineAI {

    private final Random random = new Random();

    /** Lista de tiros pendientes cuando la IA detecta un barco */
    private final Queue<int[]> targets = new ArrayDeque<>();

    /** Historial de tiros para evitar duplicados */
    private final Set<String> usedShots = new HashSet<>();

    /**
     * Devuelve el disparo de la IA como {row, col}.
     */
    public int[] shoot(Board playerBoard) {

        int size = playerBoard.getSize();

        // =============================
        // 1. PRIORIDAD: tiros inteligentes (Target Mode)
        // =============================
        if (!targets.isEmpty()) {
            int[] shot = targets.poll();

            String key = shot[0] + "," + shot[1];
            if (!usedShots.contains(key)) {
                usedShots.add(key);
                return shot;
            }
        }

        // =============================
        // 2. RANDOM HUNT MODE
        // =============================
        int row, col;
        String key;

        do {
            row = random.nextInt(size);
            col = random.nextInt(size);
            key = row + "," + col;
        } while (usedShots.contains(key));  // evitar duplicados

        usedShots.add(key);

        // =============================
        // 3. Si es HIT → generar adyacentes
        // =============================
        if (playerBoard.peek(row, col).hasShip()) {
            addAdjacentTargets(row, col, size);
        }

        return new int[] { row, col };
    }

    /**
     * Añade los vecinos (arriba, abajo, izquierda, derecha) a la lista
     * para disparos inteligentes.
     */
    private void addAdjacentTargets(int r, int c, int size) {

        if (r > 0)         targets.offer(new int[]{r - 1, c});
        if (r < size - 1)  targets.offer(new int[]{r + 1, c});
        if (c > 0)         targets.offer(new int[]{r, c - 1});
        if (c < size - 1)  targets.offer(new int[]{r, c + 1});
    }
}

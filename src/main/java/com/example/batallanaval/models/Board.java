package com.example.batallanaval.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa el tablero de juego para la Batalla Naval.
 * Contiene la cuadrícula de celdas (Cell) y la lista de barcos (Ship) colocados
 * en el tablero.
 */
public class Board implements Serializable {
    /** ID de serialización para asegurar la compatibilidad al guardar/cargar. */
    private static final long serialVersionUID = 1L;

    /** Define el tamaño de la cuadrícula (10x10). */
    private static final int SIZE = 10;

    /** La cuadrícula 2D de celdas que compone el tablero. */
    private final Cell[][] grid = new Cell[SIZE][SIZE];

    /** Lista de objetos Ship colocados actualmente en el tablero. */
    private final List<Ship> fleet = new ArrayList<>();

    /**
     * Inicializa un nuevo tablero de juego (10x10) creando una instancia
     * de Cell para cada posición de la cuadrícula.
     */
    public Board() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = new Cell();
            }
        }
    }

    /**
     * Obtine el tamaño del tablero
     * @return El tamaño del lado del tablero.
     */
    public int getSize() {
        return SIZE;
    }

    /**
     * Devuelve la celda en la posición específica sin modificar su estado.
     * @param row La fila de la celda.
     * @param col La columna de la celda.
     * @return La instancia de Cell en (row, col).
     */
    public Cell peek(int row, int col) {
        return grid[row][col];
    }

    /**
     * Obtiene el número de barcos en la flota.
     * @return El número de barcos añadidos al tablero.
     */
    public int getFleetSize() {
        return fleet.size();
    }

    /**
     * Verifica si la flota está completa (10 barcos colocados).
     * @return true si hay 10 barcos en el tablero, false en caso contrario.
     */
    public boolean isFleetComplete() {
        return fleet.size() == 10;
    }

    /**
     * Verifica si un barco puede ser colocado en una posición específica
     * sin salirse de los límites del tablero y sin colisionar con otros barcos.
     * @param ship El barco a colocar (se usa su longitud).
     * @param row La fila inicial para la colocación.
     * @param col La columna inicial para la colocación.
     * @param horizontal Indica si la colocación es horizontal (true) o vertical (false).
     * @return true si el barco puede ser colocado, false si hay límites o colisiones.
     */
    public boolean canPlaceShip(Ship ship, int row, int col, boolean horizontal) {
        int length = ship.getLength();
        // Verificación de límites
        if (horizontal) {
            if (col + length > SIZE) return false;
        } else {
            if (row + length > SIZE) return false;
        }

        // Verificación de colisiones
        for (int i = 0; i < length; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);
            //Si la celda tiene barco, hay colisión
            if (grid[r][c].hasShip()) return false;
        }

        return true;
    }

    /**
     * Coloca el barco en las coordenadas especificadas si es posible.
     * @param ship El barco a colocar.
     * @param row La fila inicial.
     * @param col La columna inicial.
     * @param horizontal Orientación.
     */
    public void placeShip(Ship ship, int row, int col, boolean horizontal) {
        int length = ship.getLength();
        // Marcar las celdas ocupadas por el barco
        for (int i = 0; i < length; i++) {
            int r = row + (horizontal ? 0 : i);
            int c = col + (horizontal ? i : 0);

            grid[r][c].placeShip(ship);
        }
        // Marcar el barco como colocado y añadirlo a la flota
        ship.setPlaced(true);
        fleet.add(ship);
    }

    /**
     * Elimina un barco específico del tablero y de la flota.
     * @param ship El barco a remover.
     */
    public void removeShip(Ship ship) {
        // Recorrer toda la cuadrícula para encontrar todas las referencias a ese barco
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (grid[r][c].getShip() == ship) {
                    grid[r][c] = new Cell();
                }
            }
        }
        fleet.remove(ship);
    }

    /**
     * Intenta disparar a una celda específica del tablero.
     * @param row La fila del disparo.
     * @param col La columna del disparo.
     * @return El resultado del disparo (MISS, HIT, SUNK) o null si la celda ya había sido disparada.
     */
    public ShotResult shoot(int row, int col) {
        Cell cell = grid[row][col];
        // Verifica si ya fue disparado.
        if (cell.isShot()) return null;
        // Marca el disparo
        cell.markShot();
        // Evaluar resultado
        if (!cell.hasShip()) {
            return ShotResult.MISS; // Agua
        }
        // Sí hay barco
        Ship ship = cell.getShip();
        ship.registerHit(); // Registra impacto
        // Devolver el resultado final (HIT o SUNK)
        return ship.isSunk() ? ShotResult.SUNK : ShotResult.HIT;
    }

    /**
     * Coloca todos los 10 barcos de la flota estándar de Batalla Naval
     * en posiciones aleatorias y válidas dentro del tablero.
     */
    public void randomizeShips() {
        int[] lengths = {4, 3, 3, 2, 2, 2, 1, 1, 1, 1};

        for (int len : lengths) {
            Ship ship = new Ship(len);
            boolean placed = false;
            // Intentar colocar el barco hasta encontrar una posición válida
            while (!placed) {
                // Generar coordenadas y orientación aleatorias
                int row = (int) (Math.random() * SIZE);
                int col = (int) (Math.random() * SIZE);
                boolean horizontal = Math.random() < 0.5; // que halla un 50% que sea horizontal
                // Verificar si la colocación es válida
                if (canPlaceShip(ship, row, col, horizontal)) {
                    placeShip(ship, row, col, horizontal);
                    placed = true;
                }
            }
        }
    }

    /**
     * Verifica si el juego ha terminado para este tablero.
     * El juego termina si todos los barcos de la flota han sido hundidos.
     * @return true si la flota está vacía y todos los barcos están hundidos, false en caso contrario.
     */
    public boolean isGameOver() {
        return !fleet.isEmpty() &&
                fleet.stream().allMatch(Ship::isSunk);
    }

    /**
     * Limpia completamente el tablero, reiniciando la cuadrícula con celdas vacías
     * y eliminando todos los barcos de la flota.
     */
    public void clear() {
        // Reinicializar la cuadrícula
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = new Cell();
            }
        }
        // Limpiar la lista de barcos
        fleet.clear();
    }
}
